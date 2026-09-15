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

import java.math.BigDecimal;
import java.time.LocalDate;

// One capitalized asset. accumulatedDepreciation is a denormalized running
// total, bumped by FixedAssetServiceImpl.runDepreciation the same way
// BankAccount.currentBalance is bumped by deposits/withdrawals — the
// authoritative period-by-period history lives in DepreciationEntry.
// Creating one auto-posts an acquisition entry (Dr Fixed Assets / Cr
// Accounts Payable) via AutoPostingService so disposal always has a real
// GL balance to clear — see FixedAssetServiceImpl.create.
@Entity
@Table(name = "fixed_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // e.g. "FA-000123" — unique within the company.
    @Column(name = "asset_code", nullable = false)
    private String assetCode;

    @Column(nullable = false)
    private String name;

    private String description;

    // Free-text grouping label (e.g. "Vehicles", "IT Equipment") — no
    // separate AssetCategory entity, matching the scoped-down register this
    // module was asked to be.
    private String category;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "acquisition_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal acquisitionCost;

    @Column(name = "salvage_value", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "useful_life_months", nullable = false)
    private Integer usefulLifeMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", nullable = false)
    private DepreciationMethod depreciationMethod;

    // Annual percentage rate — only meaningful (and required) for
    // DECLINING_BALANCE; see FixedAssetServiceImpl's depreciation formula.
    @Column(name = "declining_balance_rate", precision = 7, scale = 4)
    private BigDecimal decliningBalanceRate;

    @Column(name = "accumulated_depreciation", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FixedAssetStatus status = FixedAssetStatus.ACTIVE;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "disposal_proceeds", precision = 19, scale = 4)
    private BigDecimal disposalProceeds;
}
