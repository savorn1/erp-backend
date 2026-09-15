package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostingRuleResponse {

    private Long id;
    private Long companyId;
    private String companyName;

    private Long accountsReceivableAccountId;
    private String accountsReceivableAccountLabel;
    private Long accountsPayableAccountId;
    private String accountsPayableAccountLabel;
    private Long salesRevenueAccountId;
    private String salesRevenueAccountLabel;
    private Long salesReturnsAccountId;
    private String salesReturnsAccountLabel;
    private Long purchaseExpenseAccountId;
    private String purchaseExpenseAccountLabel;
    private Long purchaseReturnsAccountId;
    private String purchaseReturnsAccountLabel;
    private Long taxPayableAccountId;
    private String taxPayableAccountLabel;
    private Long taxReceivableAccountId;
    private String taxReceivableAccountLabel;
    private Long defaultCashAccountId;
    private String defaultCashAccountLabel;
    private Long defaultBankAccountId;
    private String defaultBankAccountLabel;
    private Long fixedAssetCostAccountId;
    private String fixedAssetCostAccountLabel;
    private Long depreciationExpenseAccountId;
    private String depreciationExpenseAccountLabel;
    private Long accumulatedDepreciationAccountId;
    private String accumulatedDepreciationAccountLabel;
    private Long assetDisposalGainLossAccountId;
    private String assetDisposalGainLossAccountLabel;
    private Long inventoryAssetAccountId;
    private String inventoryAssetAccountLabel;
    private Long cashVarianceAccountId;
    private String cashVarianceAccountLabel;
}
