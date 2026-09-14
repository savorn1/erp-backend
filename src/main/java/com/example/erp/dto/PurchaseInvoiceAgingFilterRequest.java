package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class PurchaseInvoiceAgingFilterRequest {

    private Long companyId;
    private Long supplierId;
    // Defaults to today when omitted.
    private LocalDate asOfDate;
}
