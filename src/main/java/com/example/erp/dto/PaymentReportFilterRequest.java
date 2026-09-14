package com.example.erp.dto;

import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PaymentType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

// Reused loosely across every PaymentReportController endpoint even though
// each report only reads a subset of these fields — same precedent as
// SalesReportFilterRequest.
@Data
@ParameterObject
public class PaymentReportFilterRequest {

    private Long companyId;
    private Long customerId;
    private Long supplierId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private PaymentMethod method;
    private PaymentType type;
    // "CUSTOMER" or "SUPPLIER" — null means both ledgers.
    private String party;
}
