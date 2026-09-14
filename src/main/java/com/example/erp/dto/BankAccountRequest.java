package com.example.erp.dto;

import com.example.erp.entity.BankAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Used for both create and update. openingBalance only takes effect at
// creation time — see BankAccountServiceImpl.create; updating it afterward
// does not retroactively change currentBalance.
@Data
public class BankAccountRequest {

    @NotNull
    private Long companyId;

    private Long accountId;

    @NotBlank
    private String name;

    @NotNull
    private BankAccountType type;

    private String bankName;

    private String accountNumber;

    @NotBlank
    private String currency = "USD";

    @NotNull
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private boolean active = true;
}
