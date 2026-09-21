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
public class InvoiceResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private Long salesOrderId;
    private String soNumber;
    private Long deliveryId;
    private String deliveryNumber;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String status;

    // Where the workflow actually got to, for documents that were
    // cancelled or rejected. Null when unknown — including every row
    // cancelled before this was recorded.
    private String cancelledFromStatus;
    private String notes;
    private String createdBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String foreignCurrency;
    private BigDecimal exchangeRate;
    // totalAmount / exchangeRate, only when both foreignCurrency and
    // exchangeRate are set — a display-only convenience, never stored.
    private BigDecimal foreignTotalAmount;
    // Sum of credit notes issued against this invoice.
    private BigDecimal creditedAmount;
    // Net of payment allocations (payments minus any refunds against them).
    private BigDecimal paidAmount;
    // totalAmount - creditedAmount - paidAmount.
    private BigDecimal outstandingAmount;
    // UNPAID / PARTIALLY_PAID / PAID — derived from outstandingAmount, not stored.
    private String paymentStatus;
    // True when dueDate has passed and outstandingAmount is still > 0.
    private boolean overdue;
    // Days past dueDate (0 when not overdue or dueDate is unset).
    private int daysOverdue;
    private List<InvoiceLineResponse> lines;
}
