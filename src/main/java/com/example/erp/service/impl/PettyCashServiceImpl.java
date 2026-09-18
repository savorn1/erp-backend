package com.example.erp.service.impl;

import com.example.erp.dto.CreatePettyCashEntryRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PettyCashEntryFilterRequest;
import com.example.erp.dto.PettyCashEntryResponse;
import com.example.erp.dto.PettyCashSummaryResponse;
import com.example.erp.entity.Account;
import com.example.erp.entity.AccountType;
import com.example.erp.entity.Company;
import com.example.erp.entity.PettyCashEntry;
import com.example.erp.entity.PettyCashEntryType;
import com.example.erp.entity.PostingRule;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.PettyCashEntryRepository;
import com.example.erp.repository.PostingRuleRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PettyCashService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PettyCashServiceImpl implements PettyCashService {

    private final PettyCashEntryRepository pettyCashEntryRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final PostingRuleRepository postingRuleRepository;
    private final AutoPostingService autoPostingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PettyCashEntryResponse> listEntries(PettyCashEntryFilterRequest filter) {
        List<Specification<PettyCashEntry>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getEntryDateFrom() != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("entryDate"), filter.getEntryDateFrom()));
        }
        if (filter.getEntryDateTo() != null) {
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("entryDate"), filter.getEntryDateTo()));
        }
        Specification<PettyCashEntry> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PettyCashEntry> page = pettyCashEntryRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public PettyCashEntryResponse getEntry(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PettyCashEntryResponse createEntry(CreatePettyCashEntryRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + request.getAccountId()));
        if (!account.getCompanyId().equals(request.getCompanyId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Account " + account.getAccountCode() + " does not belong to the selected company");
        }
        AccountType expectedType = request.getType() == PettyCashEntryType.TOPUP ? AccountType.ASSET : AccountType.EXPENSE;
        if (account.getAccountType() != expectedType) {
            throw new AppException(HttpStatus.BAD_REQUEST, request.getType() == PettyCashEntryType.TOPUP
                    ? "A top-up's source must be an asset (cash/bank) account"
                    : "An expense entry must charge an expense account");
        }

        PostingRule rule = postingRuleRepository.findByCompanyId(request.getCompanyId()).orElse(null);
        if (rule != null && rule.getPettyCashAccountId() != null && rule.getPettyCashAccountId().equals(request.getAccountId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "The counter account cannot be the petty cash account itself");
        }

        PettyCashEntry entry = PettyCashEntry.builder()
                .companyId(request.getCompanyId())
                .type(request.getType())
                .entryDate(request.getEntryDate())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .createdBy(actingUsername)
                .build();
        pettyCashEntryRepository.save(entry);
        String prefix = request.getType() == PettyCashEntryType.TOPUP ? "PCT-" : "PCE-";
        entry.setEntryNumber(prefix + String.format("%06d", entry.getId()));
        pettyCashEntryRepository.save(entry);

        autoPostingService.postPettyCashEntry(entry, actingUsername);

        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PettyCashSummaryResponse getSummary(Long companyId) {
        requireCompany(companyId);
        BigDecimal toppedUp = pettyCashEntryRepository.sumAmount(companyId, PettyCashEntryType.TOPUP);
        BigDecimal expensed = pettyCashEntryRepository.sumAmount(companyId, PettyCashEntryType.EXPENSE);
        return PettyCashSummaryResponse.builder()
                .companyId(companyId)
                .toppedUp(toppedUp)
                .expensed(expensed)
                .balance(toppedUp.subtract(expensed))
                .build();
    }

    private PettyCashEntry find(Long id) {
        return pettyCashEntryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Petty cash entry not found with id: " + id));
    }

    private PettyCashEntryResponse toResponse(PettyCashEntry entry) {
        Company company = companyRepository.findById(entry.getCompanyId()).orElse(null);
        Account account = accountRepository.findById(entry.getAccountId()).orElse(null);
        String accountLabel = account == null ? null : account.getAccountCode() + " — " + account.getName();

        return PettyCashEntryResponse.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .type(entry.getType())
                .entryNumber(entry.getEntryNumber())
                .entryDate(entry.getEntryDate())
                .accountId(entry.getAccountId())
                .accountLabel(accountLabel)
                .amount(entry.getAmount())
                .description(entry.getDescription())
                .createdBy(entry.getCreatedBy())
                .build();
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
