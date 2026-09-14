package com.example.erp.service.impl;

import com.example.erp.dto.JournalEntryFilterRequest;
import com.example.erp.dto.JournalEntryLineRequest;
import com.example.erp.dto.JournalEntryLineResponse;
import com.example.erp.dto.JournalEntryRequest;
import com.example.erp.dto.JournalEntryResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Account;
import com.example.erp.entity.Company;
import com.example.erp.entity.JournalEntry;
import com.example.erp.entity.JournalEntryLine;
import com.example.erp.entity.JournalEntryStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AccountRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.JournalEntryLineRepository;
import com.example.erp.repository.JournalEntryRepository;
import com.example.erp.service.JournalEntryService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalEntryResponse> list(JournalEntryFilterRequest filter) {
        List<Specification<JournalEntry>> conditions = new ArrayList<>();
        if (filter.getJournalNumber() != null && !filter.getJournalNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("journalNumber")), "%" + filter.getJournalNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getAccountId() != null) {
            List<Long> journalEntryIds = journalEntryLineRepository.findByAccountId(filter.getAccountId()).stream()
                    .map(JournalEntryLine::getJournalEntryId).distinct().toList();
            List<Long> safeIds = journalEntryIds.isEmpty() ? List.of(-1L) : journalEntryIds;
            conditions.add((root, query, cb) -> root.get("id").in(safeIds));
        }
        Specification<JournalEntry> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<JournalEntry> page = journalEntryRepository.findAll(spec, pageable);
        List<Long> ids = page.getContent().stream().map(JournalEntry::getId).toList();
        Map<Long, List<JournalEntryLine>> linesByEntryId = ids.isEmpty() ? Map.of()
                : journalEntryLineRepository.findByJournalEntryIdIn(ids).stream().collect(Collectors.groupingBy(JournalEntryLine::getJournalEntryId));

        return PageResponse.of(page.map(e -> toResponse(e, linesByEntryId.getOrDefault(e.getId(), List.of()))));
    }

    @Override
    public JournalEntryResponse get(Long id) {
        JournalEntry entry = find(id);
        return toResponse(entry, journalEntryLineRepository.findByJournalEntryId(id));
    }

    @Override
    @Transactional
    public JournalEntryResponse create(JournalEntryRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        validateLines(request.getLines(), request.getCompanyId());

        JournalEntry entry = JournalEntry.builder()
                .companyId(request.getCompanyId())
                .entryDate(request.getEntryDate())
                .description(request.getDescription())
                .createdBy(actingUsername)
                .build();
        journalEntryRepository.save(entry);
        entry.setJournalNumber("JE-" + String.format("%06d", entry.getId()));
        journalEntryRepository.save(entry);

        saveLines(entry.getId(), request.getLines());
        return get(entry.getId());
    }

    @Override
    @Transactional
    public JournalEntryResponse update(Long id, JournalEntryRequest request) {
        JournalEntry entry = find(id);
        if (entry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft journal entries can be edited");
        }
        requireCompany(request.getCompanyId());
        validateLines(request.getLines(), request.getCompanyId());

        entry.setCompanyId(request.getCompanyId());
        entry.setEntryDate(request.getEntryDate());
        entry.setDescription(request.getDescription());
        journalEntryRepository.save(entry);

        journalEntryLineRepository.deleteByJournalEntryId(id);
        saveLines(id, request.getLines());
        return get(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        JournalEntry entry = find(id);
        if (entry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft journal entries can be deleted");
        }
        journalEntryLineRepository.deleteByJournalEntryId(id);
        journalEntryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JournalEntryResponse post(Long id, String actingUsername) {
        JournalEntry entry = find(id);
        if (entry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft journal entries can be posted");
        }
        List<JournalEntryLine> lines = journalEntryLineRepository.findByJournalEntryId(id);
        if (lines.size() < 2) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A journal entry needs at least two lines before it can be posted");
        }
        requireBalanced(lines);

        entry.setStatus(JournalEntryStatus.POSTED);
        entry.setPostedBy(actingUsername);
        entry.setPostedAt(LocalDateTime.now());
        journalEntryRepository.save(entry);
        return get(id);
    }

    @Override
    @Transactional
    public JournalEntryResponse reverse(Long id, String actingUsername) {
        JournalEntry original = find(id);
        if (original.getStatus() != JournalEntryStatus.POSTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a posted journal entry can be reversed");
        }
        if (original.getReversedByJournalEntryId() != null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This journal entry has already been reversed");
        }
        List<JournalEntryLine> originalLines = journalEntryLineRepository.findByJournalEntryId(id);

        JournalEntry reversal = JournalEntry.builder()
                .companyId(original.getCompanyId())
                .entryDate(LocalDate.now())
                .description("Reversal of " + original.getJournalNumber())
                .reversalOfJournalEntryId(original.getId())
                .status(JournalEntryStatus.POSTED)
                .createdBy(actingUsername)
                .postedBy(actingUsername)
                .postedAt(LocalDateTime.now())
                .build();
        journalEntryRepository.save(reversal);
        reversal.setJournalNumber("JE-" + String.format("%06d", reversal.getId()));
        journalEntryRepository.save(reversal);

        for (JournalEntryLine line : originalLines) {
            journalEntryLineRepository.save(JournalEntryLine.builder()
                    .journalEntryId(reversal.getId())
                    .accountId(line.getAccountId())
                    .debit(line.getCredit())
                    .credit(line.getDebit())
                    .description(line.getDescription())
                    .build());
        }

        original.setReversedByJournalEntryId(reversal.getId());
        journalEntryRepository.save(original);

        return get(reversal.getId());
    }

    private void validateLines(List<JournalEntryLineRequest> lines, Long companyId) {
        if (lines == null || lines.size() < 2) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A journal entry needs at least two lines");
        }
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalEntryLineRequest line : lines) {
            boolean hasDebit = line.getDebit() != null && line.getDebit().compareTo(BigDecimal.ZERO) > 0;
            boolean hasCredit = line.getCredit() != null && line.getCredit().compareTo(BigDecimal.ZERO) > 0;
            if (hasDebit == hasCredit) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Each line needs exactly one of debit or credit greater than zero");
            }
            Account account = accountRepository.findById(line.getAccountId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Account not found with id: " + line.getAccountId()));
            if (!account.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Account " + account.getAccountCode() + " does not belong to the selected company");
            }
            totalDebit = totalDebit.add(line.getDebit());
            totalCredit = totalCredit.add(line.getCredit());
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Journal entry is not balanced — total debit (" + totalDebit + ") must equal total credit (" + totalCredit + ")");
        }
    }

    private void requireBalanced(List<JournalEntryLine> lines) {
        BigDecimal totalDebit = lines.stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Journal entry is not balanced — total debit (" + totalDebit + ") must equal total credit (" + totalCredit + ")");
        }
    }

    private void saveLines(Long journalEntryId, List<JournalEntryLineRequest> lines) {
        for (JournalEntryLineRequest line : lines) {
            journalEntryLineRepository.save(JournalEntryLine.builder()
                    .journalEntryId(journalEntryId)
                    .accountId(line.getAccountId())
                    .debit(line.getDebit())
                    .credit(line.getCredit())
                    .description(line.getDescription())
                    .build());
        }
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private JournalEntry find(Long id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Journal entry not found with id: " + id));
    }

    private JournalEntryResponse toResponse(JournalEntry entry, List<JournalEntryLine> lines) {
        String companyName = companyRepository.findById(entry.getCompanyId()).map(Company::getName).orElse(null);
        String reversalOfNumber = entry.getReversalOfJournalEntryId() == null ? null
                : journalEntryRepository.findById(entry.getReversalOfJournalEntryId()).map(JournalEntry::getJournalNumber).orElse(null);
        String reversedByNumber = entry.getReversedByJournalEntryId() == null ? null
                : journalEntryRepository.findById(entry.getReversedByJournalEntryId()).map(JournalEntry::getJournalNumber).orElse(null);

        Map<Long, Account> accounts = lines.isEmpty() ? Map.of() : accountRepository.findAllById(
                lines.stream().map(JournalEntryLine::getAccountId).distinct().toList()
        ).stream().collect(Collectors.toMap(Account::getId, a -> a));

        List<JournalEntryLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Account account = accounts.get(line.getAccountId());
                    return JournalEntryLineResponse.builder()
                            .id(line.getId())
                            .accountId(line.getAccountId())
                            .accountCode(account == null ? null : account.getAccountCode())
                            .accountName(account == null ? null : account.getName())
                            .debit(line.getDebit())
                            .credit(line.getCredit())
                            .description(line.getDescription())
                            .build();
                })
                .toList();

        BigDecimal totalDebit = lines.stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);

        return JournalEntryResponse.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .companyName(companyName)
                .journalNumber(entry.getJournalNumber())
                .entryDate(entry.getEntryDate())
                .description(entry.getDescription())
                .status(entry.getStatus().name())
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .reversalOfJournalEntryId(entry.getReversalOfJournalEntryId())
                .reversalOfJournalNumber(reversalOfNumber)
                .reversedByJournalEntryId(entry.getReversedByJournalEntryId())
                .reversedByJournalNumber(reversedByNumber)
                .createdBy(entry.getCreatedBy())
                .createdAt(entry.getCreatedAt())
                .postedBy(entry.getPostedBy())
                .postedAt(entry.getPostedAt())
                .lines(lineResponses)
                .build();
    }
}
