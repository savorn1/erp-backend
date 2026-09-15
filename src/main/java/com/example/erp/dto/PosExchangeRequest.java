package com.example.erp.dto;

import com.example.erp.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PosExchangeRequest {

    @NotNull
    private Long posSessionId;

    @NotNull
    private Long originalPosSaleId;

    @NotEmpty
    @Valid
    private List<PosExchangeReturnLineRequest> returnLines;

    // New items taken instead — may be empty for a pure partial return.
    @Valid
    private List<PosCheckoutLineRequest> newLines = List.of();

    // Required only when the return/new-item values don't net to zero.
    private PaymentMethod settlementMethod;

    private String settlementReference;

    // Only meaningful when settlementMethod is CASH and the customer owes —
    // defaults to the exact amount due (no change) when omitted.
    private BigDecimal cashTendered;
}
