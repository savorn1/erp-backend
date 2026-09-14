package com.example.erp.service.impl;

import com.example.erp.dto.CreateRoutingRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RoutingFilterRequest;
import com.example.erp.dto.RoutingOperationRequest;
import com.example.erp.dto.RoutingOperationResponse;
import com.example.erp.dto.RoutingResponse;
import com.example.erp.dto.UpdateRoutingRequest;
import com.example.erp.entity.BillOfMaterial;
import com.example.erp.entity.Company;
import com.example.erp.entity.Machine;
import com.example.erp.entity.Routing;
import com.example.erp.entity.RoutingOperation;
import com.example.erp.entity.RoutingStatus;
import com.example.erp.entity.WorkCenter;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BillOfMaterialRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.RoutingOperationRepository;
import com.example.erp.repository.RoutingRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.repository.WorkOrderRepository;
import com.example.erp.service.RoutingService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final RoutingRepository routingRepository;
    private final RoutingOperationRepository operationRepository;
    private final CompanyRepository companyRepository;
    private final BillOfMaterialRepository bomRepository;
    private final WorkCenterRepository workCenterRepository;
    private final MachineRepository machineRepository;
    private final WorkOrderRepository workOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoutingResponse> listRoutings(RoutingFilterRequest filter) {
        List<Specification<Routing>> conditions = new ArrayList<>();
        if (filter.getRoutingNumber() != null && !filter.getRoutingNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("routingNumber")), "%" + filter.getRoutingNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getBomId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("bomId"), filter.getBomId()));
        if (filter.getStatus() != null) conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        Specification<Routing> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Routing> page = routingRepository.findAll(spec, pageable);
        List<Routing> content = page.getContent();
        List<Long> routingIds = content.stream().map(Routing::getId).toList();
        Map<Long, List<RoutingOperation>> opsByRoutingId = routingIds.isEmpty() ? Map.of() : allOperationsGroupedByRouting(routingIds);

        return PageResponse.of(page.map(r -> toFullResponse(r, opsByRoutingId.getOrDefault(r.getId(), List.of()))));
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingResponse getRouting(Long id) {
        Routing routing = find(id);
        return toFullResponse(routing, operationRepository.findByRoutingIdOrderBySequenceNumberAsc(id));
    }

    @Override
    @Transactional
    public RoutingResponse createRouting(CreateRoutingRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        BillOfMaterial bom = requireBom(request.getBomId(), request.getCompanyId());
        validateOperations(request.getOperations(), request.getCompanyId());

        Routing routing = Routing.builder()
                .companyId(request.getCompanyId())
                .bomId(bom.getId())
                .name(request.getName())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        routingRepository.save(routing);
        routing.setRoutingNumber("RT-" + String.format("%06d", routing.getId()));
        routingRepository.save(routing);

        List<RoutingOperation> operations = saveOperations(routing.getId(), request.getOperations());
        return toFullResponse(routing, operations);
    }

    @Override
    @Transactional
    public RoutingResponse updateRouting(Long id, UpdateRoutingRequest request) {
        Routing routing = find(id);
        validateOperations(request.getOperations(), routing.getCompanyId());

        routing.setName(request.getName());
        routing.setNotes(request.getNotes());
        routingRepository.save(routing);

        operationRepository.deleteByRoutingId(id);
        List<RoutingOperation> operations = saveOperations(id, request.getOperations());
        return toFullResponse(routing, operations);
    }

    @Override
    @Transactional
    public RoutingResponse activateRouting(Long id) {
        Routing routing = find(id);
        routing.setStatus(RoutingStatus.ACTIVE);
        routingRepository.save(routing);
        return toFullResponse(routing, operationRepository.findByRoutingIdOrderBySequenceNumberAsc(id));
    }

    @Override
    @Transactional
    public RoutingResponse deactivateRouting(Long id) {
        Routing routing = find(id);
        routing.setStatus(RoutingStatus.INACTIVE);
        routingRepository.save(routing);
        return toFullResponse(routing, operationRepository.findByRoutingIdOrderBySequenceNumberAsc(id));
    }

    @Override
    @Transactional
    public void deleteRouting(Long id) {
        Routing routing = find(id);
        List<Long> operationIds = operationRepository.findByRoutingIdOrderBySequenceNumberAsc(id).stream().map(RoutingOperation::getId).toList();
        if (!operationIds.isEmpty() && workOrderRepository.existsByRoutingOperationIdIn(operationIds)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a routing that already has work orders generated from it — deactivate it instead");
        }
        operationRepository.deleteByRoutingId(id);
        routingRepository.delete(routing);
    }

    private void validateOperations(List<RoutingOperationRequest> operations, Long companyId) {
        Map<Long, WorkCenter> workCenters = workCenterRepository.findAllById(
                operations.stream().map(RoutingOperationRequest::getWorkCenterId).distinct().toList()
        ).stream().collect(Collectors.toMap(WorkCenter::getId, w -> w));
        for (RoutingOperationRequest op : operations) {
            WorkCenter workCenter = workCenters.get(op.getWorkCenterId());
            if (workCenter == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Work center not found with id: " + op.getWorkCenterId());
            }
            if (!workCenter.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Work center does not belong to the selected company: " + workCenter.getName());
            }
            if (op.getMachineId() != null) {
                Machine machine = machineRepository.findById(op.getMachineId())
                        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Machine not found with id: " + op.getMachineId()));
                if (!machine.getWorkCenterId().equals(op.getWorkCenterId())) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Machine does not belong to the selected work center: " + machine.getName());
                }
            }
        }
    }

    private List<RoutingOperation> saveOperations(Long routingId, List<RoutingOperationRequest> requests) {
        List<RoutingOperation> operations = requests.stream()
                .map(r -> RoutingOperation.builder()
                        .routingId(routingId)
                        .sequenceNumber(r.getSequenceNumber())
                        .name(r.getName())
                        .workCenterId(r.getWorkCenterId())
                        .machineId(r.getMachineId())
                        .standardTimeMinutes(r.getStandardTimeMinutes())
                        .build())
                .toList();
        return operationRepository.saveAll(operations);
    }

    private Map<Long, List<RoutingOperation>> allOperationsGroupedByRouting(List<Long> routingIds) {
        List<RoutingOperation> all = new ArrayList<>();
        for (Long routingId : routingIds) {
            all.addAll(operationRepository.findByRoutingIdOrderBySequenceNumberAsc(routingId));
        }
        return all.stream().collect(Collectors.groupingBy(RoutingOperation::getRoutingId));
    }

    private RoutingResponse toFullResponse(Routing routing, List<RoutingOperation> operations) {
        Company company = companyRepository.findById(routing.getCompanyId()).orElse(null);
        BillOfMaterial bom = bomRepository.findById(routing.getBomId()).orElse(null);

        Map<Long, WorkCenter> workCenters = workCenterRepository.findAllById(
                operations.stream().map(RoutingOperation::getWorkCenterId).distinct().toList()
        ).stream().collect(Collectors.toMap(WorkCenter::getId, w -> w));
        Map<Long, Machine> machines = machineRepository.findAllById(
                operations.stream().map(RoutingOperation::getMachineId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Machine::getId, m -> m));

        List<RoutingOperationResponse> operationResponses = operations.stream()
                .map(op -> {
                    WorkCenter workCenter = workCenters.get(op.getWorkCenterId());
                    Machine machine = op.getMachineId() == null ? null : machines.get(op.getMachineId());
                    return RoutingOperationResponse.builder()
                            .id(op.getId())
                            .sequenceNumber(op.getSequenceNumber())
                            .name(op.getName())
                            .workCenterId(op.getWorkCenterId())
                            .workCenterName(workCenter == null ? null : workCenter.getName())
                            .machineId(op.getMachineId())
                            .machineName(machine == null ? null : machine.getName())
                            .standardTimeMinutes(op.getStandardTimeMinutes())
                            .build();
                })
                .toList();

        return RoutingResponse.builder()
                .id(routing.getId())
                .companyId(routing.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .bomId(routing.getBomId())
                .bomNumber(bom == null ? null : bom.getBomNumber())
                .routingNumber(routing.getRoutingNumber())
                .name(routing.getName())
                .status(routing.getStatus().name())
                .notes(routing.getNotes())
                .createdBy(routing.getCreatedBy())
                .createdAt(routing.getCreatedAt())
                .operations(operationResponses)
                .build();
    }

    private Routing find(Long id) {
        return routingRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Routing not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private BillOfMaterial requireBom(Long bomId, Long companyId) {
        BillOfMaterial bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Bill of materials not found with id: " + bomId));
        if (!bom.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Bill of materials does not belong to the selected company");
        }
        return bom;
    }
}
