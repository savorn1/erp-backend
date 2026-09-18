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
import java.time.LocalDate;

// A freight/customs/insurance/other charge allocated across one already-
// COMPLETED GoodsReceipt's lines, bumping each affected Product.costPrice —
// see LandedCostServiceImpl. Inventory-only: it never posts a journal entry,
// since the charge itself is assumed already booked as its own expense
// elsewhere. Create-only, no edit/delete, same "immutable ledger entry"
// reasoning as the GoodsReceipt it's attached to.
@Entity
@Table(name = "landed_costs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandedCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "goods_receipt_id", nullable = false)
    private Long goodsReceiptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", nullable = false)
    private LandedCostType costType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocation_method", nullable = false)
    private LandedCostAllocationMethod allocationMethod;

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    private String reference;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
