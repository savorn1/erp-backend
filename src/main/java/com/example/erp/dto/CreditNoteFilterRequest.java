package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class CreditNoteFilterRequest {

    private String creditNoteNumber;
    private Long companyId;
    private Long invoiceId;
    private Long customerId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
