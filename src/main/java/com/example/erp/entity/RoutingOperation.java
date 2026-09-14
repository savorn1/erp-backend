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

@Entity
@Table(name = "routing_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "routing_id", nullable = false)
    private Long routingId;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    private String name;

    @Column(name = "work_center_id", nullable = false)
    private Long workCenterId;

    // Optional — a specific machine within the work center, if it matters.
    @Column(name = "machine_id")
    private Long machineId;

    // Standard time to run one batch of the parent BOM through this
    // operation — copied onto each generated WorkOrder for later
    // standard-vs-actual comparison (see ManufacturingReportServiceImpl's
    // Operation Performance report).
    @Column(name = "standard_time_minutes", precision = 19, scale = 2)
    private BigDecimal standardTimeMinutes;
}
