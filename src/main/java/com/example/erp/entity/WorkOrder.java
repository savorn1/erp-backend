package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// One shop-floor operation step of a ManufacturingOrder, generated (one per
// RoutingOperation) from the order's BOM's ACTIVE Routing at order-creation
// time — see ManufacturingOrderServiceImpl.generateWorkOrders. Deliberately
// additive/tracking-only: starting or completing a WorkOrder does NOT itself
// move stock — the ManufacturingOrder's own start/complete/quality-check
// actions remain the sole source of material consumption and finished-goods
// receipt (built and tested earlier). This lets shop-floor progress be
// tracked per operation without touching that already-working pipeline.
@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturing_order_id", nullable = false)
    private Long manufacturingOrderId;

    // The RoutingOperation this was generated from — kept for traceability,
    // not re-read live (name/workCenterId/etc. are snapshotted below so a
    // later routing edit doesn't retroactively change history).
    @Column(name = "routing_operation_id")
    private Long routingOperationId;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    private String name;

    @Column(name = "work_center_id", nullable = false)
    private Long workCenterId;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "standard_time_minutes", precision = 19, scale = 2)
    private BigDecimal standardTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WorkOrderStatus status = WorkOrderStatus.PENDING;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(columnDefinition = "text")
    private String notes;
}
