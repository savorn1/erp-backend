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

// Posting a receipt is itself immutable — its header/line fields are never
// edited — but each line's quality check (see GoodsReceiptLine) still needs
// to move the stock increase + PurchaseOrderLine.quantityReceived update that
// used to happen at posting time, hence `status` here tracking whether every
// line has been inspected yet.

@Entity
@Table(name = "goods_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    // Copied from the PO at posting time so this record stays a stable
    // historical fact even if the PO's own warehouse were ever to change.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(unique = true)
    private String receiptNumber;

    @Column(nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GoodsReceiptStatus status = GoodsReceiptStatus.PENDING_QC;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
