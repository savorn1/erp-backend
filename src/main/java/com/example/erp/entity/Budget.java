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

import java.math.BigDecimal;

// A planned amount for one Account (optionally scoped to a CostCenter) in
// one AccountingPeriod — the same granularity postings already use, so
// FinancialReportServiceImpl.budgetVsActual can resolve which periods a
// given date range covers and sum straight across them. One row per
// (companyId, accountId, costCenterId, accountingPeriodId) — see
// BudgetServiceImpl.upsertBudget, which finds-or-updates by that key rather
// than exposing a separate id-based edit flow.
@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    // Optional — null means "not scoped to a cost center."
    @Column(name = "cost_center_id")
    private Long costCenterId;

    @Column(name = "accounting_period_id", nullable = false)
    private Long accountingPeriodId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
