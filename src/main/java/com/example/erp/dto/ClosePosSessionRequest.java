package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClosePosSessionRequest {

    @NotNull
    private BigDecimal countedCash;
}
