package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosCheckoutLineRequest {

    @NotNull
    private Long productId;

    @NotNull
    private BigDecimal quantity;

    // Defaults to 0 — an optional per-line discount the cashier applies.
    private BigDecimal discountPercent = BigDecimal.ZERO;
}
