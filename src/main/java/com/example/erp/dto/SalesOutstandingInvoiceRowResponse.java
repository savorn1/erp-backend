package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOutstandingInvoiceRowResponse {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    // Negative when not yet due.
    private long daysOverdue;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal outstandingAmount;
}
