package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ProductTypeFilterRequest;
import com.example.erp.dto.ProductTypeRequest;
import com.example.erp.dto.ProductTypeResponse;
import com.example.erp.entity.ProductType;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ProductTypeRepository;
import com.example.erp.service.ProductTypeService;
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
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductTypeResponse> list(ProductTypeFilterRequest filter) {
        List<Specification<ProductType>> conditions = new ArrayList<>();
        if (filter.getCode() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("code"), filter.getCode()));
        }
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<ProductType> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ProductType> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public ProductTypeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductTypeResponse create(ProductTypeRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Type name already taken: " + request.getName());
        }
        // No uniqueness check on the code: several types can share a kind
        // ("Spare parts" and "Packaging" are both CONSUMABLE). The name is the
        // unique handle; the code only says how the system should treat the type.
        ProductType type = ProductType.builder()
                .code(request.getCode())
                .name(request.getName())
                .active(request.isActive())
                .build();
        repository.save(type);
        return toResponse(type);
    }

    @Override
    @Transactional
    public ProductTypeResponse update(Long id, ProductTypeRequest request) {
        ProductType type = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Type name already taken: " + request.getName());
        }
        type.setCode(request.getCode());
        type.setName(request.getName());
        type.setActive(request.isActive());
        repository.save(type);
        return toResponse(type);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private ProductType find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Type not found with id: " + id));
    }

    private ProductTypeResponse toResponse(ProductType type) {
        return ProductTypeResponse.builder()
                .id(type.getId())
                .code(type.getCode() == null ? null : type.getCode().name())
                .name(type.getName())
                .active(type.isActive())
                .build();
    }
}
