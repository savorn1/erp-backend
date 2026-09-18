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
public class CommissionReportRowResponse {

    private Long salesRepUserId;
    private String salesRepName;
    private long entryCount;
    // Sum of the paid invoice amounts this rep's commission was computed on.
    private BigDecimal totalBasisAmount;
    private BigDecimal totalCommissionAmount;
    private BigDecimal paidCommissionAmount;
    private BigDecimal unpaidCommissionAmount;
}
