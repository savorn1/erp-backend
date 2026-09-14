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

// Line items live in the separate StockCountLine entity/table, looked up by
// stockCountId. DRAFT -> COMPLETED (all lines counted) -> RECONCILED (any
// variance turned into a StockAdjustment via StockCountServiceImpl, which
// then goes through the normal Step 13 approval flow — this entity never
// touches StockLevel directly).
@Entity
@Table(name = "stock_counts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String countNumber;

    @Column(nullable = false)
    private LocalDate countDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockCountStatus status = StockCountStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String countedBy;

    // Set once reconciliation produces a StockAdjustment (only when at least
    // one line has a non-zero variance).
    @Column(name = "adjustment_id")
    private Long adjustmentId;
}
