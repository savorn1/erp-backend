package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// Used for both "generate from purchase order" and "generate from goods
// receipt" — lines are copied from the source, not entered manually (see
// PurchaseInvoiceServiceImpl.createFromPurchaseOrder/createFromGoodsReceipt).
@Data
public class CreatePurchaseInvoiceRequest {

    @NotNull
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private String notes;
}
