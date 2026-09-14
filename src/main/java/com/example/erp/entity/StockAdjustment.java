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

import java.time.LocalDate;

// Line items live in the separate StockAdjustmentLine entity/table, looked up
// by stockAdjustmentId — same header/lines split as PurchaseOrder/SalesOrder.
// Nothing touches StockLevel until approval — request and approval are
// deliberately separate acts (see StockAdjustmentServiceImpl).
@Entity
@Table(name = "stock_adjustments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String adjustmentNumber;

    @Column(nullable = false)
    private LocalDate adjustmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockAdjustmentStatus status = StockAdjustmentStatus.PENDING;

    @Column(columnDefinition = "text")
    private String notes;

    private String requestedBy;

    private String approvedBy;

    private LocalDate approvalDate;
}
