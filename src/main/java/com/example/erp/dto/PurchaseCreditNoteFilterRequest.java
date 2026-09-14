package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PurchaseCreditNoteFilterRequest {

    private String creditNoteNumber;
    private Long companyId;
    private Long purchaseInvoiceId;
    private Long supplierId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
