package com.example.erp.service.impl;

import com.example.erp.dto.JournalFilterRequest;
import com.example.erp.dto.JournalRequest;
import com.example.erp.dto.JournalResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.Journal;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.JournalEntryRepository;
import com.example.erp.repository.JournalRepository;
import com.example.erp.service.JournalService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;
    private final CompanyRepository companyRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> list(JournalFilterRequest filter) {
        List<Specification<Journal>> conditions = new ArrayList<>();
        if (filter.getCode() != null && !filter.getCode().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getActive() != null) conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        Specification<Journal> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Journal> page = journalRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public JournalResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public JournalResponse create(JournalRequest request) {
        requireCompany(request.getCompanyId());
        if (journalRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A journal with code '" + request.getCode() + "' already exists for this company");
        }
        Journal journal = Journal.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        return toResponse(journalRepository.save(journal));
    }

    @Override
    @Transactional
    public JournalResponse update(Long id, JournalRequest request) {
        Journal journal = find(id);
        if (journalRepository.existsByCompanyIdAndCodeAndIdNot(request.getCompanyId(), request.getCode(), id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A journal with code '" + request.getCode() + "' already exists for this company");
        }
        journal.setCode(request.getCode());
        journal.setName(request.getName());
        journal.setDescription(request.getDescription());
        journal.setActive(request.isActive());
        return toResponse(journalRepository.save(journal));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Journal journal = find(id);
        if (journalEntryRepository.exists((root, query, cb) -> cb.equal(root.get("journalId"), id))) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a journal that already has journal entries filed under it");
        }
        journalRepository.delete(journal);
    }

    private JournalResponse toResponse(Journal journal) {
        Company company = companyRepository.findById(journal.getCompanyId()).orElse(null);
        return JournalResponse.builder()
                .id(journal.getId())
                .companyId(journal.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .code(journal.getCode())
                .name(journal.getName())
                .description(journal.getDescription())
                .active(journal.isActive())
                .build();
    }

    private Journal find(Long id) {
        return journalRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Journal not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
