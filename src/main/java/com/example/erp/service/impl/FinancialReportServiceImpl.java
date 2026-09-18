package com.example.erp.service.impl;

import com.example.erp.dto.BalanceSheetFilterRequest;
import com.example.erp.dto.BalanceSheetLineResponse;
import com.example.erp.dto.BalanceSheetResponse;
import com.example.erp.dto.BudgetVsActualFilterRequest;
import com.example.erp.dto.BudgetVsActualResponse;
import com.example.erp.dto.BudgetVsActualRowResponse;
import com.example.erp.dto.CashFlowAccountRowResponse;
import com.example.erp.dto.CashFlowFilterRequest;
import com.example.erp.dto.CashFlowResponse;
import com.example.erp.dto.GeneralLedgerFilterRequest;
import com.example.erp.dto.GeneralLedgerLineResponse;
import com.example.erp.dto.GeneralLedgerResponse;
import com.example.erp.dto.ProfitAndLossFilterRequest;
import com.example.erp.dto.ProfitAndLossResponse;
import com.example.erp.dto.StatementOfChangesInEquityLineResponse;
import com.example.erp.dto.StatementOfChangesInEquityResponse;
import com.example.erp.dto.TrialBalanceFilterRequest;
import com.example.erp.dto.TrialBalanceResponse;
import com.example.erp.dto.TrialBalanceRowResponse;
import com.example.erp.entity.Account;
import com.example.erp.entity.AccountType;
import com.example.erp.entity.AccountingPeriod;
import com.example.erp.entity.BankAccount;
import com.example.erp.entity.BankTransaction;
import com.example.erp.entity.BankTransactionType;
import com.example.erp.entity.Budget;
import com.example.erp.entity.JournalEntry;
import com.example.erp.entity.JournalEntryLine;
import com.example.erp.entity.JournalEntryStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.repository.BankAccountRepository;
import com.example.erp.repository.BankTransactionRepository;
import com.example.erp.repository.BudgetRepository;
import com.example.erp.repository.JournalEntryLineRepository;
import com.example.erp.repository.JournalEntryRepository;
import com.example.erp.service.FinancialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportServiceImpl implements FinancialReportService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final BudgetRepository budgetRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceResponse trialBalance(TrialBalanceFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();
        List<JournalEntryLine> lines = postedLines(filter.getCompanyId(), null, asOfDate);

        Map<Long, BigDecimal[]> byAccount = groupByAccount(lines);
        Map<Long, Account> accounts = accountRepository.findAllById(byAccount.keySet()).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<TrialBalanceRowResponse> rows = new ArrayList<>();
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal[]> entry : byAccount.entrySet()) {
            Account account = accounts.get(entry.getKey());
            if (account == null) continue;
            BigDecimal debitSum = entry.getValue()[0];
            BigDecimal creditSum = entry.getValue()[1];
            BigDecimal net = debitSum.subtract(creditSum);
            if (net.compareTo(BigDecimal.ZERO) == 0) continue;
            BigDecimal debitBalance = net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO;
            BigDecimal creditBalance = net.compareTo(BigDecimal.ZERO) < 0 ? net.negate() : BigDecimal.ZERO;
            rows.add(TrialBalanceRowResponse.builder()
                    .accountId(account.getId())
                    .accountCode(account.getAccountCode())
                    .accountName(account.getName())
                    .accountType(account.getAccountType().name())
                    .debitBalance(debitBalance)
                    .creditBalance(creditBalance)
                    .build());
            debitTotal = debitTotal.add(debitBalance);
            creditTotal = creditTotal.add(creditBalance);
        }
        rows.sort(Comparator.comparing(TrialBalanceRowResponse::getAccountCode));

        return TrialBalanceResponse.builder()
                .asOfDate(asOfDate)
                .rows(rows)
                .debitTotal(debitTotal)
                .creditTotal(creditTotal)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralLedgerResponse generalLedger(GeneralLedgerFilterRequest filter) {
        Account account = accountRepository.findById(filter.getAccountId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + filter.getAccountId()));
        boolean debitNormal = account.getAccountType() == AccountType.ASSET || account.getAccountType() == AccountType.EXPENSE;

        List<Specification<JournalEntry>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), JournalEntryStatus.POSTED));
        conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), account.getCompanyId()));
        List<JournalEntry> entries = journalEntryRepository.findAll(Specification.allOf(conditions));
        Map<Long, JournalEntry> entriesById = new HashMap<>();
        entries.forEach(e -> entriesById.put(e.getId(), e));

        List<JournalEntryLine> lines = entries.isEmpty() ? List.of()
                : journalEntryLineRepository.findByJournalEntryIdIn(entries.stream().map(JournalEntry::getId).toList()).stream()
                        .filter(l -> l.getAccountId().equals(account.getId()))
                        .sorted(Comparator.comparing((JournalEntryLine l) -> entriesById.get(l.getJournalEntryId()).getEntryDate())
                                .thenComparing(JournalEntryLine::getId))
                        .toList();

        BigDecimal openingBalance = BigDecimal.ZERO;
        List<GeneralLedgerLineResponse> lineResponses = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (JournalEntryLine line : lines) {
            LocalDate entryDate = entriesById.get(line.getJournalEntryId()).getEntryDate();
            BigDecimal delta = debitNormal ? line.getDebit().subtract(line.getCredit()) : line.getCredit().subtract(line.getDebit());
            if (filter.getDateFrom() != null && entryDate.isBefore(filter.getDateFrom())) {
                openingBalance = openingBalance.add(delta);
                continue;
            }
            if (filter.getDateTo() != null && entryDate.isAfter(filter.getDateTo())) {
                continue;
            }
            running = (lineResponses.isEmpty() ? openingBalance : running).add(delta);
            JournalEntry entry = entriesById.get(line.getJournalEntryId());
            lineResponses.add(GeneralLedgerLineResponse.builder()
                    .journalEntryId(entry.getId())
                    .journalNumber(entry.getJournalNumber())
                    .entryDate(entryDate)
                    .description(line.getDescription() != null ? line.getDescription() : entry.getDescription())
                    .debit(line.getDebit())
                    .credit(line.getCredit())
                    .runningBalance(running)
                    .build());
        }

        return GeneralLedgerResponse.builder()
                .accountId(account.getId())
                .accountCode(account.getAccountCode())
                .accountName(account.getName())
                .accountType(account.getAccountType().name())
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .openingBalance(openingBalance)
                .lines(lineResponses)
                .closingBalance(lineResponses.isEmpty() ? openingBalance : lineResponses.get(lineResponses.size() - 1).getRunningBalance())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetResponse balanceSheet(BalanceSheetFilterRequest filter) {
        LocalDate asOfDate = filter.getAsOfDate() != null ? filter.getAsOfDate() : LocalDate.now();
        List<JournalEntryLine> lines = postedLines(filter.getCompanyId(), null, asOfDate);

        Map<Long, BigDecimal[]> byAccount = groupByAccount(lines);
        Map<Long, Account> accounts = accountRepository.findAllById(byAccount.keySet()).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<BalanceSheetLineResponse> assets = new ArrayList<>();
        List<BalanceSheetLineResponse> liabilities = new ArrayList<>();
        List<BalanceSheetLineResponse> equity = new ArrayList<>();
        BigDecimal assetsTotal = BigDecimal.ZERO;
        BigDecimal liabilitiesTotal = BigDecimal.ZERO;
        BigDecimal equityTotal = BigDecimal.ZERO;
        BigDecimal revenueTotal = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal[]> entry : byAccount.entrySet()) {
            Account account = accounts.get(entry.getKey());
            if (account == null) continue;
            BigDecimal debitSum = entry.getValue()[0];
            BigDecimal creditSum = entry.getValue()[1];
            switch (account.getAccountType()) {
                case ASSET -> {
                    BigDecimal balance = debitSum.subtract(creditSum);
                    if (balance.compareTo(BigDecimal.ZERO) == 0) continue;
                    assets.add(line(account, balance));
                    assetsTotal = assetsTotal.add(balance);
                }
                case LIABILITY -> {
                    BigDecimal balance = creditSum.subtract(debitSum);
                    if (balance.compareTo(BigDecimal.ZERO) == 0) continue;
                    liabilities.add(line(account, balance));
                    liabilitiesTotal = liabilitiesTotal.add(balance);
                }
                case EQUITY -> {
                    BigDecimal balance = creditSum.subtract(debitSum);
                    if (balance.compareTo(BigDecimal.ZERO) == 0) continue;
                    equity.add(line(account, balance));
                    equityTotal = equityTotal.add(balance);
                }
                case REVENUE -> revenueTotal = revenueTotal.add(creditSum.subtract(debitSum));
                case EXPENSE -> expenseTotal = expenseTotal.add(debitSum.subtract(creditSum));
            }
        }
        assets.sort(Comparator.comparing(BalanceSheetLineResponse::getAccountCode));
        liabilities.sort(Comparator.comparing(BalanceSheetLineResponse::getAccountCode));
        equity.sort(Comparator.comparing(BalanceSheetLineResponse::getAccountCode));

        BigDecimal netIncome = revenueTotal.subtract(expenseTotal);
        equity.add(BalanceSheetLineResponse.builder().accountId(null).accountCode(null).accountName("Current period earnings").amount(netIncome).build());
        equityTotal = equityTotal.add(netIncome);
        BigDecimal liabilitiesAndEquityTotal = liabilitiesTotal.add(equityTotal);

        return BalanceSheetResponse.builder()
                .asOfDate(asOfDate)
                .assets(assets)
                .assetsTotal(assetsTotal)
                .liabilities(liabilities)
                .liabilitiesTotal(liabilitiesTotal)
                .equity(equity)
                .equityTotal(equityTotal)
                .liabilitiesAndEquityTotal(liabilitiesAndEquityTotal)
                .balanced(assetsTotal.compareTo(liabilitiesAndEquityTotal) == 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitAndLossResponse profitAndLoss(ProfitAndLossFilterRequest filter) {
        List<JournalEntryLine> lines = postedLines(filter.getCompanyId(), filter.getDateFrom(), filter.getDateTo());

        Map<Long, BigDecimal[]> byAccount = groupByAccount(lines);
        Map<Long, Account> accounts = accountRepository.findAllById(byAccount.keySet()).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<BalanceSheetLineResponse> revenue = new ArrayList<>();
        List<BalanceSheetLineResponse> expenses = new ArrayList<>();
        BigDecimal revenueTotal = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal[]> entry : byAccount.entrySet()) {
            Account account = accounts.get(entry.getKey());
            if (account == null) continue;
            BigDecimal debitSum = entry.getValue()[0];
            BigDecimal creditSum = entry.getValue()[1];
            if (account.getAccountType() == AccountType.REVENUE) {
                BigDecimal balance = creditSum.subtract(debitSum);
                if (balance.compareTo(BigDecimal.ZERO) == 0) continue;
                revenue.add(line(account, balance));
                revenueTotal = revenueTotal.add(balance);
            } else if (account.getAccountType() == AccountType.EXPENSE) {
                BigDecimal balance = debitSum.subtract(creditSum);
                if (balance.compareTo(BigDecimal.ZERO) == 0) continue;
                expenses.add(line(account, balance));
                expenseTotal = expenseTotal.add(balance);
            }
        }
        revenue.sort(Comparator.comparing(BalanceSheetLineResponse::getAccountCode));
        expenses.sort(Comparator.comparing(BalanceSheetLineResponse::getAccountCode));

        return ProfitAndLossResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .revenue(revenue)
                .revenueTotal(revenueTotal)
                .expenses(expenses)
                .expenseTotal(expenseTotal)
                .netIncome(revenueTotal.subtract(expenseTotal))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CashFlowResponse cashFlow(CashFlowFilterRequest filter) {
        List<BankAccount> bankAccounts = filter.getCompanyId() != null
                ? bankAccountRepository.findAll().stream().filter(a -> a.getCompanyId().equals(filter.getCompanyId())).toList()
                : bankAccountRepository.findAll();

        List<CashFlowAccountRowResponse> rows = new ArrayList<>();
        BigDecimal totalOpening = BigDecimal.ZERO;
        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;

        for (BankAccount bankAccount : bankAccounts) {
            List<BankTransaction> transactions = bankTransactionRepository.findByBankAccountId(bankAccount.getId());
            BigDecimal opening = bankAccount.getOpeningBalance();
            BigDecimal inflow = BigDecimal.ZERO;
            BigDecimal outflow = BigDecimal.ZERO;
            for (BankTransaction transaction : transactions) {
                boolean isInflow = transaction.getType() == BankTransactionType.DEPOSIT || transaction.getType() == BankTransactionType.TRANSFER_IN;
                LocalDate date = transaction.getTransactionDate();
                if (filter.getDateFrom() != null && date.isBefore(filter.getDateFrom())) {
                    opening = isInflow ? opening.add(transaction.getAmount()) : opening.subtract(transaction.getAmount());
                    continue;
                }
                if (filter.getDateTo() != null && date.isAfter(filter.getDateTo())) continue;
                if (isInflow) inflow = inflow.add(transaction.getAmount());
                else outflow = outflow.add(transaction.getAmount());
            }
            BigDecimal netChange = inflow.subtract(outflow);
            BigDecimal closing = opening.add(netChange);
            rows.add(CashFlowAccountRowResponse.builder()
                    .bankAccountId(bankAccount.getId())
                    .bankAccountName(bankAccount.getName())
                    .openingBalance(opening)
                    .inflow(inflow)
                    .outflow(outflow)
                    .netChange(netChange)
                    .closingBalance(closing)
                    .build());
            totalOpening = totalOpening.add(opening);
            totalInflow = totalInflow.add(inflow);
            totalOutflow = totalOutflow.add(outflow);
        }
        rows.sort(Comparator.comparing(CashFlowAccountRowResponse::getBankAccountName));

        return CashFlowResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .accounts(rows)
                .totalOpeningBalance(totalOpening)
                .totalInflow(totalInflow)
                .totalOutflow(totalOutflow)
                .totalNetChange(totalInflow.subtract(totalOutflow))
                .totalClosingBalance(totalOpening.add(totalInflow).subtract(totalOutflow))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StatementOfChangesInEquityResponse statementOfChangesInEquity(ProfitAndLossFilterRequest filter) {
        LocalDate dateTo = filter.getDateTo() != null ? filter.getDateTo() : LocalDate.now();
        LocalDate dateFrom = filter.getDateFrom() != null ? filter.getDateFrom() : dateTo.withDayOfMonth(1);

        BalanceSheetFilterRequest beginningFilter = new BalanceSheetFilterRequest();
        beginningFilter.setCompanyId(filter.getCompanyId());
        beginningFilter.setAsOfDate(dateFrom.minusDays(1));
        BigDecimal beginningEquity = balanceSheet(beginningFilter).getEquityTotal();

        BalanceSheetFilterRequest endingFilter = new BalanceSheetFilterRequest();
        endingFilter.setCompanyId(filter.getCompanyId());
        endingFilter.setAsOfDate(dateTo);
        BigDecimal endingEquity = balanceSheet(endingFilter).getEquityTotal();

        ProfitAndLossFilterRequest pnlFilter = new ProfitAndLossFilterRequest();
        pnlFilter.setCompanyId(filter.getCompanyId());
        pnlFilter.setDateFrom(dateFrom);
        pnlFilter.setDateTo(dateTo);
        BigDecimal netIncome = profitAndLoss(pnlFilter).getNetIncome();

        BigDecimal otherEquityChanges = endingEquity.subtract(beginningEquity).subtract(netIncome);

        List<StatementOfChangesInEquityLineResponse> lines = new ArrayList<>();
        lines.add(StatementOfChangesInEquityLineResponse.builder().label("Beginning equity").amount(beginningEquity).build());
        lines.add(StatementOfChangesInEquityLineResponse.builder().label("Net income for the period").amount(netIncome).build());
        lines.add(StatementOfChangesInEquityLineResponse.builder().label("Other equity changes").amount(otherEquityChanges).build());
        lines.add(StatementOfChangesInEquityLineResponse.builder().label("Ending equity").amount(endingEquity).build());

        return StatementOfChangesInEquityResponse.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .lines(lines)
                .beginningEquity(beginningEquity)
                .netIncome(netIncome)
                .otherEquityChanges(otherEquityChanges)
                .endingEquity(endingEquity)
                .build();
    }

    private BalanceSheetLineResponse line(Account account, BigDecimal amount) {
        return BalanceSheetLineResponse.builder()
                .accountId(account.getId())
                .accountCode(account.getAccountCode())
                .accountName(account.getName())
                .amount(amount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetVsActualResponse budgetVsActual(BudgetVsActualFilterRequest filter) {
        List<JournalEntryLine> lines = postedLines(filter.getCompanyId(), filter.getDateFrom(), filter.getDateTo());
        if (filter.getCostCenterId() != null) {
            lines = lines.stream().filter(l -> filter.getCostCenterId().equals(l.getCostCenterId())).toList();
        }
        Map<Long, BigDecimal[]> actualsByAccount = groupByAccount(lines);

        List<AccountingPeriod> periods = filter.getCompanyId() != null
                ? accountingPeriodRepository.findByCompanyId(filter.getCompanyId())
                : List.of();
        List<Long> periodIds = periods.stream()
                .filter(p -> filter.getDateFrom() == null || !p.getEndDate().isBefore(filter.getDateFrom()))
                .filter(p -> filter.getDateTo() == null || !p.getStartDate().isAfter(filter.getDateTo()))
                .map(AccountingPeriod::getId)
                .toList();
        List<Budget> budgets = periodIds.isEmpty() ? List.of() : budgetRepository.findByAccountingPeriodIdIn(periodIds);
        if (filter.getCostCenterId() != null) {
            budgets = budgets.stream().filter(b -> filter.getCostCenterId().equals(b.getCostCenterId())).toList();
        }
        Map<Long, BigDecimal> budgetByAccount = new HashMap<>();
        for (Budget budget : budgets) {
            budgetByAccount.merge(budget.getAccountId(), budget.getAmount(), BigDecimal::add);
        }

        Set<Long> accountIds = new HashSet<>();
        accountIds.addAll(actualsByAccount.keySet());
        accountIds.addAll(budgetByAccount.keySet());
        Map<Long, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<BudgetVsActualRowResponse> rows = new ArrayList<>();
        for (Long accountId : accountIds) {
            Account account = accounts.get(accountId);
            if (account == null) continue;
            boolean debitNormal = account.getAccountType() == AccountType.ASSET || account.getAccountType() == AccountType.EXPENSE;
            BigDecimal[] debitCredit = actualsByAccount.getOrDefault(accountId, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal actual = debitNormal ? debitCredit[0].subtract(debitCredit[1]) : debitCredit[1].subtract(debitCredit[0]);
            BigDecimal budgetAmount = budgetByAccount.getOrDefault(accountId, BigDecimal.ZERO);
            rows.add(BudgetVsActualRowResponse.builder()
                    .accountId(account.getId())
                    .accountCode(account.getAccountCode())
                    .accountName(account.getName())
                    .accountType(account.getAccountType().name())
                    .budgetAmount(budgetAmount)
                    .actualAmount(actual)
                    .varianceAmount(actual.subtract(budgetAmount))
                    .build());
        }
        rows.sort(Comparator.comparing(BudgetVsActualRowResponse::getAccountCode));

        return BudgetVsActualResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .build();
    }

    // Every POSTED line for the company (if given) with entryDate in
    // [dateFrom, dateTo] — either bound may be null for "unbounded".
    private List<JournalEntryLine> postedLines(Long companyId, LocalDate dateFrom, LocalDate dateTo) {
        List<Specification<JournalEntry>> conditions = new ArrayList<>();
        conditions.add((root, query, cb) -> cb.equal(root.get("status"), JournalEntryStatus.POSTED));
        if (companyId != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        }
        if (dateFrom != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("entryDate"), dateFrom));
        }
        if (dateTo != null) {
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("entryDate"), dateTo));
        }
        List<JournalEntry> entries = journalEntryRepository.findAll(Specification.allOf(conditions));
        if (entries.isEmpty()) return List.of();
        return journalEntryLineRepository.findByJournalEntryIdIn(entries.stream().map(JournalEntry::getId).toList());
    }

    // accountId -> [totalDebit, totalCredit].
    private Map<Long, BigDecimal[]> groupByAccount(List<JournalEntryLine> lines) {
        Map<Long, BigDecimal[]> byAccount = new HashMap<>();
        for (JournalEntryLine line : lines) {
            BigDecimal[] bucket = byAccount.computeIfAbsent(line.getAccountId(), id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            bucket[0] = bucket[0].add(line.getDebit());
            bucket[1] = bucket[1].add(line.getCredit());
        }
        return byAccount;
    }
}
