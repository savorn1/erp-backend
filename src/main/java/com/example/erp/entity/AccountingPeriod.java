package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// One slice of a FiscalYear (typically a calendar month) — the actual unit
// JournalEntryServiceImpl checks a posting date against. companyId is
// denormalized from the parent FiscalYear so a period lookup by date doesn't
// need a join.
@Entity
@Table(name = "accounting_periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fiscal_year_id", nullable = false)
    private Long fiscalYearId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // 1-based position within the fiscal year.
    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    // e.g. "January 2026".
    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountingPeriodStatus status = AccountingPeriodStatus.OPEN;
}
