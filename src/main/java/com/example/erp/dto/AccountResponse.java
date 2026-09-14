package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String accountCode;
    private String name;
    private String accountType;
    private Long parentAccountId;
    private String parentAccountCode;
    private String parentAccountName;
    private String description;
    private boolean active;
    // True when at least one other account points to this one as its parent
    // — the frontend's tree renders such an account as a group header.
    private boolean hasChildren;
}
