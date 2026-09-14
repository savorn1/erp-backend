package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocationResponse {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    // Signed — positive for a payment, negative for a refund.
    private BigDecimal amount;
}
