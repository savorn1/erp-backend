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

// A request for quotation sent to one or more suppliers for the same set of
// products (see RfqLine), inviting each of them to quote a price (see
// RfqSupplier/RfqQuotationLine). Selecting a winning supplier drafts a
// PurchaseOrder from their quoted lines (see RfqServiceImpl.selectSupplier).
// No JPA relationship mapping — plain FK columns, matching this codebase's
// existing convention.
@Entity
@Table(name = "rfqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rfq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // Destination warehouse for the eventual purchase order — carried on the
    // Rfq itself since PurchaseOrder.warehouseId is required.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    // Optional traceability back to the internal ask this Rfq was raised
    // from — set client-side when the RFQ form is pre-filled from an
    // approved PurchaseRequest; not enforced server-side beyond existing.
    @Column(name = "purchase_request_id")
    private Long purchaseRequestId;

    // Human-readable reference (e.g. "RFQ-000001") — assigned right after the
    // first save, once the generated id is known (see RfqServiceImpl).
    @Column(unique = true)
    private String rfqNumber;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RfqStatus status = RfqStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    // Set once a supplier is selected (see RfqServiceImpl.selectSupplier).
    @Column(name = "awarded_supplier_id")
    private Long awardedSupplierId;

    @Column(name = "awarded_purchase_order_id")
    private Long awardedPurchaseOrderId;

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
    private RfqStatus cancelledFromStatus;
}
