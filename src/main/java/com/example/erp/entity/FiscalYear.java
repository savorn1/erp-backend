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

// A company's fiscal year, e.g. "FY2026" running Jan 1 - Dec 31 (or
// whatever range Company.fiscalYearStartMonth implies — this entity is the
// actual dated instance, that field is just the yearly default). Divided
// into AccountingPeriods, looked up by fiscalYearId — no JPA relationship
// mapping, matching this codebase's plain-FK convention. Closing a fiscal
// year closes every period within it (see FiscalYearServiceImpl.close) so
// journal entries stop posting into any of its dates.
@Entity
@Table(name = "fiscal_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // e.g. "FY2026" — unique within the company.
    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FiscalYearStatus status = FiscalYearStatus.OPEN;
}
