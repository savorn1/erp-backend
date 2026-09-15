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
public class FixedAssetResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String assetCode;
    private String name;
    private String description;
    private String category;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private BigDecimal salvageValue;
    private Integer usefulLifeMonths;
    private String depreciationMethod;
    private BigDecimal decliningBalanceRate;
    private BigDecimal accumulatedDepreciation;
    // acquisitionCost - accumulatedDepreciation.
    private BigDecimal bookValue;
    private String status;
    private LocalDate disposalDate;
    private BigDecimal disposalProceeds;
}
