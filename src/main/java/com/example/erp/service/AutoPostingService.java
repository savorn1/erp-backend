package com.example.erp.service;

import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.SupplierPayment;

import java.math.BigDecimal;

// Auto-generates a balanced JournalEntry for a business event, using the
// company's PostingRule account mapping. Every method is a no-op (returns
// normally, posts nothing) when the company has no PostingRule configured or
// the specific accounts a given event needs aren't mapped — auto-posting is
// opt-in, and its absence must never block the underlying business action
// (approving an invoice, recording a payment, etc. always succeeds on its
// own terms regardless of GL setup). Call sites: InvoiceServiceImpl,
// PurchaseInvoiceServiceImpl, PaymentServiceImpl, SupplierPaymentServiceImpl,
// CreditNoteServiceImpl, PurchaseCreditNoteServiceImpl.
public interface AutoPostingService {

    // Dr Accounts Receivable (totalAmount) / Cr Sales Revenue (netAmount) /
    // Cr Tax Payable (taxAmount, only if > 0).
    void postInvoiceApproval(Invoice invoice, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String actingUsername);

    // Dr Purchase Expense (netAmount) / Dr Tax Receivable (taxAmount, only
    // if > 0) / Cr Accounts Payable (totalAmount).
    void postPurchaseInvoiceApproval(PurchaseInvoice invoice, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String actingUsername);

    // PAYMENT: Dr Cash/Bank (by method) / Cr Accounts Receivable.
    // REFUND: Dr Accounts Receivable / Cr Cash/Bank (by method) — reversed.
    void postCustomerPayment(Payment payment, String actingUsername);

    // PAYMENT: Dr Accounts Payable / Cr Cash/Bank (by method).
    // REFUND: Dr Cash/Bank (by method) / Cr Accounts Payable — reversed.
    void postSupplierPayment(SupplierPayment payment, String actingUsername);

    // Dr Sales Returns / Cr Accounts Receivable.
    void postCreditNote(CreditNote creditNote, String actingUsername);

    // Dr Accounts Payable / Cr Purchase Returns.
    void postPurchaseCreditNote(PurchaseCreditNote creditNote, String actingUsername);

    // Finds the auto-posted entry (if any) for this source and reverses it —
    // a POSTED entry gets a swapped-lines reversal (same as
    // JournalEntryServiceImpl.reverse), a DRAFT one (created while its
    // period was locked, never posted) is simply deleted. No-op if no such
    // entry exists or it was already reversed. Called when cancelling an
    // approved Invoice/PurchaseInvoice.
    void reverseAutoEntry(String sourceType, Long sourceId, String actingUsername);
}
