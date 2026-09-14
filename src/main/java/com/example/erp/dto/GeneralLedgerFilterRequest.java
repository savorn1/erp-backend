package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class GeneralLedgerFilterRequest {

    @NotNull
    private Long accountId;

    // Both optional. openingBalance in the response is computed from every
    // POSTED line dated strictly before dateFrom (or from inception if unset).
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
