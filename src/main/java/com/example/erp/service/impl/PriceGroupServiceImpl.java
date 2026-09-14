package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PriceGroupFilterRequest;
import com.example.erp.dto.PriceGroupRequest;
import com.example.erp.dto.PriceGroupResponse;
import com.example.erp.entity.PriceGroup;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.service.PriceGroupService;
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
public class PriceGroupServiceImpl implements PriceGroupService {

    private final PriceGroupRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PriceGroupResponse> list(PriceGroupFilterRequest filter) {
        List<Specification<PriceGroup>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<PriceGroup> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PriceGroup> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public PriceGroupResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PriceGroupResponse create(PriceGroupRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Price group name already taken: " + request.getName());
        }
        PriceGroup group = PriceGroup.builder()
                .name(request.getName())
                .discountPercent(request.getDiscountPercent())
                .active(request.isActive())
                .build();
        repository.save(group);
        return toResponse(group);
    }

    @Override
    @Transactional
    public PriceGroupResponse update(Long id, PriceGroupRequest request) {
        PriceGroup group = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Price group name already taken: " + request.getName());
        }
        group.setName(request.getName());
        group.setDiscountPercent(request.getDiscountPercent());
        group.setActive(request.isActive());
        repository.save(group);
        return toResponse(group);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private PriceGroup find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Price group not found with id: " + id));
    }

    private PriceGroupResponse toResponse(PriceGroup group) {
        return PriceGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .discountPercent(group.getDiscountPercent())
                .active(group.isActive())
                .build();
    }
}
