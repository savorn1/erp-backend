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
                request.getDefaultCashAccountId(), request.getDefaultBankAccountId()
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
