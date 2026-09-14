package com.example.erp.dto;

import com.example.erp.entity.BankTransactionType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class BankTransactionFilterRequest {

    private Long companyId;
    private Long bankAccountId;
    private BankTransactionType type;
    private Boolean reconciled;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
