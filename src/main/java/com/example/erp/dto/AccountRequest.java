package com.example.erp.dto;

import com.example.erp.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Used for both create and update — this lookup's shape doesn't diverge
// between the two, matching ProductCategoryRequest's own convention.
@Data
public class AccountRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String accountCode;

    @NotBlank
    private String name;

    @NotNull
    private AccountType accountType;

    // Null for a top-level account.
    private Long parentAccountId;

    private String description;

    private boolean active = true;
}
