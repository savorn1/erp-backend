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

// A supplier bill — generated either from a PurchaseOrder (bills the full
// ordered quantity) or from a GoodsReceipt (bills only what was actually
// received and passed quality check), never created with manual line items —
// see PurchaseInvoiceServiceImpl.createFromPurchaseOrder/createFromGoodsReceipt.
// Approving charges the supplier balance via SupplierService.adjustBalance;
// cancelling an approved invoice reverses that charge. A PurchaseCreditNote
// against an approved invoice applies the opposite (a payment-like credit).
@Entity
@Table(name = "purchase_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    // Set only when generated from a specific receipt (3-way match) rather
    // than the whole PO.
    @Column(name = "goods_receipt_id")
    private Long goodsReceiptId;

    @Column(unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseInvoiceStatus status = PurchaseInvoiceStatus.DRAFT;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
