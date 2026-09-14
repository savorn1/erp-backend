package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// The payment ledgers (Payment/SupplierPayment) and the bank ledger
// (BankTransaction) aren't linked row-by-row in this codebase, so this
// reconciles at the totals level — bank-method receipts vs. deposits,
// bank-method supplier payments vs. withdrawals — plus each account's own
// reconciled/unreconciled transaction status.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReconciliationResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<PaymentReconciliationRowResponse> rows;
    // Net customer payments with method BANK_TRANSFER or PAYMENT_GATEWAY.
    private BigDecimal customerBankReceipts;
    // DEPOSIT transactions only (internal transfers excluded).
    private BigDecimal totalDeposits;
    // totalDeposits - customerBankReceipts.
    private BigDecimal receiptsVariance;
    private BigDecimal supplierBankPayments;
    // WITHDRAWAL transactions only.
    private BigDecimal totalWithdrawals;
    // totalWithdrawals - supplierBankPayments.
    private BigDecimal paymentsVariance;
}
