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

// One asset's contribution to a DepreciationRun — the authoritative
// per-period history that powers the Depreciation Schedule report.
// accumulatedAfter is a snapshot of FixedAsset.accumulatedDepreciation right
// after this entry was applied, so the schedule never needs to recompute a
// running total from scratch.
@Entity
@Table(name = "depreciation_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepreciationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "depreciation_run_id", nullable = false)
    private Long depreciationRunId;

    // Denormalized from the run, so filtering the schedule report by company
    // doesn't need a join — same convention as AccountingPeriod.companyId.
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "accumulated_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedAfter;
}
