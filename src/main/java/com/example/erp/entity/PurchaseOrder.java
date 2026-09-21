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

// Line items live in the separate PurchaseOrderLine entity/table, looked up
// by purchaseOrderId — no JPA relationship mapping, matching this codebase's
// existing convention of plain FK columns everywhere (see RefreshToken.userId,
// Branch.companyId): the service layer assembles header + lines explicitly.
@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    // Destination warehouse — where goods receipts against this PO increase stock.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    // Human-readable reference (e.g. "PO-000001") — assigned right after the
    // first save, once the generated id is known (see PurchaseOrderServiceImpl).
    @Column(unique = true)
    private String poNumber;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    // The status this document held when it was cancelled or rejected.
    // Overwriting `status` destroys the only record of how far the workflow
    // actually got, which is what decides whether anything has to be unwound —
    // reserved stock, a posted receipt — so it is captured on the way past.
    //
    // Nullable, and not only for ddl-auto=update: rows cancelled before this
    // existed genuinely have nothing to report, and the UI shows no progress
    // rather than inventing some.
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_from_status")
    private PurchaseOrderStatus cancelledFromStatus;
}
