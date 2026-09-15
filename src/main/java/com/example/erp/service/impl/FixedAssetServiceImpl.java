package com.example.erp.service.impl;

import com.example.erp.dto.DepreciationEntryFilterRequest;
import com.example.erp.dto.DepreciationEntryResponse;
import com.example.erp.dto.DepreciationRunResponse;
import com.example.erp.dto.DisposeFixedAssetRequest;
import com.example.erp.dto.FixedAssetFilterRequest;
import com.example.erp.dto.FixedAssetRequest;
import com.example.erp.dto.FixedAssetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RunDepreciationRequest;
import com.example.erp.entity.AccountingPeriod;
import com.example.erp.entity.Company;
import com.example.erp.entity.DepreciationEntry;
import com.example.erp.entity.DepreciationMethod;
import com.example.erp.entity.DepreciationRun;
import com.example.erp.entity.FixedAsset;
import com.example.erp.entity.FixedAssetStatus;
import com.example.erp.entity.JournalEntry;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountingPeriodRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.DepreciationEntryRepository;
import com.example.erp.repository.DepreciationRunRepository;
import com.example.erp.repository.FixedAssetRepository;
import com.example.erp.repository.JournalEntryRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.FixedAssetService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixedAssetServiceImpl implements FixedAssetService {

    private final FixedAssetRepository fixedAssetRepository;
    private final CompanyRepository companyRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final DepreciationRunRepository depreciationRunRepository;
    private final DepreciationEntryRepository depreciationEntryRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AutoPostingService autoPostingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FixedAssetResponse> list(FixedAssetFilterRequest filter) {
        List<Specification<FixedAsset>> conditions = new ArrayList<>();
        if (filter.getAssetCode() != null && !filter.getAssetCode().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("assetCode")), "%" + filter.getAssetCode().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), FixedAssetStatus.valueOf(filter.getStatus())));
        }
        Specification<FixedAsset> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<FixedAsset> page = fixedAssetRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public FixedAssetResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public FixedAssetResponse create(FixedAssetRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        if (fixedAssetRepository.existsByCompanyIdAndAssetCode(request.getCompanyId(), request.getAssetCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A fixed asset with code '" + request.getAssetCode() + "' already exists for this company");
        }
        if (request.getDepreciationMethod() == DepreciationMethod.DECLINING_BALANCE && request.getDecliningBalanceRate() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A declining balance rate is required for the declining balance method");
        }

        FixedAsset asset = FixedAsset.builder()
                .companyId(request.getCompanyId())
                .assetCode(request.getAssetCode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .acquisitionDate(request.getAcquisitionDate())
                .acquisitionCost(request.getAcquisitionCost())
                .salvageValue(request.getSalvageValue() == null ? BigDecimal.ZERO : request.getSalvageValue())
                .usefulLifeMonths(request.getUsefulLifeMonths())
                .depreciationMethod(request.getDepreciationMethod())
                .decliningBalanceRate(request.getDecliningBalanceRate())
                .build();
        fixedAssetRepository.save(asset);
        autoPostingService.postFixedAssetAcquisition(asset, actingUsername);
        return toResponse(asset);
    }

    @Override
    @Transactional
    public FixedAssetResponse update(Long id, FixedAssetRequest request) {
        FixedAsset asset = find(id);
        if (fixedAssetRepository.existsByCompanyIdAndAssetCodeAndIdNot(request.getCompanyId(), request.getAssetCode(), id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A fixed asset with code '" + request.getAssetCode() + "' already exists for this company");
        }
        // Once depreciation has been posted against this asset, its
        // financial fields are frozen — changing them retroactively would
        // desync the schedule already posted to the GL. Only descriptive
        // fields stay editable.
        boolean hasDepreciationHistory = depreciationEntryRepository.existsByAssetId(id);
        if (hasDepreciationHistory) {
            asset.setName(request.getName());
            asset.setDescription(request.getDescription());
            asset.setCategory(request.getCategory());
            return toResponse(fixedAssetRepository.save(asset));
        }

        if (request.getDepreciationMethod() == DepreciationMethod.DECLINING_BALANCE && request.getDecliningBalanceRate() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A declining balance rate is required for the declining balance method");
        }
        asset.setAssetCode(request.getAssetCode());
        asset.setName(request.getName());
        asset.setDescription(request.getDescription());
        asset.setCategory(request.getCategory());
        asset.setAcquisitionDate(request.getAcquisitionDate());
        asset.setAcquisitionCost(request.getAcquisitionCost());
        asset.setSalvageValue(request.getSalvageValue() == null ? BigDecimal.ZERO : request.getSalvageValue());
        asset.setUsefulLifeMonths(request.getUsefulLifeMonths());
        asset.setDepreciationMethod(request.getDepreciationMethod());
        asset.setDecliningBalanceRate(request.getDecliningBalanceRate());
        return toResponse(fixedAssetRepository.save(asset));
    }

    @Override
    @Transactional
    public void delete(Long id, String actingUsername) {
        FixedAsset asset = find(id);
        if (depreciationEntryRepository.existsByAssetId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a fixed asset that already has depreciation posted — dispose it instead");
        }
        if (asset.getStatus() == FixedAssetStatus.DISPOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a disposed fixed asset");
        }
        autoPostingService.reverseAutoEntry("FIXED_ASSET_ACQUISITION", id, actingUsername);
        fixedAssetRepository.delete(asset);
    }

    @Override
    @Transactional
    public FixedAssetResponse dispose(Long id, DisposeFixedAssetRequest request, String actingUsername) {
        FixedAsset asset = find(id);
        if (asset.getStatus() == FixedAssetStatus.DISPOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This asset has already been disposed");
        }
        asset.setDisposalDate(request.getDisposalDate());
        asset.setDisposalProceeds(request.getProceeds());
        asset.setStatus(FixedAssetStatus.DISPOSED);
        fixedAssetRepository.save(asset);
        autoPostingService.postAssetDisposal(asset, request.getProceeds(), actingUsername);
        return toResponse(asset);
    }

    @Override
    @Transactional
    public DepreciationRunResponse runDepreciation(RunDepreciationRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        AccountingPeriod period = accountingPeriodRepository.findById(request.getAccountingPeriodId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Accounting period not found with id: " + request.getAccountingPeriodId()));
        if (!period.getCompanyId().equals(request.getCompanyId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Accounting period does not belong to the selected company");
        }
        if (depreciationRunRepository.findByCompanyIdAndAccountingPeriodId(request.getCompanyId(), request.getAccountingPeriodId()).isPresent()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Depreciation has already been run for this period");
        }

        DepreciationRun run = DepreciationRun.builder()
                .companyId(request.getCompanyId())
                .accountingPeriodId(request.getAccountingPeriodId())
                .runDate(LocalDate.now())
                .createdBy(actingUsername)
                .build();
        depreciationRunRepository.save(run);

        List<FixedAsset> activeAssets = fixedAssetRepository.findByCompanyIdAndStatus(request.getCompanyId(), FixedAssetStatus.ACTIVE);
        List<DepreciationEntry> entries = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FixedAsset asset : activeAssets) {
            if (asset.getAcquisitionDate().isAfter(period.getEndDate())) continue;

            BigDecimal depreciableBase = asset.getAcquisitionCost().subtract(asset.getSalvageValue());
            BigDecimal remaining = depreciableBase.subtract(asset.getAccumulatedDepreciation());
            if (remaining.signum() <= 0) continue;

            BigDecimal amount = asset.getDepreciationMethod() == DepreciationMethod.STRAIGHT_LINE
                    ? depreciableBase.divide(BigDecimal.valueOf(asset.getUsefulLifeMonths()), 4, RoundingMode.HALF_UP)
                    : decliningBalanceMonthlyAmount(asset);
            if (amount.compareTo(remaining) > 0) amount = remaining;
            if (amount.signum() <= 0) continue;

            asset.setAccumulatedDepreciation(asset.getAccumulatedDepreciation().add(amount));
            if (asset.getAccumulatedDepreciation().compareTo(depreciableBase) >= 0) {
                asset.setStatus(FixedAssetStatus.FULLY_DEPRECIATED);
            }
            fixedAssetRepository.save(asset);

            DepreciationEntry entry = DepreciationEntry.builder()
                    .depreciationRunId(run.getId())
                    .companyId(asset.getCompanyId())
                    .assetId(asset.getId())
                    .amount(amount)
                    .accumulatedAfter(asset.getAccumulatedDepreciation())
                    .build();
            depreciationEntryRepository.save(entry);
            entries.add(entry);
            total = total.add(amount);
        }

        if (total.signum() > 0) {
            autoPostingService.postDepreciationRun(request.getCompanyId(), period.getEndDate(), total, run.getId(), actingUsername);
            journalEntryRepository.findBySourceTypeAndSourceId("DEPRECIATION_RUN", run.getId())
                    .ifPresent(je -> {
                        run.setJournalEntryId(je.getId());
                        depreciationRunRepository.save(run);
                    });
        }

        return toRunResponse(run, period, entries);
    }

    // Monthly amount = book value x (annual rate / 12), where the annual
    // rate is a plain percentage (e.g. 40 for double-declining on a 5-year
    // life). Capped by the caller against the asset's remaining depreciable
    // base.
    private BigDecimal decliningBalanceMonthlyAmount(FixedAsset asset) {
        BigDecimal bookValue = asset.getAcquisitionCost().subtract(asset.getAccumulatedDepreciation());
        BigDecimal monthlyRate = asset.getDecliningBalanceRate()
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        return bookValue.multiply(monthlyRate).setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepreciationEntryResponse> listDepreciationEntries(DepreciationEntryFilterRequest filter) {
        List<Specification<DepreciationEntry>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getAssetId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("assetId"), filter.getAssetId()));
        Specification<DepreciationEntry> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<DepreciationEntry> page = depreciationEntryRepository.findAll(spec, pageable);
        List<DepreciationEntry> content = page.getContent();

        Map<Long, FixedAsset> assets = content.isEmpty() ? Map.of() : fixedAssetRepository.findAllById(
                content.stream().map(DepreciationEntry::getAssetId).distinct().toList()
        ).stream().collect(Collectors.toMap(FixedAsset::getId, a -> a));
        Map<Long, DepreciationRun> runs = content.isEmpty() ? Map.of() : depreciationRunRepository.findAllById(
                content.stream().map(DepreciationEntry::getDepreciationRunId).distinct().toList()
        ).stream().collect(Collectors.toMap(DepreciationRun::getId, r -> r));
        Map<Long, AccountingPeriod> periods = runs.isEmpty() ? Map.of() : accountingPeriodRepository.findAllById(
                runs.values().stream().map(DepreciationRun::getAccountingPeriodId).distinct().toList()
        ).stream().collect(Collectors.toMap(AccountingPeriod::getId, p -> p));

        return PageResponse.of(page.map(entry -> {
            FixedAsset asset = assets.get(entry.getAssetId());
            DepreciationRun run = runs.get(entry.getDepreciationRunId());
            AccountingPeriod period = run == null ? null : periods.get(run.getAccountingPeriodId());
            return DepreciationEntryResponse.builder()
                    .id(entry.getId())
                    .depreciationRunId(entry.getDepreciationRunId())
                    .runDate(run == null ? null : run.getRunDate())
                    .accountingPeriodId(run == null ? null : run.getAccountingPeriodId())
                    .accountingPeriodName(period == null ? null : period.getName())
                    .assetId(entry.getAssetId())
                    .assetCode(asset == null ? null : asset.getAssetCode())
                    .assetName(asset == null ? null : asset.getName())
                    .amount(entry.getAmount())
                    .accumulatedAfter(entry.getAccumulatedAfter())
                    .build();
        }));
    }

    private DepreciationRunResponse toRunResponse(DepreciationRun run, AccountingPeriod period, List<DepreciationEntry> entries) {
        Map<Long, FixedAsset> assets = entries.isEmpty() ? Map.of() : fixedAssetRepository.findAllById(
                entries.stream().map(DepreciationEntry::getAssetId).distinct().toList()
        ).stream().collect(Collectors.toMap(FixedAsset::getId, a -> a));
        JournalEntry journalEntry = run.getJournalEntryId() == null ? null : journalEntryRepository.findById(run.getJournalEntryId()).orElse(null);

        List<DepreciationEntryResponse> entryResponses = entries.stream()
                .map(entry -> {
                    FixedAsset asset = assets.get(entry.getAssetId());
                    return DepreciationEntryResponse.builder()
                            .id(entry.getId())
                            .depreciationRunId(run.getId())
                            .runDate(run.getRunDate())
                            .accountingPeriodId(run.getAccountingPeriodId())
                            .accountingPeriodName(period.getName())
                            .assetId(entry.getAssetId())
                            .assetCode(asset == null ? null : asset.getAssetCode())
                            .assetName(asset == null ? null : asset.getName())
                            .amount(entry.getAmount())
                            .accumulatedAfter(entry.getAccumulatedAfter())
                            .build();
                })
                .toList();

        return DepreciationRunResponse.builder()
                .id(run.getId())
                .companyId(run.getCompanyId())
                .accountingPeriodId(run.getAccountingPeriodId())
                .accountingPeriodName(period.getName())
                .runDate(run.getRunDate())
                .journalEntryId(run.getJournalEntryId())
                .journalNumber(journalEntry == null ? null : journalEntry.getJournalNumber())
                .totalAmount(entries.stream().map(DepreciationEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .assetCount(entries.size())
                .createdBy(run.getCreatedBy())
                .entries(entryResponses)
                .build();
    }

    private FixedAssetResponse toResponse(FixedAsset asset) {
        Company company = companyRepository.findById(asset.getCompanyId()).orElse(null);
        return FixedAssetResponse.builder()
                .id(asset.getId())
                .companyId(asset.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .description(asset.getDescription())
                .category(asset.getCategory())
                .acquisitionDate(asset.getAcquisitionDate())
                .acquisitionCost(asset.getAcquisitionCost())
                .salvageValue(asset.getSalvageValue())
                .usefulLifeMonths(asset.getUsefulLifeMonths())
                .depreciationMethod(asset.getDepreciationMethod().name())
                .decliningBalanceRate(asset.getDecliningBalanceRate())
                .accumulatedDepreciation(asset.getAccumulatedDepreciation())
                .bookValue(asset.getAcquisitionCost().subtract(asset.getAccumulatedDepreciation()))
                .status(asset.getStatus().name())
                .disposalDate(asset.getDisposalDate())
                .disposalProceeds(asset.getDisposalProceeds())
                .build();
    }

    private FixedAsset find(Long id) {
        return fixedAssetRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Fixed asset not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
