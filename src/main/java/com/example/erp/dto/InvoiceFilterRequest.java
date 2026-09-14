package com.example.erp.dto;

import com.example.erp.entity.InvoiceStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class InvoiceFilterRequest {

    private String invoiceNumber;
    private Long companyId;
    private Long customerId;
    private Long salesOrderId;
    private Long deliveryId;
    private InvoiceStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
