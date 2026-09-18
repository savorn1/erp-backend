package com.example.erp.service.impl;

import com.example.erp.dto.BudgetFilterRequest;
import com.example.erp.dto.BudgetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpsertBudgetRequest;
import com.example.erp.entity.Account;
import com.example.erp.entity.AccountingPeriod;
import com.example.erp.entity.Budget;
import com.example.erp.entity.CostCenter;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.repository.BudgetRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CostCenterRepository;
import com.example.erp.service.BudgetService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final CostCenterRepository costCenterRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BudgetResponse> listBudgets(BudgetFilterRequest filter) {
        List<Specification<Budget>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCostCenterId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("costCenterId"), filter.getCostCenterId()));
        }
        if (filter.getAccountingPeriodId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("accountingPeriodId"), filter.getAccountingPeriodId()));
        } else if (filter.getFiscalYearId() != null) {
            List<Long> periodIds = accountingPeriodRepository.findByFiscalYearIdOrderByPeriodNumberAsc(filter.getFiscalYearId())
                    .stream().map(AccountingPeriod::getId).toList();
            conditions.add((root, query, cb) -> root.get("accountingPeriodId").in(periodIds));
        }
        Specification<Budget> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Budget> page = budgetRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public BudgetResponse upsertBudget(UpsertBudgetRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        Account account = requireAccount(request.getAccountId(), request.getCompanyId());
        CostCenter costCenter = null;
        if (request.getCostCenterId() != null) {
            costCenter = costCenterRepository.findById(request.getCostCenterId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Cost center not found with id: " + request.getCostCenterId()));
            if (!costCenter.getCompanyId().equals(request.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Cost center does not belong to the selected company");
            }
        }
        AccountingPeriod period = accountingPeriodRepository.findById(request.getAccountingPeriodId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Accounting period not found with id: " + request.getAccountingPeriodId()));
        if (!period.getCompanyId().equals(request.getCompanyId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Accounting period does not belong to the selected company");
        }

        Budget budget = budgetRepository.findByCompanyIdAndAccountIdAndCostCenterIdAndAccountingPeriodId(
                        request.getCompanyId(), request.getAccountId(), request.getCostCenterId(), request.getAccountingPeriodId())
                .orElseGet(() -> Budget.builder()
                        .companyId(request.getCompanyId())
                        .accountId(request.getAccountId())
                        .costCenterId(request.getCostCenterId())
                        .accountingPeriodId(request.getAccountingPeriodId())
                        .createdBy(actingUsername)
                        .build());
        budget.setAmount(request.getAmount());
        budget.setNotes(request.getNotes());
        budgetRepository.save(budget);

        return toResponse(budget, account, costCenter, period);
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Budget not found with id: " + id));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        Account account = accountRepository.findById(budget.getAccountId()).orElse(null);
        CostCenter costCenter = budget.getCostCenterId() == null ? null
                : costCenterRepository.findById(budget.getCostCenterId()).orElse(null);
        AccountingPeriod period = accountingPeriodRepository.findById(budget.getAccountingPeriodId()).orElse(null);
        return toResponse(budget, account, costCenter, period);
    }

    private BudgetResponse toResponse(Budget budget, Account account, CostCenter costCenter, AccountingPeriod period) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .companyId(budget.getCompanyId())
                .accountId(budget.getAccountId())
                .accountCode(account == null ? null : account.getAccountCode())
                .accountName(account == null ? null : account.getName())
                .costCenterId(budget.getCostCenterId())
                .costCenterName(costCenter == null ? null : costCenter.getName())
                .accountingPeriodId(budget.getAccountingPeriodId())
                .periodName(period == null ? null : period.getName())
                .amount(budget.getAmount())
                .notes(budget.getNotes())
                .createdBy(budget.getCreatedBy())
                .build();
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private Account requireAccount(Long accountId, Long companyId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + accountId));
        if (!account.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Account does not belong to the selected company");
        }
        return account;
    }
}
