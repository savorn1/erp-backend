package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PosCheckoutRequest {

    @NotNull
    private Long posSessionId;

    // Null = walk-in — resolves to the company's synthetic Walk-in Customer.
    private Long customerId;

    @NotEmpty
    @Valid
    private List<PosCheckoutLineRequest> lines;

    @NotEmpty
    @Valid
    private List<PosTenderRequest> tenders;
}
