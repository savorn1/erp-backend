package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OpenPosSessionRequest {

    @NotNull
    private Long registerId;

    @NotNull
    private BigDecimal openingFloat;
}
