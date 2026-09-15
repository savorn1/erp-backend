package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PosHoldRequest {

    @NotNull
    private Long posSessionId;

    // Null = walk-in — same convention as PosCheckoutRequest, but left
    // unresolved (no synthetic customer created) until the sale actually
    // checks out.
    private Long customerId;

    private String note;

    @NotEmpty
    @Valid
    private List<PosCheckoutLineRequest> lines;
}
