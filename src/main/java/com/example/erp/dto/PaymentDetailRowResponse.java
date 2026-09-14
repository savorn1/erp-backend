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
public class PaymentDetailRowResponse {

    // "CUSTOMER" or "SUPPLIER".
    private String party;
    private Long paymentId;
    private String paymentNumber;
    private LocalDate paymentDate;
    private Long partyId;
    private String partyName;
    private String type;
    private String method;
    private BigDecimal amount;
    private String reference;
    private String createdBy;
}
