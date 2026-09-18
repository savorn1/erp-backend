package com.example.erp.dto;

import com.example.erp.entity.LandedCostAllocationMethod;
import com.example.erp.entity.LandedCostType;
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
public class LandedCostResponse {

    private Long id;
    private Long companyId;
    private Long goodsReceiptId;
    private String goodsReceiptNumber;
    private LandedCostType costType;
    private BigDecimal amount;
    private LandedCostAllocationMethod allocationMethod;
    private LocalDate costDate;
    private String reference;
    private String notes;
    private String createdBy;
    private List<LandedCostAllocationResponse> allocations;
}
