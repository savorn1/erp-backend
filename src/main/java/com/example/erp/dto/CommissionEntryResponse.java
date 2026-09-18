package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionEntryResponse {

    private Long id;
    private Long salesRepUserId;
    private String salesRepName;
    private Long invoiceId;
    private String invoiceNumber;
    private Long paymentId;
    private String paymentNumber;
    private BigDecimal basisAmount;
    private BigDecimal ratePercent;
    private BigDecimal commissionAmount;
    private LocalDate earnedDate;
    private boolean paidOut;
    private LocalDateTime paidOutAt;
}
