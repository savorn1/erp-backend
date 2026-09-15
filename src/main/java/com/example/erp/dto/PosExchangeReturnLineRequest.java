package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosExchangeReturnLineRequest {

    // The original PosSaleLine being (partially) returned.
    @NotNull
    private Long posSaleLineId;

    @NotNull
    private BigDecimal quantity;
}
