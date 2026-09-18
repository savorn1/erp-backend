package com.example.erp.dto;

import com.example.erp.entity.PettyCashEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePettyCashEntryRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private PettyCashEntryType type;

    @NotNull
    private LocalDate entryDate;

    // ASSET account for TOPUP (the cash/bank source), EXPENSE account for
    // EXPENSE (what was spent on) — validated in PettyCashServiceImpl.
    @NotNull
    private Long accountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String description;
}
