package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseInvoiceResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long supplierId;
    private String supplierName;
    private Long purchaseOrderId;
    private String poNumber;
    private Long goodsReceiptId;
    private String receiptNumber;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    // Sum of purchase credit notes issued against this invoice.
    private BigDecimal creditedAmount;
    // Net of supplier payment allocations (payments minus any refunds against them).
    private BigDecimal paidAmount;
    // totalAmount - creditedAmount - paidAmount.
    private BigDecimal outstandingAmount;
    // UNPAID / PARTIALLY_PAID / PAID — derived from outstandingAmount, not stored.
    private String paymentStatus;
    // True once dueDate has passed and outstandingAmount is still > 0.
    private boolean overdue;
    // Days past dueDate (0 when not overdue or dueDate is unset).
    private int daysOverdue;
    private List<PurchaseInvoiceLineResponse> lines;
}
