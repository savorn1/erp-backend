package com.example.erp.service.impl;

import com.example.erp.dto.PostingRuleRequest;
import com.example.erp.dto.PostingRuleResponse;
import com.example.erp.entity.Account;
import com.example.erp.entity.Company;
import com.example.erp.entity.PostingRule;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.PostingRuleRepository;
import com.example.erp.service.AccountService;
import com.example.erp.service.PostingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostingRuleServiceImpl implements PostingRuleService {

    private final PostingRuleRepository postingRuleRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    // One default account code per module — matches AccountServiceImpl's
    // standard seed chart exactly, so seedFromChartOfAccounts can always
    // find every one of these once that chart has been seeded.
    private static final String CODE_ACCOUNTS_RECEIVABLE = "1130";
    private static final String CODE_ACCOUNTS_PAYABLE = "2100";
    private static final String CODE_SALES_REVENUE = "4100";
    private static final String CODE_SALES_RETURNS = "4900";
    private static final String CODE_PURCHASE_EXPENSE = "5100";
    private static final String CODE_PURCHASE_RETURNS = "5150";
    private static final String CODE_TAX_PAYABLE = "2200";
    private static final String CODE_TAX_RECEIVABLE = "1160";
    private static final String CODE_DEFAULT_CASH = "1110";
    private static final String CODE_DEFAULT_BANK = "1120";
    private static final String CODE_FIXED_ASSET_COST = "1200";
    private static final String CODE_DEPRECIATION_EXPENSE = "5500";
    private static final String CODE_ACCUMULATED_DEPRECIATION = "1250";
    private static final String CODE_ASSET_DISPOSAL_GAIN_LOSS = "5900";
    private static final String CODE_INVENTORY_ASSET = "1140";
    private static final String CODE_POS_CASH = "1113";
    private static final String CODE_PETTY_CASH = "1112";
    private static final String CODE_CASH_IN_TRANSIT = "1114";

    @Override
    @Transactional(readOnly = true)
    public PostingRuleResponse getForCompany(Long companyId) {
        return postingRuleRepository.findByCompanyId(companyId)
                .map(this::toResponse)
                .orElseGet(() -> PostingRuleResponse.builder().companyId(companyId).build());
    }

    @Override
    @Transactional
    public PostingRuleResponse upsert(PostingRuleRequest request) {
        requireCompany(request.getCompanyId());
        List<Long> accountIds = java.util.stream.Stream.of(
                request.getAccountsReceivableAccountId(), request.getAccountsPayableAccountId(),
                request.getSalesRevenueAccountId(), request.getSalesReturnsAccountId(),
                request.getPurchaseExpenseAccountId(), request.getPurchaseReturnsAccountId(),
                request.getTaxPayableAccountId(), request.getTaxReceivableAccountId(),
                request.getDefaultCashAccountId(), request.getDefaultBankAccountId(),
                request.getFixedAssetCostAccountId(), request.getDepreciationExpenseAccountId(),
                request.getAccumulatedDepreciationAccountId(), request.getAssetDisposalGainLossAccountId(),
                request.getInventoryAssetAccountId(), request.getCashVarianceAccountId(),
                request.getPosCashAccountId(), request.getPettyCashAccountId(), request.getCashInTransitAccountId()
        ).filter(Objects::nonNull).distinct().toList();

        Map<Long, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));
        for (Long accountId : accountIds) {
            Account account = accounts.get(accountId);
            if (account == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + accountId);
            }
            if (!account.getCompanyId().equals(request.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Account " + account.getAccountCode() + " does not belong to the selected company");
            }
        }

        PostingRule rule = postingRuleRepository.findByCompanyId(request.getCompanyId())
                .orElseGet(() -> PostingRule.builder().companyId(request.getCompanyId()).build());
        rule.setAccountsReceivableAccountId(request.getAccountsReceivableAccountId());
        rule.setAccountsPayableAccountId(request.getAccountsPayableAccountId());
        rule.setSalesRevenueAccountId(request.getSalesRevenueAccountId());
        rule.setSalesReturnsAccountId(request.getSalesReturnsAccountId());
        rule.setPurchaseExpenseAccountId(request.getPurchaseExpenseAccountId());
        rule.setPurchaseReturnsAccountId(request.getPurchaseReturnsAccountId());
        rule.setTaxPayableAccountId(request.getTaxPayableAccountId());
        rule.setTaxReceivableAccountId(request.getTaxReceivableAccountId());
        rule.setDefaultCashAccountId(request.getDefaultCashAccountId());
        rule.setDefaultBankAccountId(request.getDefaultBankAccountId());
        rule.setFixedAssetCostAccountId(request.getFixedAssetCostAccountId());
        rule.setDepreciationExpenseAccountId(request.getDepreciationExpenseAccountId());
        rule.setAccumulatedDepreciationAccountId(request.getAccumulatedDepreciationAccountId());
        rule.setAssetDisposalGainLossAccountId(request.getAssetDisposalGainLossAccountId());
        rule.setInventoryAssetAccountId(request.getInventoryAssetAccountId());
        rule.setCashVarianceAccountId(request.getCashVarianceAccountId());
        rule.setPosCashAccountId(request.getPosCashAccountId());
        rule.setPettyCashAccountId(request.getPettyCashAccountId());
        rule.setCashInTransitAccountId(request.getCashInTransitAccountId());
        postingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Override
    @Transactional
    public PostingRuleResponse seedFromChartOfAccounts(Long companyId) {
        requireCompany(companyId);
        accountService.seedSampleChartOfAccounts(companyId);

        Map<String, Account> byCode = accountRepository.findByCompanyId(companyId).stream()
                .collect(Collectors.toMap(Account::getAccountCode, a -> a, (a, b) -> a));

        PostingRule rule = postingRuleRepository.findByCompanyId(companyId)
                .orElseGet(() -> PostingRule.builder().companyId(companyId).build());
        if (rule.getAccountsReceivableAccountId() == null) rule.setAccountsReceivableAccountId(idFor(byCode, CODE_ACCOUNTS_RECEIVABLE));
        if (rule.getAccountsPayableAccountId() == null) rule.setAccountsPayableAccountId(idFor(byCode, CODE_ACCOUNTS_PAYABLE));
        if (rule.getSalesRevenueAccountId() == null) rule.setSalesRevenueAccountId(idFor(byCode, CODE_SALES_REVENUE));
        if (rule.getSalesReturnsAccountId() == null) rule.setSalesReturnsAccountId(idFor(byCode, CODE_SALES_RETURNS));
        if (rule.getPurchaseExpenseAccountId() == null) rule.setPurchaseExpenseAccountId(idFor(byCode, CODE_PURCHASE_EXPENSE));
        if (rule.getPurchaseReturnsAccountId() == null) rule.setPurchaseReturnsAccountId(idFor(byCode, CODE_PURCHASE_RETURNS));
        if (rule.getTaxPayableAccountId() == null) rule.setTaxPayableAccountId(idFor(byCode, CODE_TAX_PAYABLE));
        if (rule.getTaxReceivableAccountId() == null) rule.setTaxReceivableAccountId(idFor(byCode, CODE_TAX_RECEIVABLE));
        if (rule.getDefaultCashAccountId() == null) rule.setDefaultCashAccountId(idFor(byCode, CODE_DEFAULT_CASH));
        if (rule.getDefaultBankAccountId() == null) rule.setDefaultBankAccountId(idFor(byCode, CODE_DEFAULT_BANK));
        if (rule.getFixedAssetCostAccountId() == null) rule.setFixedAssetCostAccountId(idFor(byCode, CODE_FIXED_ASSET_COST));
        if (rule.getDepreciationExpenseAccountId() == null) rule.setDepreciationExpenseAccountId(idFor(byCode, CODE_DEPRECIATION_EXPENSE));
        if (rule.getAccumulatedDepreciationAccountId() == null) rule.setAccumulatedDepreciationAccountId(idFor(byCode, CODE_ACCUMULATED_DEPRECIATION));
        if (rule.getAssetDisposalGainLossAccountId() == null) rule.setAssetDisposalGainLossAccountId(idFor(byCode, CODE_ASSET_DISPOSAL_GAIN_LOSS));
        if (rule.getInventoryAssetAccountId() == null) rule.setInventoryAssetAccountId(idFor(byCode, CODE_INVENTORY_ASSET));
        if (rule.getPosCashAccountId() == null) rule.setPosCashAccountId(idFor(byCode, CODE_POS_CASH));
        if (rule.getPettyCashAccountId() == null) rule.setPettyCashAccountId(idFor(byCode, CODE_PETTY_CASH));
        if (rule.getCashInTransitAccountId() == null) rule.setCashInTransitAccountId(idFor(byCode, CODE_CASH_IN_TRANSIT));
        postingRuleRepository.save(rule);
        return toResponse(rule);
    }

    private Long idFor(Map<String, Account> byCode, String code) {
        Account account = byCode.get(code);
        return account == null ? null : account.getId();
    }

    private PostingRuleResponse toResponse(PostingRule rule) {
        List<Long> accountIds = new ArrayList<>();
        accountIds.add(rule.getAccountsReceivableAccountId());
        accountIds.add(rule.getAccountsPayableAccountId());
        accountIds.add(rule.getSalesRevenueAccountId());
        accountIds.add(rule.getSalesReturnsAccountId());
        accountIds.add(rule.getPurchaseExpenseAccountId());
        accountIds.add(rule.getPurchaseReturnsAccountId());
        accountIds.add(rule.getTaxPayableAccountId());
        accountIds.add(rule.getTaxReceivableAccountId());
        accountIds.add(rule.getDefaultCashAccountId());
        accountIds.add(rule.getDefaultBankAccountId());
        accountIds.add(rule.getFixedAssetCostAccountId());
        accountIds.add(rule.getDepreciationExpenseAccountId());
        accountIds.add(rule.getAccumulatedDepreciationAccountId());
        accountIds.add(rule.getAssetDisposalGainLossAccountId());
        accountIds.add(rule.getInventoryAssetAccountId());
        accountIds.add(rule.getCashVarianceAccountId());
        accountIds.add(rule.getPosCashAccountId());
        accountIds.add(rule.getPettyCashAccountId());
        accountIds.add(rule.getCashInTransitAccountId());
        accountIds.removeIf(Objects::isNull);
        Map<Long, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));
        Company company = companyRepository.findById(rule.getCompanyId()).orElse(null);

        return PostingRuleResponse.builder()
                .id(rule.getId())
                .companyId(rule.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .accountsReceivableAccountId(rule.getAccountsReceivableAccountId())
                .accountsReceivableAccountLabel(label(accounts, rule.getAccountsReceivableAccountId()))
                .accountsPayableAccountId(rule.getAccountsPayableAccountId())
                .accountsPayableAccountLabel(label(accounts, rule.getAccountsPayableAccountId()))
                .salesRevenueAccountId(rule.getSalesRevenueAccountId())
                .salesRevenueAccountLabel(label(accounts, rule.getSalesRevenueAccountId()))
                .salesReturnsAccountId(rule.getSalesReturnsAccountId())
                .salesReturnsAccountLabel(label(accounts, rule.getSalesReturnsAccountId()))
                .purchaseExpenseAccountId(rule.getPurchaseExpenseAccountId())
                .purchaseExpenseAccountLabel(label(accounts, rule.getPurchaseExpenseAccountId()))
                .purchaseReturnsAccountId(rule.getPurchaseReturnsAccountId())
                .purchaseReturnsAccountLabel(label(accounts, rule.getPurchaseReturnsAccountId()))
                .taxPayableAccountId(rule.getTaxPayableAccountId())
                .taxPayableAccountLabel(label(accounts, rule.getTaxPayableAccountId()))
                .taxReceivableAccountId(rule.getTaxReceivableAccountId())
                .taxReceivableAccountLabel(label(accounts, rule.getTaxReceivableAccountId()))
                .defaultCashAccountId(rule.getDefaultCashAccountId())
                .defaultCashAccountLabel(label(accounts, rule.getDefaultCashAccountId()))
                .defaultBankAccountId(rule.getDefaultBankAccountId())
                .defaultBankAccountLabel(label(accounts, rule.getDefaultBankAccountId()))
                .fixedAssetCostAccountId(rule.getFixedAssetCostAccountId())
                .fixedAssetCostAccountLabel(label(accounts, rule.getFixedAssetCostAccountId()))
                .depreciationExpenseAccountId(rule.getDepreciationExpenseAccountId())
                .depreciationExpenseAccountLabel(label(accounts, rule.getDepreciationExpenseAccountId()))
                .accumulatedDepreciationAccountId(rule.getAccumulatedDepreciationAccountId())
                .accumulatedDepreciationAccountLabel(label(accounts, rule.getAccumulatedDepreciationAccountId()))
                .assetDisposalGainLossAccountId(rule.getAssetDisposalGainLossAccountId())
                .assetDisposalGainLossAccountLabel(label(accounts, rule.getAssetDisposalGainLossAccountId()))
                .inventoryAssetAccountId(rule.getInventoryAssetAccountId())
                .inventoryAssetAccountLabel(label(accounts, rule.getInventoryAssetAccountId()))
                .cashVarianceAccountId(rule.getCashVarianceAccountId())
                .cashVarianceAccountLabel(label(accounts, rule.getCashVarianceAccountId()))
                .posCashAccountId(rule.getPosCashAccountId())
                .posCashAccountLabel(label(accounts, rule.getPosCashAccountId()))
                .pettyCashAccountId(rule.getPettyCashAccountId())
                .pettyCashAccountLabel(label(accounts, rule.getPettyCashAccountId()))
                .cashInTransitAccountId(rule.getCashInTransitAccountId())
                .cashInTransitAccountLabel(label(accounts, rule.getCashInTransitAccountId()))
                .build();
    }

    private String label(Map<Long, Account> accounts, Long accountId) {
        if (accountId == null) return null;
        Account account = accounts.get(accountId);
        return account == null ? null : account.getAccountCode() + " — " + account.getName();
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
