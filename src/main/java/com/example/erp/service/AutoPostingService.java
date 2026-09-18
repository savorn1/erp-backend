package com.example.erp.service;

import com.example.erp.entity.CreditNote;
import com.example.erp.entity.FixedAsset;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PettyCashEntry;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.SupplierPayment;

import java.math.BigDecimal;
import java.time.LocalDate;

// Auto-generates a balanced JournalEntry for a business event, using the
// company's PostingRule account mapping. Every method is a no-op (returns
// normally, posts nothing) when the company has no PostingRule configured or
// the specific accounts a given event needs aren't mapped — auto-posting is
// opt-in, and its absence must never block the underlying business action
// (approving an invoice, recording a payment, etc. always succeeds on its
// own terms regardless of GL setup). Call sites: InvoiceServiceImpl,
// PurchaseInvoiceServiceImpl, PaymentServiceImpl, SupplierPaymentServiceImpl,
// CreditNoteServiceImpl, PurchaseCreditNoteServiceImpl, FixedAssetServiceImpl.
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

    // Dr Fixed Assets, at cost (acquisitionCost) / Cr Accounts Payable —
    // posted once, when the asset is created.
    void postFixedAssetAcquisition(FixedAsset asset, String actingUsername);

    // Dr Depreciation Expense / Cr Accumulated Depreciation — one combined
    // entry per depreciation run, summed across every asset it covered.
    void postDepreciationRun(Long companyId, LocalDate date, BigDecimal totalAmount, Long sourceId, String actingUsername);

    // Dr Cash/Bank (proceeds) + Dr Accumulated Depreciation (this asset's
    // balance, cleared) / Cr Fixed Assets, at cost (acquisitionCost) + the
    // gain or loss (proceeds vs. book value) plugged into
    // assetDisposalGainLossAccountId on whichever side balances the entry.
    void postAssetDisposal(FixedAsset asset, BigDecimal proceeds, String actingUsername);

    // Dr Cash (cashAmount, only if > 0) + Dr Bank (bankAmount, only if > 0)
    // / Cr Sales Revenue (netAmount) + Cr Tax Payable (taxAmount, only if >
    // 0); plus, only if cogsAmount > 0: Dr Cost of Goods Sold (reusing
    // purchaseExpenseAccountId) / Cr Inventory (inventoryAssetAccountId).
    // No Accounts Receivable leg — a POS sale is always paid in full at
    // checkout, unlike an Invoice.
    void postPosSale(Long companyId, LocalDate date, BigDecimal cashAmount, BigDecimal bankAmount,
                      BigDecimal netAmount, BigDecimal taxAmount, BigDecimal cogsAmount,
                      Long sourceId, String actingUsername);

    // Adjusts the cash account to match what was physically counted at
    // session close. variance = countedCash - expectedCash: positive (over)
    // -> Dr Cash / Cr cashVarianceAccountId; negative (short) -> Dr
    // cashVarianceAccountId / Cr Cash. A zero variance posts nothing.
    void postCashVariance(Long companyId, LocalDate date, BigDecimal variance, Long sourceId, String actingUsername);

    // One netted, sign-aware entry per POS exchange — netRevenue/netTax/
    // netCogs are each (new items total) minus (returned items total), and
    // cashAmount/bankAmount are the signed settlement (positive = customer
    // paid in, negative = store paid out), placed on whichever the
    // exchange's settlementMethod bucketed to. Each leg posts to whichever
    // side its sign implies (e.g. netRevenue > 0 -> Cr Sales Revenue;
    // netRevenue < 0 -> Dr Sales Revenue, revenue given back). A leg that is
    // exactly zero is omitted entirely, same as postPosSale.
    void postPosExchange(Long companyId, LocalDate date, BigDecimal netRevenue, BigDecimal netTax, BigDecimal netCogs,
                          BigDecimal cashAmount, BigDecimal bankAmount, Long sourceId, String actingUsername);

    // TOPUP: Dr pettyCashAccountId / Cr entry.accountId (the funding
    // source). EXPENSE: Dr entry.accountId (the expense account) / Cr
    // pettyCashAccountId. No-op if pettyCashAccountId isn't mapped.
    void postPettyCashEntry(PettyCashEntry entry, String actingUsername);

    // Finds the auto-posted entry (if any) for this source and reverses it —
    // a POSTED entry gets a swapped-lines reversal (same as
    // JournalEntryServiceImpl.reverse), a DRAFT one (created while its
    // period was locked, never posted) is simply deleted. No-op if no such
    // entry exists or it was already reversed. Called when cancelling an
    // approved Invoice/PurchaseInvoice.
    void reverseAutoEntry(String sourceType, Long sourceId, String actingUsername);
}
