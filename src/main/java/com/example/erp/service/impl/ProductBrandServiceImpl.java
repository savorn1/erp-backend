package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductBrandFilterRequest;
import com.example.erp.dto.ProductBrandRequest;
import com.example.erp.dto.ProductBrandResponse;
import com.example.erp.entity.ProductBrand;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductBrandRepository;
import com.example.erp.service.ProductBrandService;
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
public class ProductBrandServiceImpl implements ProductBrandService {

    private final ProductBrandRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductBrandResponse> list(ProductBrandFilterRequest filter) {
        List<Specification<ProductBrand>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<ProductBrand> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductBrand> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public ProductBrandResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductBrandResponse create(ProductBrandRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Brand name already taken: " + request.getName());
        }
        ProductBrand brand = ProductBrand.builder()
                .name(request.getName())
                .active(request.isActive())
                .build();
        repository.save(brand);
        return toResponse(brand);
    }

    @Override
    @Transactional
    public ProductBrandResponse update(Long id, ProductBrandRequest request) {
        ProductBrand brand = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Brand name already taken: " + request.getName());
        }
        brand.setName(request.getName());
        brand.setActive(request.isActive());
        repository.save(brand);
        return toResponse(brand);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private ProductBrand find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Brand not found with id: " + id));
    }

    private ProductBrandResponse toResponse(ProductBrand brand) {
        return ProductBrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .active(brand.isActive())
                .build();
    }
}
