package com.example.erp.service.impl;

import com.example.erp.dto.CreateWorkCenterRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateWorkCenterRequest;
import com.example.erp.dto.UpdateWorkCenterStatusRequest;
import com.example.erp.dto.WorkCenterFilterRequest;
import com.example.erp.dto.WorkCenterResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.Warehouse;
import com.example.erp.entity.WorkCenter;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.RoutingOperationRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.service.WorkCenterService;
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
public class WorkCenterServiceImpl implements WorkCenterService {

    private final WorkCenterRepository workCenterRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final MachineRepository machineRepository;
    private final RoutingOperationRepository routingOperationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkCenterResponse> listWorkCenters(WorkCenterFilterRequest filter) {
        List<Specification<WorkCenter>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getActive() != null) conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        Specification<WorkCenter> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<WorkCenter> page = workCenterRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkCenterResponse getWorkCenter(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public WorkCenterResponse createWorkCenter(CreateWorkCenterRequest request) {
        requireCompany(request.getCompanyId());
        WorkCenter workCenter = WorkCenter.builder()
                .companyId(request.getCompanyId())
                .name(request.getName())
                .code(request.getCode())
                .warehouseId(request.getWarehouseId())
                .description(request.getDescription())
                .capacityPerHour(request.getCapacityPerHour())
                .build();
        return toResponse(workCenterRepository.save(workCenter));
    }

    @Override
    @Transactional
    public WorkCenterResponse updateWorkCenter(Long id, UpdateWorkCenterRequest request) {
        WorkCenter workCenter = find(id);
        workCenter.setName(request.getName());
        workCenter.setCode(request.getCode());
        workCenter.setWarehouseId(request.getWarehouseId());
        workCenter.setDescription(request.getDescription());
        workCenter.setCapacityPerHour(request.getCapacityPerHour());
        return toResponse(workCenterRepository.save(workCenter));
    }

    @Override
    @Transactional
    public WorkCenterResponse updateStatus(Long id, UpdateWorkCenterStatusRequest request) {
        WorkCenter workCenter = find(id);
        workCenter.setActive(request.getActive());
        return toResponse(workCenterRepository.save(workCenter));
    }

    @Override
    @Transactional
    public void deleteWorkCenter(Long id) {
        WorkCenter workCenter = find(id);
        if (machineRepository.exists((root, query, cb) -> cb.equal(root.get("workCenterId"), id))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a work center that has machines assigned to it");
        }
        if (routingOperationRepository.existsByWorkCenterId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a work center used by a routing operation");
        }
        workCenterRepository.delete(workCenter);
    }

    private WorkCenterResponse toResponse(WorkCenter workCenter) {
        Company company = companyRepository.findById(workCenter.getCompanyId()).orElse(null);
        Warehouse warehouse = workCenter.getWarehouseId() == null ? null : warehouseRepository.findById(workCenter.getWarehouseId()).orElse(null);
        return WorkCenterResponse.builder()
                .id(workCenter.getId())
                .companyId(workCenter.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .name(workCenter.getName())
                .code(workCenter.getCode())
                .warehouseId(workCenter.getWarehouseId())
                .warehouseName(warehouse == null ? null : warehouse.getName())
                .description(workCenter.getDescription())
                .capacityPerHour(workCenter.getCapacityPerHour())
                .active(workCenter.isActive())
                .build();
    }

    private WorkCenter find(Long id) {
        return workCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Work center not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
