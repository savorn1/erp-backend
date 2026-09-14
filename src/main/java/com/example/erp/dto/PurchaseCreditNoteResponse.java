package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseCreditNoteResponse {

    private Long id;
    private Long companyId;
    private Long purchaseInvoiceId;
    private String invoiceNumber;
    private Long supplierId;
    private String supplierName;
    private String creditNoteNumber;
    private LocalDate creditNoteDate;
    private String reason;
    private BigDecimal amount;
    private String createdBy;
}
