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
public class CreditNoteResponse {

    private Long id;
    private Long companyId;
    private Long invoiceId;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private String creditNoteNumber;
    private LocalDate creditNoteDate;
    private String reason;
    private BigDecimal amount;
    private String createdBy;
}
