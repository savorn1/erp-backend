package com.example.erp.dto;

import com.example.erp.entity.AccountType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class AccountFilterRequest {

    // Matches accountCode or name.
    private String search;
    private Long companyId;
    private AccountType accountType;
    private Long parentAccountId;
    private Boolean active;

    private String sortBy = "accountCode";
    private String sortOrder = "asc";
    private int page = 1;
    private int size = 10;
}
