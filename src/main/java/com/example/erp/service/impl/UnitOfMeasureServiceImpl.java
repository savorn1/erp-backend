package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UnitOfMeasureFilterRequest;
import com.example.erp.dto.UnitOfMeasureRequest;
import com.example.erp.dto.UnitOfMeasureResponse;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.UomCategory;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.UomCategoryRepository;
import com.example.erp.repository.UomConversionRepository;
import com.example.erp.service.UnitOfMeasureService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;
    private final UomCategoryRepository uomCategoryRepository;
    private final UomConversionRepository uomConversionRepository;
    private final ProductRepository productRepository;
    private final ProductUomRepository productUomRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnitOfMeasureResponse> list(UnitOfMeasureFilterRequest filter) {
        List<Specification<UnitOfMeasure>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<UnitOfMeasure> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<UnitOfMeasure> page = repository.findAll(spec, pageable);
        List<UnitOfMeasure> content = page.getContent();
        Map<Long, UomCategory> categoriesById = uomCategoryRepository.findAllById(
                content.stream().map(UnitOfMeasure::getCategoryId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UomCategory::getId, c -> c));

        return PageResponse.of(page.map(u -> toResponse(u, categoriesById.get(u.getCategoryId()))));
    }

    @Override
    public UnitOfMeasureResponse get(Long id) {
        UnitOfMeasure uom = find(id);
        UomCategory category = categoryOf(uom.getCategoryId());
        return toResponse(uom, category);
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse create(UnitOfMeasureRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Unit of measure name already taken: " + request.getName());
        }
        UomCategory category = requireCategory(request.getCategoryId());
        boolean wantsBase = category != null && request.isBaseUnit();

        UnitOfMeasure uom = UnitOfMeasure.builder()
                .name(request.getName())
                .abbreviation(request.getAbbreviation())
                .description(request.getDescription())
                .decimalAllowed(request.isDecimalAllowed())
                .active(request.isActive())
                .categoryId(category == null ? null : category.getId())
                .baseUnit(wantsBase)
                .build();
        repository.save(uom);

        if (wantsBase) {
            setCategoryBaseUnit(category, uom);
        }
        return toResponse(uom, category);
    }

    @Override
    @Transactional
    public UnitOfMeasureResponse update(Long id, UnitOfMeasureRequest request) {
        UnitOfMeasure uom = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Unit of measure name already taken: " + request.getName());
        }
        UomCategory category = requireCategory(request.getCategoryId());
        boolean wantsBase = category != null && request.isBaseUnit();
        boolean wasBase = Boolean.TRUE.equals(uom.getBaseUnit());
        Long oldCategoryId = uom.getCategoryId();

        if (wasBase && !wantsBase && oldCategoryId != null) {
            UomCategory oldCategory = categoryOf(oldCategoryId);
            if (oldCategory != null && id.equals(oldCategory.getBaseUnitId())) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Cannot unset the base unit of a category — assign a different unit as base instead");
            }
        }

        uom.setName(request.getName());
        uom.setAbbreviation(request.getAbbreviation());
        uom.setDescription(request.getDescription());
        uom.setDecimalAllowed(request.isDecimalAllowed());
        uom.setActive(request.isActive());
        uom.setCategoryId(category == null ? null : category.getId());
        uom.setBaseUnit(wantsBase);
        repository.save(uom);

        // A category change invalidates any conversion this unit had defined
        // against its old category's base — see UomConversionServiceImpl for
        // where those rows are actually managed now.
        boolean categoryChanged = !Objects.equals(oldCategoryId, uom.getCategoryId());
        if (categoryChanged) {
            uomConversionRepository.deleteByFromUnitOfMeasureId(uom.getId());
        }
        if (wantsBase) {
            setCategoryBaseUnit(category, uom);
        }
        return toResponse(uom, category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UnitOfMeasure uom = find(id);
        UomCategory category = categoryOf(uom.getCategoryId());
        if (category != null && id.equals(category.getBaseUnitId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a category's base unit — assign a different base unit first");
        }
        if (productRepository.existsByUnitOfMeasureId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a unit of measure that a product uses as its base unit");
        }
        if (productUomRepository.existsByUnitOfMeasureId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a unit of measure that a product's alternate UOMs use");
        }
        uomConversionRepository.deleteByFromUnitOfMeasureIdOrToUnitOfMeasureId(id, id);
        repository.delete(uom);
    }

    // Only one unit per category may be the base unit — flagging a new one
    // unsets whichever one held it before and repoints UomCategory.baseUnitId
    // at it. Other units' existing conversion-to-(old)base rows are left as
    // they were, same simplification the prior single-column design made.
    private void setCategoryBaseUnit(UomCategory category, UnitOfMeasure newBaseUnit) {
        for (UnitOfMeasure other : repository.findByCategoryId(category.getId())) {
            if (other.getId().equals(newBaseUnit.getId()) || !Boolean.TRUE.equals(other.getBaseUnit())) continue;
            other.setBaseUnit(false);
            repository.save(other);
        }
        category.setBaseUnitId(newBaseUnit.getId());
        uomCategoryRepository.save(category);
        uomConversionRepository.deleteByFromUnitOfMeasureId(newBaseUnit.getId());
    }

    private UomCategory requireCategory(Long categoryId) {
        if (categoryId == null) return null;
        return uomCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "UOM category not found with id: " + categoryId));
    }

    private UomCategory categoryOf(Long categoryId) {
        return categoryId == null ? null : uomCategoryRepository.findById(categoryId).orElse(null);
    }

    private UnitOfMeasure find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Unit of measure not found with id: " + id));
    }

    private UnitOfMeasureResponse toResponse(UnitOfMeasure uom, UomCategory category) {
        return UnitOfMeasureResponse.builder()
                .id(uom.getId())
                .name(uom.getName())
                .abbreviation(uom.getAbbreviation())
                .description(uom.getDescription())
                .decimalAllowed(!Boolean.FALSE.equals(uom.getDecimalAllowed()))
                .active(uom.isActive())
                .categoryId(uom.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .baseUnit(Boolean.TRUE.equals(uom.getBaseUnit()))
                .build();
    }
}
