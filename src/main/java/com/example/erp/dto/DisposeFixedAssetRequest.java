package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DisposeFixedAssetRequest {

    @NotNull
    private LocalDate disposalDate;

    @NotNull
    private BigDecimal proceeds;
}
