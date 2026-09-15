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

import java.time.LocalDate;

// One depreciation batch for a company+period — at most one per
// (companyId, accountingPeriodId), enforced in FixedAssetServiceImpl.
// journalEntryId is null when every asset was skipped (nothing to post) or
// AutoPostingService silently declined (no PostingRule/account mapped yet)
// — the run itself still records that this period was processed, so it's
// never re-run accidentally.
@Entity
@Table(name = "depreciation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepreciationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "accounting_period_id", nullable = false)
    private Long accountingPeriodId;

    @Column(name = "run_date", nullable = false)
    private LocalDate runDate;

    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    private String createdBy;
}
