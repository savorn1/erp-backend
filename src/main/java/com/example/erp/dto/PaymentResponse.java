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
public class PaymentResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private String paymentNumber;
    private LocalDate paymentDate;
    private String type;
    private String method;
    private BigDecimal amount;
    private String reference;
    private String notes;
    private String createdBy;
    private Long relatedPaymentId;
    private String relatedPaymentNumber;
    // How much of this payment has since been reversed by refunds — only
    // meaningful when type=PAYMENT.
    private BigDecimal refundedAmount;
    private List<PaymentAllocationResponse> allocations;
}
