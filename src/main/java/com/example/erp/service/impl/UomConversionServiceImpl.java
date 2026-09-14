package com.example.erp.service.impl;

import com.example.erp.dto.ConvertUnitsRequest;
import com.example.erp.dto.ConvertUnitsResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomConversionFilterRequest;
import com.example.erp.dto.UomConversionRequest;
import com.example.erp.dto.UomConversionResponse;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.UomCategory;
import com.example.erp.entity.UomConversion;
import com.example.erp.exception.AppException;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.UomCategoryRepository;
import com.example.erp.repository.UomConversionRepository;
import com.example.erp.service.UomConversionService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UomConversionServiceImpl implements UomConversionService {

    private final UomConversionRepository repository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final UomCategoryRepository uomCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UomConversionResponse> list(UomConversionFilterRequest filter) {
        List<Specification<UomConversion>> conditions = new ArrayList<>();
        if (filter.getUnitOfMeasureId() != null) {
            conditions.add((root, query, cb) -> cb.or(
                    cb.equal(root.get("fromUnitOfMeasureId"), filter.getUnitOfMeasureId()),
                    cb.equal(root.get("toUnitOfMeasureId"), filter.getUnitOfMeasureId())));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<UomConversion> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<UomConversion> page = repository.findAll(spec, pageable);
        List<UomConversion> content = page.getContent();

        List<Long> unitIds = content.stream()
                .flatMap(c -> java.util.stream.Stream.of(c.getFromUnitOfMeasureId(), c.getToUnitOfMeasureId()))
                .distinct().toList();
        Map<Long, UnitOfMeasure> unitsById = unitOfMeasureRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        return PageResponse.of(page.map(c -> toResponse(c, unitsById)));
    }

    @Override
    public UomConversionResponse get(Long id) {
        UomConversion conversion = find(id);
        return toResponse(conversion, unitsMapFor(conversion));
    }

    @Override
    @Transactional
    public UomConversionResponse create(UomConversionRequest request) {
        UnitOfMeasure from = requireUnit(request.getFromUnitOfMeasureId());
        UnitOfMeasure to = requireUnit(request.getToUnitOfMeasureId());
        validatePair(from, to);
        if (repository.existsByFromUnitOfMeasureIdAndToUnitOfMeasureId(from.getId(), to.getId())) {
            throw new AppException(HttpStatus.CONFLICT, "A conversion from " + from.getName() + " to " + to.getName() + " already exists");
        }
        validateConsistency(from, to, request.getConversionFactor(), null);

        UomConversion conversion = UomConversion.builder()
                .fromUnitOfMeasureId(from.getId())
                .toUnitOfMeasureId(to.getId())
                .conversionFactor(request.getConversionFactor())
                .active(request.isActive())
                .build();
        repository.save(conversion);
        return toResponse(conversion, Map.of(from.getId(), from, to.getId(), to));
    }

    @Override
    @Transactional
    public UomConversionResponse update(Long id, UomConversionRequest request) {
        UomConversion conversion = find(id);
        UnitOfMeasure from = requireUnit(request.getFromUnitOfMeasureId());
        UnitOfMeasure to = requireUnit(request.getToUnitOfMeasureId());
        validatePair(from, to);
        if (repository.existsByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndIdNot(from.getId(), to.getId(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "A conversion from " + from.getName() + " to " + to.getName() + " already exists");
        }
        validateConsistency(from, to, request.getConversionFactor(), id);

        conversion.setFromUnitOfMeasureId(from.getId());
        conversion.setToUnitOfMeasureId(to.getId());
        conversion.setConversionFactor(request.getConversionFactor());
        conversion.setActive(request.isActive());
        repository.save(conversion);
        return toResponse(conversion, Map.of(from.getId(), from, to.getId(), to));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ConvertUnitsResponse convert(ConvertUnitsRequest request) {
        UnitOfMeasure from = requireUnit(request.getFromUnitOfMeasureId());
        UnitOfMeasure to = requireUnit(request.getToUnitOfMeasureId());

        BigDecimal converted = from.getId().equals(to.getId())
                ? request.getQuantity()
                : convertQuantity(from, to, request.getQuantity());

        return ConvertUnitsResponse.builder()
                .fromUnitOfMeasureId(from.getId())
                .quantity(request.getQuantity())
                .toUnitOfMeasureId(to.getId())
                .convertedQuantity(converted)
                .build();
    }

    private BigDecimal convertQuantity(UnitOfMeasure from, UnitOfMeasure to, BigDecimal quantity) {
        Optional<UomConversion> direct = repository.findByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndActiveTrue(from.getId(), to.getId());
        if (direct.isPresent()) {
            return quantity.multiply(direct.get().getConversionFactor());
        }
        Optional<UomConversion> reverse = repository.findByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndActiveTrue(to.getId(), from.getId());
        if (reverse.isPresent()) {
            return quantity.divide(reverse.get().getConversionFactor(), 6, RoundingMode.HALF_UP);
        }

        if (from.getCategoryId() == null || to.getCategoryId() == null || !from.getCategoryId().equals(to.getCategoryId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "No conversion defined between " + from.getName() + " and " + to.getName());
        }
        UomCategory category = uomCategoryRepository.findById(from.getCategoryId()).orElse(null);
        Long baseUnitId = category == null ? null : category.getBaseUnitId();
        if (baseUnitId == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This category has no base unit defined yet");
        }
        BigDecimal fromFactor = factorToBase(from.getId(), baseUnitId);
        BigDecimal toFactor = factorToBase(to.getId(), baseUnitId);
        BigDecimal baseQuantity = quantity.multiply(fromFactor);
        return baseQuantity.divide(toFactor, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal factorToBase(Long unitId, Long baseUnitId) {
        if (unitId.equals(baseUnitId)) return BigDecimal.ONE;
        Optional<UomConversion> direct = repository.findByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndActiveTrue(unitId, baseUnitId);
        if (direct.isPresent()) {
            return direct.get().getConversionFactor();
        }
        Optional<UomConversion> reverse = repository.findByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndActiveTrue(baseUnitId, unitId);
        if (reverse.isPresent()) {
            return BigDecimal.ONE.divide(reverse.get().getConversionFactor(), 6, RoundingMode.HALF_UP);
        }
        throw new AppException(HttpStatus.BAD_REQUEST, "No conversion factor to base defined for unit id: " + unitId);
    }

    // Guards against a from→to factor silently disagreeing with an
    // already-stored to→from row (e.g. BOX→PCS=24 alongside PCS→BOX=0.05
    // instead of 0.041667) — both directions are stored independently, so
    // nothing else keeps them in sync.
    private void validateConsistency(UnitOfMeasure from, UnitOfMeasure to, BigDecimal factor, Long excludeId) {
        repository.findByFromUnitOfMeasureIdAndToUnitOfMeasureId(to.getId(), from.getId())
                .filter(reverse -> excludeId == null || !reverse.getId().equals(excludeId))
                .ifPresent(reverse -> {
                    BigDecimal product = factor.multiply(reverse.getConversionFactor());
                    if (product.subtract(BigDecimal.ONE).abs().compareTo(new BigDecimal("0.0005")) > 0) {
                        BigDecimal expected = BigDecimal.ONE.divide(reverse.getConversionFactor(), 6, RoundingMode.HALF_UP);
                        throw new AppException(HttpStatus.CONFLICT, "Inconsistent with the existing " + to.getName() + " → "
                                + from.getName() + " conversion (factor " + reverse.getConversionFactor() + "): this should be about "
                                + expected + ", not " + factor);
                    }
                });
    }

    private void validatePair(UnitOfMeasure from, UnitOfMeasure to) {
        if (from.getId().equals(to.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "From and to units must be different");
        }
        if (from.getCategoryId() == null || to.getCategoryId() == null || !from.getCategoryId().equals(to.getCategoryId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Both units must belong to the same UOM category");
        }
    }

    private UnitOfMeasure requireUnit(Long unitOfMeasureId) {
        return unitOfMeasureRepository.findById(unitOfMeasureId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Unit of measure not found with id: " + unitOfMeasureId));
    }

    private UomConversion find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "UOM conversion not found with id: " + id));
    }

    private Map<Long, UnitOfMeasure> unitsMapFor(UomConversion conversion) {
        return unitOfMeasureRepository.findAllById(List.of(conversion.getFromUnitOfMeasureId(), conversion.getToUnitOfMeasureId())).stream()
                .collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));
    }

    private UomConversionResponse toResponse(UomConversion conversion, Map<Long, UnitOfMeasure> unitsById) {
        UnitOfMeasure from = unitsById.get(conversion.getFromUnitOfMeasureId());
        UnitOfMeasure to = unitsById.get(conversion.getToUnitOfMeasureId());
        return UomConversionResponse.builder()
                .id(conversion.getId())
                .fromUnitOfMeasureId(conversion.getFromUnitOfMeasureId())
                .fromUnitOfMeasureName(from == null ? null : from.getName())
                .fromUnitOfMeasureAbbreviation(from == null ? null : from.getAbbreviation())
                .toUnitOfMeasureId(conversion.getToUnitOfMeasureId())
                .toUnitOfMeasureName(to == null ? null : to.getName())
                .toUnitOfMeasureAbbreviation(to == null ? null : to.getAbbreviation())
                .conversionFactor(conversion.getConversionFactor())
                .active(conversion.isActive())
                .build();
    }
}
