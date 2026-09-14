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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Material requirement/consumption lines live in the separate
// ManufacturingOrderMaterial entity/table, looked up by
// manufacturingOrderId — no JPA relationship mapping, matching this
// codebase's existing convention of plain FK columns everywhere.
@Entity
@Table(name = "manufacturing_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    // Copied from the BOM at creation time — the finished good this order
    // produces.
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Where materials are drawn from and finished goods are received into.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    // Optional grouping under a ProductionPlan — see that entity's comment.
    @Column(name = "production_plan_id")
    private Long productionPlanId;

    @Column(name = "mo_number", unique = true)
    private String moNumber;

    @Column(name = "planned_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal plannedQuantity;

    @Column(name = "produced_quantity", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    // Finished-good units produced but not usable — never posted to stock,
    // see StockMovementType.PRODUCTION_OUTPUT's own comment.
    @Column(name = "scrap_quantity", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal scrapQuantity = BigDecimal.ZERO;

    @Column(name = "scrap_reason", columnDefinition = "text")
    private String scrapReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ManufacturingOrderStatus status = ManufacturingOrderStatus.DRAFT;

    // Set when the order reaches PENDING_QC/COMPLETED — see
    // ManufacturingOrderServiceImpl.recordQualityCheck. Reuses
    // QualityCheckStatus (already used by GoodsReceiptLine) rather than a
    // new enum.
    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status")
    @Builder.Default
    private QualityCheckStatus qualityStatus = QualityCheckStatus.PENDING;

    @Column(name = "quality_notes", columnDefinition = "text")
    private String qualityNotes;

    @Column(name = "quality_checked_by")
    private String qualityCheckedBy;

    @Column(name = "quality_checked_at")
    private LocalDateTime qualityCheckedAt;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    // Sum of consumed material lines' lineCost, stamped when production
    // starts (see ManufacturingOrderServiceImpl.startProduction).
    @Column(name = "material_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal materialCost = BigDecimal.ZERO;

    // Manually entered when production completes — this codebase has no
    // labor/timesheet or overhead-allocation module to derive these from.
    @Column(name = "labor_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal laborCost = BigDecimal.ZERO;

    @Column(name = "overhead_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal overheadCost = BigDecimal.ZERO;

    // materialCost + laborCost + overheadCost, stamped on completion.
    @Column(name = "total_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    // totalCost / producedQuantity, stamped on completion (zero if nothing
    // was produced).
    @Column(name = "unit_cost", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
