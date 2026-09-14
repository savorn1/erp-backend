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
public class PaymentByBranchRowResponse {

    // Null (with branchName "Unassigned") when the recording user has no branch.
    private Long branchId;
    private String branchName;
    private long customerCount;
    private BigDecimal customerNet;
    private long supplierCount;
    private BigDecimal supplierNet;
}
