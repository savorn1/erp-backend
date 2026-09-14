package com.example.erp.service.impl;

import com.example.erp.dto.CustomerGroupFilterRequest;
import com.example.erp.dto.CustomerGroupRequest;
import com.example.erp.dto.CustomerGroupResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.PriceGroup;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.service.CustomerGroupService;
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
public class CustomerGroupServiceImpl implements CustomerGroupService {

    private final CustomerGroupRepository repository;
    private final PriceGroupRepository priceGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerGroupResponse> list(CustomerGroupFilterRequest filter) {
        List<Specification<CustomerGroup>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<CustomerGroup> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CustomerGroup> page = repository.findAll(spec, pageable);
        List<CustomerGroup> content = page.getContent();
        Map<Long, String> priceGroupNames = priceGroupRepository.findAllById(
                content.stream().map(CustomerGroup::getPriceGroupId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(PriceGroup::getId, PriceGroup::getName));

        return PageResponse.of(page.map(g -> toResponse(g, g.getPriceGroupId() == null ? null : priceGroupNames.get(g.getPriceGroupId()))));
    }

    @Override
    public CustomerGroupResponse get(Long id) {
        CustomerGroup group = find(id);
        return toResponse(group, priceGroupNameOf(group.getPriceGroupId()));
    }

    @Override
    @Transactional
    public CustomerGroupResponse create(CustomerGroupRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Customer group name already taken: " + request.getName());
        }
        if (request.getPriceGroupId() != null) {
            requirePriceGroup(request.getPriceGroupId());
        }
        CustomerGroup group = CustomerGroup.builder()
                .name(request.getName())
                .priceGroupId(request.getPriceGroupId())
                .active(request.isActive())
                .build();
        repository.save(group);
        return toResponse(group, priceGroupNameOf(request.getPriceGroupId()));
    }

    @Override
    @Transactional
    public CustomerGroupResponse update(Long id, CustomerGroupRequest request) {
        CustomerGroup group = find(id);
        if (repository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Customer group name already taken: " + request.getName());
        }
        if (request.getPriceGroupId() != null) {
            requirePriceGroup(request.getPriceGroupId());
        }
        group.setName(request.getName());
        group.setPriceGroupId(request.getPriceGroupId());
        group.setActive(request.isActive());
        repository.save(group);
        return toResponse(group, priceGroupNameOf(request.getPriceGroupId()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private void requirePriceGroup(Long priceGroupId) {
        if (!priceGroupRepository.existsById(priceGroupId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Price group not found with id: " + priceGroupId);
        }
    }

    private String priceGroupNameOf(Long priceGroupId) {
        return priceGroupId == null ? null : priceGroupRepository.findById(priceGroupId).map(PriceGroup::getName).orElse(null);
    }

    private CustomerGroup find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Customer group not found with id: " + id));
    }

    private CustomerGroupResponse toResponse(CustomerGroup group, String priceGroupName) {
        return CustomerGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .priceGroupId(group.getPriceGroupId())
                .priceGroupName(priceGroupName)
                .active(group.isActive())
                .build();
    }
}
