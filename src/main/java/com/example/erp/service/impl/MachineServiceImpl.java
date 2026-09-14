package com.example.erp.service.impl;

import com.example.erp.dto.CreateMachineRequest;
import com.example.erp.dto.MachineFilterRequest;
import com.example.erp.dto.MachineResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateMachineRequest;
import com.example.erp.dto.UpdateMachineStatusRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Machine;
import com.example.erp.entity.WorkCenter;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.RoutingOperationRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.service.MachineService;
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
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;
    private final CompanyRepository companyRepository;
    private final WorkCenterRepository workCenterRepository;
    private final RoutingOperationRepository routingOperationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MachineResponse> listMachines(MachineFilterRequest filter) {
        List<Specification<Machine>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getWorkCenterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("workCenterId"), filter.getWorkCenterId()));
        if (filter.getStatus() != null) conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        Specification<Machine> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Machine> page = machineRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public MachineResponse getMachine(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public MachineResponse createMachine(CreateMachineRequest request) {
        requireCompany(request.getCompanyId());
        requireWorkCenter(request.getWorkCenterId(), request.getCompanyId());
        Machine machine = Machine.builder()
                .companyId(request.getCompanyId())
                .workCenterId(request.getWorkCenterId())
                .name(request.getName())
                .code(request.getCode())
                .costPerHour(request.getCostPerHour())
                .build();
        return toResponse(machineRepository.save(machine));
    }

    @Override
    @Transactional
    public MachineResponse updateMachine(Long id, UpdateMachineRequest request) {
        Machine machine = find(id);
        requireWorkCenter(request.getWorkCenterId(), machine.getCompanyId());
        machine.setWorkCenterId(request.getWorkCenterId());
        machine.setName(request.getName());
        machine.setCode(request.getCode());
        machine.setCostPerHour(request.getCostPerHour());
        return toResponse(machineRepository.save(machine));
    }

    @Override
    @Transactional
    public MachineResponse updateStatus(Long id, UpdateMachineStatusRequest request) {
        Machine machine = find(id);
        machine.setStatus(request.getStatus());
        return toResponse(machineRepository.save(machine));
    }

    @Override
    @Transactional
    public void deleteMachine(Long id) {
        Machine machine = find(id);
        if (routingOperationRepository.existsByMachineId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a machine used by a routing operation");
        }
        machineRepository.delete(machine);
    }

    private MachineResponse toResponse(Machine machine) {
        Company company = companyRepository.findById(machine.getCompanyId()).orElse(null);
        WorkCenter workCenter = workCenterRepository.findById(machine.getWorkCenterId()).orElse(null);
        return MachineResponse.builder()
                .id(machine.getId())
                .companyId(machine.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .workCenterId(machine.getWorkCenterId())
                .workCenterName(workCenter == null ? null : workCenter.getName())
                .name(machine.getName())
                .code(machine.getCode())
                .status(machine.getStatus().name())
                .costPerHour(machine.getCostPerHour())
                .build();
    }

    private Machine find(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Machine not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireWorkCenter(Long workCenterId, Long companyId) {
        WorkCenter workCenter = workCenterRepository.findById(workCenterId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Work center not found with id: " + workCenterId));
        if (!workCenter.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Work center does not belong to the selected company");
        }
    }
}
