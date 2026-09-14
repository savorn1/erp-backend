package com.example.erp.service.impl;

import com.example.erp.dto.CustomerTypeFilterRequest;
import com.example.erp.dto.CustomerTypeRequest;
import com.example.erp.dto.CustomerTypeResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.CustomerType;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CustomerTypeRepository;
import com.example.erp.service.CustomerTypeService;
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
public class CustomerTypeServiceImpl implements CustomerTypeService {

    private final CustomerTypeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerTypeResponse> list(CustomerTypeFilterRequest filter) {
        List<Specification<CustomerType>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<CustomerType> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CustomerType> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public CustomerTypeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public CustomerTypeResponse create(CustomerTypeRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Customer type name already taken: " + request.getName());
        }
        CustomerType type = CustomerType.builder()
                .name(request.getName())
                .active(request.isActive())
                .build();
        repository.save(type);
        return toResponse(type);
    }

    @Override
    @Transactional
    public CustomerTypeResponse update(Long id, CustomerTypeRequest request) {
        CustomerType type = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Customer type name already taken: " + request.getName());
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

    private CustomerType find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Customer type not found with id: " + id));
    }

    private CustomerTypeResponse toResponse(CustomerType type) {
        return CustomerTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .active(type.isActive())
                .build();
    }
}
