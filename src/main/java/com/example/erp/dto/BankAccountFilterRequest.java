package com.example.erp.dto;

import com.example.erp.entity.BankAccountType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class BankAccountFilterRequest {

    private String search;
    private Long companyId;
    private BankAccountType type;
    private Boolean active;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
