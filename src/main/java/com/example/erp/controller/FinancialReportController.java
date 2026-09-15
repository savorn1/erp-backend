package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
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
import com.example.erp.service.FinancialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/financial-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    @GetMapping("/trial-balance")
    public ResponseEntity<ApiResponse<TrialBalanceResponse>> trialBalance(@ModelAttribute TrialBalanceFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.trialBalance(filter)));
    }

    @GetMapping("/general-ledger")
    public ResponseEntity<ApiResponse<GeneralLedgerResponse>> generalLedger(@ModelAttribute GeneralLedgerFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.generalLedger(filter)));
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<ApiResponse<BalanceSheetResponse>> balanceSheet(@ModelAttribute BalanceSheetFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.balanceSheet(filter)));
    }

    @GetMapping("/profit-and-loss")
    public ResponseEntity<ApiResponse<ProfitAndLossResponse>> profitAndLoss(@ModelAttribute ProfitAndLossFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.profitAndLoss(filter)));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ApiResponse<CashFlowResponse>> cashFlow(@ModelAttribute CashFlowFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.cashFlow(filter)));
    }

    @GetMapping("/statement-of-changes-in-equity")
    public ResponseEntity<ApiResponse<StatementOfChangesInEquityResponse>> statementOfChangesInEquity(@ModelAttribute ProfitAndLossFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(financialReportService.statementOfChangesInEquity(filter)));
    }
}
