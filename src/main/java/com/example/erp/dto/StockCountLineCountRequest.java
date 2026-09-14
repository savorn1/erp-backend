package com.example.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockCountLineCountRequest {

    @NotNull
    private Long lineId;

    @NotNull
    @DecimalMin(value = "0", message = "Counted quantity cannot be negative")
    private BigDecimal countedQuantity;
}
