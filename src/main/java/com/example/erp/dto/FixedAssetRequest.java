package com.example.erp.dto;

import com.example.erp.entity.DepreciationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FixedAssetRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String assetCode;

    @NotBlank
    private String name;

    private String description;
    private String category;

    @NotNull
    private LocalDate acquisitionDate;

    @NotNull
    private BigDecimal acquisitionCost;

    private BigDecimal salvageValue = BigDecimal.ZERO;

    @NotNull
    private Integer usefulLifeMonths;

    @NotNull
    private DepreciationMethod depreciationMethod;

    // Required only when depreciationMethod is DECLINING_BALANCE.
    private BigDecimal decliningBalanceRate;
}
