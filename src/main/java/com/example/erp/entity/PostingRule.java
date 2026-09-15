package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One row per company — the "Auto-Posting Accounts" a business event maps
// to. AutoPostingServiceImpl reads this before building a journal entry for
// an approved Invoice/PurchaseInvoice/Payment/SupplierPayment/CreditNote/
// PurchaseCreditNote; every field is nullable and auto-posting for a given
// document is skipped entirely (not an error — the underlying business
// action still succeeds) whenever an account it needs isn't mapped yet.
// taxPayableAccountId/taxReceivableAccountId are the fallback used when a
// tax amount can't be traced to one specific TaxRate.accountId — see
// TaxRate's own comment on why that link usually can't be made (order/
// invoice lines only carry a raw tax percent, not a TaxRate id).
@Entity
@Table(name = "posting_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One PostingRule per company — enforced in PostingRuleServiceImpl, not
    // a DB constraint (ddl-auto=update caution, same as elsewhere).
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "accounts_receivable_account_id")
    private Long accountsReceivableAccountId;

    @Column(name = "accounts_payable_account_id")
    private Long accountsPayableAccountId;

    @Column(name = "sales_revenue_account_id")
    private Long salesRevenueAccountId;

    @Column(name = "sales_returns_account_id")
    private Long salesReturnsAccountId;

    // COGS / purchase expense.
    @Column(name = "purchase_expense_account_id")
    private Long purchaseExpenseAccountId;

    @Column(name = "purchase_returns_account_id")
    private Long purchaseReturnsAccountId;

    // Output tax fallback (sales side).
    @Column(name = "tax_payable_account_id")
    private Long taxPayableAccountId;

    // Input tax fallback (purchase side).
    @Column(name = "tax_receivable_account_id")
    private Long taxReceivableAccountId;

    // Used for a Payment/SupplierPayment whose method is CASH.
    @Column(name = "default_cash_account_id")
    private Long defaultCashAccountId;

    // Used for BANK_TRANSFER / PAYMENT_GATEWAY methods.
    @Column(name = "default_bank_account_id")
    private Long defaultBankAccountId;

    // Shared "Fixed Assets, at cost" account — every FixedAsset's
    // acquisition and disposal posts against this same account (the
    // FixedAsset table itself is the per-asset sub-ledger).
    @Column(name = "fixed_asset_cost_account_id")
    private Long fixedAssetCostAccountId;

    @Column(name = "depreciation_expense_account_id")
    private Long depreciationExpenseAccountId;

    @Column(name = "accumulated_depreciation_account_id")
    private Long accumulatedDepreciationAccountId;

    @Column(name = "asset_disposal_gain_loss_account_id")
    private Long assetDisposalGainLossAccountId;

    // POS: credited when a sale's inventory is relieved (paired with the
    // existing purchaseExpenseAccountId as the COGS debit — see
    // AutoPostingServiceImpl.postPosSale).
    @Column(name = "inventory_asset_account_id")
    private Long inventoryAssetAccountId;

    // POS: Dr/Cr'd for a session's over/short at close — no standard seed
    // code fits this, so it's left unmapped unless the admin sets it.
    @Column(name = "cash_variance_account_id")
    private Long cashVarianceAccountId;
}
