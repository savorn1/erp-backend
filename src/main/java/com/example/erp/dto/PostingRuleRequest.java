package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Every account field is optional — AutoPostingServiceImpl simply skips
// whichever document type needs a mapping that's still null. companyId is
// the only required field, and there's at most one PostingRule per company
// (see PostingRuleServiceImpl.upsert).
@Data
public class PostingRuleRequest {

    @NotNull
    private Long companyId;

    private Long accountsReceivableAccountId;
    private Long accountsPayableAccountId;
    private Long salesRevenueAccountId;
    private Long salesReturnsAccountId;
    private Long purchaseExpenseAccountId;
    private Long purchaseReturnsAccountId;
    private Long taxPayableAccountId;
    private Long taxReceivableAccountId;
    private Long defaultCashAccountId;
    private Long defaultBankAccountId;
    private Long fixedAssetCostAccountId;
    private Long depreciationExpenseAccountId;
    private Long accumulatedDepreciationAccountId;
    private Long assetDisposalGainLossAccountId;
    private Long inventoryAssetAccountId;
    private Long cashVarianceAccountId;
}
