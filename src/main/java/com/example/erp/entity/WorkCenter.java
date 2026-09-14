package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// A physical/logical production area (e.g. "Mixing Station", "Oven Line 1")
// that a RoutingOperation is assigned to. Plain master data, same shape as
// Warehouse — no capacity/scheduling engine reads capacityPerHour, it's only
// used to compute a rough utilization percent in ManufacturingReportService.
@Entity
@Table(name = "work_centers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    private String code;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    private String description;

    // Nominal throughput, e.g. units/hour — optional, only used to compute a
    // rough utilization percent when present.
    @Column(name = "capacity_per_hour", precision = 19, scale = 4)
    private BigDecimal capacityPerHour;

    @Builder.Default
    private boolean active = true;
}
