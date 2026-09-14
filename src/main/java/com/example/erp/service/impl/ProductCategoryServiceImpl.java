package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductCategoryFilterRequest;
import com.example.erp.dto.ProductCategoryRequest;
import com.example.erp.dto.ProductCategoryResponse;
import com.example.erp.entity.ProductCategory;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductCategoryRepository;
import com.example.erp.service.ProductCategoryService;
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

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductCategoryResponse> list(ProductCategoryFilterRequest filter) {
        List<Specification<ProductCategory>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<ProductCategory> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductCategory> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public ProductCategoryResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductCategoryResponse create(ProductCategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Category name already taken: " + request.getName());
        }
        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .active(request.isActive())
                .build();
        repository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public ProductCategoryResponse update(Long id, ProductCategoryRequest request) {
        ProductCategory category = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Category name already taken: " + request.getName());
        }
        category.setName(request.getName());
        category.setActive(request.isActive());
        repository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private ProductCategory find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Category not found with id: " + id));
    }

    private ProductCategoryResponse toResponse(ProductCategory category) {
        return ProductCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .active(category.isActive())
                .build();
    }
}
