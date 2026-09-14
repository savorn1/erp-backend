package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierTypeFilterRequest;
import com.example.erp.dto.SupplierTypeRequest;
import com.example.erp.dto.SupplierTypeResponse;
import com.example.erp.entity.SupplierType;
import com.example.erp.exception.AppException;
import com.example.erp.repository.SupplierTypeRepository;
import com.example.erp.service.SupplierTypeService;
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
public class SupplierTypeServiceImpl implements SupplierTypeService {

    private final SupplierTypeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierTypeResponse> list(SupplierTypeFilterRequest filter) {
        List<Specification<SupplierType>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<SupplierType> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<SupplierType> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public SupplierTypeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public SupplierTypeResponse create(SupplierTypeRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Supplier type name already taken: " + request.getName());
        }
        SupplierType type = SupplierType.builder()
                .name(request.getName())
                .active(request.isActive())
                .build();
        repository.save(type);
        return toResponse(type);
    }

    @Override
    @Transactional
    public SupplierTypeResponse update(Long id, SupplierTypeRequest request) {
        SupplierType type = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Supplier type name already taken: " + request.getName());
        }
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

    private SupplierType find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Supplier type not found with id: " + id));
    }

    private SupplierTypeResponse toResponse(SupplierType type) {
        return SupplierTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .active(type.isActive())
                .build();
    }
}
