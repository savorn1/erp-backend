package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomCategoryFilterRequest;
import com.example.erp.dto.UomCategoryRequest;
import com.example.erp.dto.UomCategoryResponse;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.UomCategory;
import com.example.erp.entity.UomConversion;
import com.example.erp.exception.AppException;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.UomCategoryRepository;
import com.example.erp.repository.UomConversionRepository;
import com.example.erp.service.UomCategoryService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UomCategoryServiceImpl implements UomCategoryService {

    private final UomCategoryRepository repository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final UomConversionRepository uomConversionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UomCategoryResponse> list(UomCategoryFilterRequest filter) {
        List<Specification<UomCategory>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<UomCategory> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<UomCategory> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public UomCategoryResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public UomCategoryResponse create(UomCategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "UOM category name already taken: " + request.getName());
        }
        if (repository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "UOM category code already taken: " + request.getCode());
        }
        UomCategory category = UomCategory.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        repository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public UomCategoryResponse update(Long id, UomCategoryRequest request) {
        UomCategory category = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "UOM category name already taken: " + request.getName());
        }
        if (repository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "UOM category code already taken: " + request.getCode());
        }
        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.isActive());
        repository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        find(id);
        if (unitOfMeasureRepository.existsByCategoryId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a UOM category that still has units assigned to it");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public List<UomCategoryResponse> seedStandardCategories() {
        // factorToBase is null for the base unit itself, and also for BOX/CTN
        // — how many pieces they hold isn't a universal constant (that's
        // exactly why ProductUom exists for per-product packaging), so those
        // are seeded uncategorized-factor and left for an admin (or a
        // product's own ProductUom row) to define. G/TON/ML/CM/MINUTE/DAY get
        // real metric factors since those ratios are fixed. decimalAllowed is
        // false for Quantity units (you can't sell half a piece) and true
        // everywhere else.
        record SeedUnit(String name, String abbreviation, boolean base, BigDecimal factorToBase, boolean decimalAllowed) {}
        record SeedCategory(String code, String name, String description, List<SeedUnit> units) {}
        List<SeedCategory> seeds = List.of(
                new SeedCategory("QUANTITY", "Quantity", "Units used to count products", List.of(
                        new SeedUnit("Piece", "PCS", true, null, false),
                        new SeedUnit("Box", "BOX", false, null, false),
                        new SeedUnit("Carton", "CTN", false, null, false)
                )),
                new SeedCategory("WEIGHT", "Weight", "Units used to measure weight", List.of(
                        new SeedUnit("Kilogram", "KG", true, null, true),
                        new SeedUnit("Gram", "G", false, new BigDecimal("0.001"), true),
                        new SeedUnit("Ton", "TON", false, new BigDecimal("1000"), true)
                )),
                new SeedCategory("VOLUME", "Volume", "Units used to measure volume", List.of(
                        new SeedUnit("Liter", "L", true, null, true),
                        new SeedUnit("Milliliter", "ML", false, new BigDecimal("0.001"), true)
                )),
                new SeedCategory("LENGTH", "Length", "Units used to measure length", List.of(
                        new SeedUnit("Meter", "M", true, null, true),
                        new SeedUnit("Centimeter", "CM", false, new BigDecimal("0.01"), true)
                )),
                new SeedCategory("AREA", "Area", "Units used to measure area", List.of(
                        new SeedUnit("Square Meter", "M2", true, null, true)
                )),
                new SeedCategory("TIME", "Time", "Units used to measure time", List.of(
                        new SeedUnit("Minute", "MINUTE", false, new BigDecimal("0.016667"), true),
                        new SeedUnit("Hour", "HOUR", true, null, true),
                        new SeedUnit("Day", "DAY", false, new BigDecimal("24"), true)
                ))
        );

        List<UomCategory> result = new ArrayList<>();
        for (SeedCategory seed : seeds) {
            UomCategory category = repository.findByCode(seed.code())
                    .orElseGet(() -> repository.save(UomCategory.builder()
                            .code(seed.code())
                            .name(seed.name())
                            .description(seed.description())
                            .active(true)
                            .build()));

            for (SeedUnit seedUnit : seed.units()) {
                if (unitOfMeasureRepository.existsByName(seedUnit.name())) continue;
                UnitOfMeasure unit = unitOfMeasureRepository.save(UnitOfMeasure.builder()
                        .name(seedUnit.name())
                        .abbreviation(seedUnit.abbreviation())
                        .decimalAllowed(seedUnit.decimalAllowed())
                        .categoryId(category.getId())
                        .baseUnit(seedUnit.base())
                        .active(true)
                        .build());
                if (seedUnit.base() && category.getBaseUnitId() == null) {
                    category.setBaseUnitId(unit.getId());
                    repository.save(category);
                } else if (!seedUnit.base() && seedUnit.factorToBase() != null && category.getBaseUnitId() != null) {
                    uomConversionRepository.save(UomConversion.builder()
                            .fromUnitOfMeasureId(unit.getId())
                            .toUnitOfMeasureId(category.getBaseUnitId())
                            .conversionFactor(seedUnit.factorToBase())
                            .active(true)
                            .build());
                }
            }
            result.add(category);
        }

        return result.stream().map(this::toResponse).toList();
    }

    private UomCategory find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "UOM category not found with id: " + id));
    }

    private UomCategoryResponse toResponse(UomCategory category) {
        UnitOfMeasure baseUnit = category.getBaseUnitId() == null ? null : unitOfMeasureRepository.findById(category.getBaseUnitId()).orElse(null);
        return UomCategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .baseUnitId(baseUnit == null ? null : baseUnit.getId())
                .baseUnitName(baseUnit == null ? null : baseUnit.getName())
                .baseUnitAbbreviation(baseUnit == null ? null : baseUnit.getAbbreviation())
                .build();
    }
}
