package com.example.erp.service.impl;

import com.example.erp.dto.CompleteWorkOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WorkOrderFilterRequest;
import com.example.erp.dto.WorkOrderResponse;
import com.example.erp.entity.Machine;
import com.example.erp.entity.ManufacturingOrder;
import com.example.erp.entity.WorkCenter;
import com.example.erp.entity.WorkOrder;
import com.example.erp.entity.WorkOrderStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.MachineRepository;
import com.example.erp.repository.ManufacturingOrderRepository;
import com.example.erp.repository.WorkCenterRepository;
import com.example.erp.repository.WorkOrderRepository;
import com.example.erp.service.WorkOrderService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final WorkCenterRepository workCenterRepository;
    private final MachineRepository machineRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> listWorkOrders(WorkOrderFilterRequest filter) {
        List<Specification<WorkOrder>> conditions = new ArrayList<>();
        if (filter.getManufacturingOrderId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("manufacturingOrderId"), filter.getManufacturingOrderId()));
        }
        if (filter.getWorkCenterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("workCenterId"), filter.getWorkCenterId()));
        if (filter.getMachineId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("machineId"), filter.getMachineId()));
        if (filter.getStatus() != null) conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        if (filter.getDateFrom() != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("actualStartDate"), filter.getDateFrom().atStartOfDay()));
        }
        if (filter.getDateTo() != null) {
            conditions.add((root, query, cb) -> cb.lessThan(root.get("actualStartDate"), filter.getDateTo().plusDays(1).atStartOfDay()));
        }
        Specification<WorkOrder> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<WorkOrder> page = workOrderRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrder(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public WorkOrderResponse start(Long id) {
        WorkOrder workOrder = find(id);
        if (workOrder.getStatus() != WorkOrderStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a pending work order can be started");
        }
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setActualStartDate(LocalDateTime.now());
        return toResponse(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional
    public WorkOrderResponse complete(Long id, CompleteWorkOrderRequest request) {
        WorkOrder workOrder = find(id);
        if (workOrder.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only an in-progress work order can be completed");
        }
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setActualEndDate(LocalDateTime.now());
        workOrder.setNotes(request.getNotes());
        return toResponse(workOrderRepository.save(workOrder));
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        ManufacturingOrder mo = manufacturingOrderRepository.findById(workOrder.getManufacturingOrderId()).orElse(null);
        WorkCenter workCenter = workCenterRepository.findById(workOrder.getWorkCenterId()).orElse(null);
        Machine machine = workOrder.getMachineId() == null ? null : machineRepository.findById(workOrder.getMachineId()).orElse(null);
        return WorkOrderResponse.builder()
                .id(workOrder.getId())
                .manufacturingOrderId(workOrder.getManufacturingOrderId())
                .moNumber(mo == null ? null : mo.getMoNumber())
                .sequenceNumber(workOrder.getSequenceNumber())
                .name(workOrder.getName())
                .workCenterId(workOrder.getWorkCenterId())
                .workCenterName(workCenter == null ? null : workCenter.getName())
                .machineId(workOrder.getMachineId())
                .machineName(machine == null ? null : machine.getName())
                .standardTimeMinutes(workOrder.getStandardTimeMinutes())
                .status(workOrder.getStatus().name())
                .actualStartDate(workOrder.getActualStartDate())
                .actualEndDate(workOrder.getActualEndDate())
                .notes(workOrder.getNotes())
                .build();
    }

    private WorkOrder find(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Work order not found with id: " + id));
    }
}
