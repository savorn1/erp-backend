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
public class RefundRowResponse {

    // "CUSTOMER" or "SUPPLIER".
    private String party;
    private Long refundId;
    private String refundNumber;
    private LocalDate refundDate;
    private Long partyId;
    private String partyName;
    private String method;
    private BigDecimal amount;
    private String originalPaymentNumber;
    private String notes;
}
