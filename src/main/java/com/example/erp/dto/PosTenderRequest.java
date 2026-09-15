package com.example.erp.dto;

import com.example.erp.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosTenderRequest {

    @NotNull
    private PaymentMethod method;

    // For CASH this may be more than the amount actually needed — the
    // excess is returned to the customer as change, and only the applied
    // portion is persisted on the PosPaymentLine (see PosSaleServiceImpl).
    @NotNull
    private BigDecimal amount;

    private String reference;
}
