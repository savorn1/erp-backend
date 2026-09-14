package com.example.erp.service;

import com.example.erp.dto.BalanceSheetFilterRequest;
import com.example.erp.dto.BalanceSheetResponse;
import com.example.erp.dto.CashFlowFilterRequest;
import com.example.erp.dto.CashFlowResponse;
import com.example.erp.dto.GeneralLedgerFilterRequest;
import com.example.erp.dto.GeneralLedgerResponse;
import com.example.erp.dto.ProfitAndLossFilterRequest;
import com.example.erp.dto.ProfitAndLossResponse;
import com.example.erp.dto.StatementOfChangesInEquityResponse;
import com.example.erp.dto.TrialBalanceFilterRequest;
import com.example.erp.dto.TrialBalanceResponse;

// Trial balance / general ledger / balance sheet / profit & loss are all
// derived strictly from POSTED Journal Entry lines — since nothing else in
// this system posts to the general ledger automatically yet (see
// JournalEntry's own comment), these reports only reflect what's actually
// been manually journaled. Cash flow is deliberately different: it's built
// from real BankTransaction activity instead, since that data is populated
// by every deposit/withdrawal/transfer regardless of journaling.
public interface FinancialReportService {

    TrialBalanceResponse trialBalance(TrialBalanceFilterRequest filter);

    GeneralLedgerResponse generalLedger(GeneralLedgerFilterRequest filter);

    BalanceSheetResponse balanceSheet(BalanceSheetFilterRequest filter);

    ProfitAndLossResponse profitAndLoss(ProfitAndLossFilterRequest filter);

    CashFlowResponse cashFlow(CashFlowFilterRequest filter);

    // Derived from balanceSheet()'s equityTotal at the start and end of the
    // period, plus profitAndLoss()'s net income for the period — no separate
    // ledger query needed (see StatementOfChangesInEquityResponse).
    StatementOfChangesInEquityResponse statementOfChangesInEquity(ProfitAndLossFilterRequest filter);
}
