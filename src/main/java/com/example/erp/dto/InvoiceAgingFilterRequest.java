package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class InvoiceAgingFilterRequest {

    private Long companyId;
    private Long customerId;
    // Defaults to today when omitted.
    private LocalDate asOfDate;
}
