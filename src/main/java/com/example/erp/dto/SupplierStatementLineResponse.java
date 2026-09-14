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
public class SupplierStatementLineResponse {

    private LocalDate date;
    // "Invoice", "Payment", or "Credit Note".
    private String type;
    private String reference;
    // Same convention as CustomerStatementLineResponse: an invoice increases
    // the balance owed (debit), a payment or credit note decreases it (credit).
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal runningBalance;
}
