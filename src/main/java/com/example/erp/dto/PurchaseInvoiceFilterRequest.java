package com.example.erp.dto;

import com.example.erp.entity.PurchaseInvoiceStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PurchaseInvoiceFilterRequest {

    private String invoiceNumber;
    private Long companyId;
    private Long supplierId;
    private Long purchaseOrderId;
    private Long goodsReceiptId;
    private PurchaseInvoiceStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
