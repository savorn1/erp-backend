package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TaxRateFilterRequest;
import com.example.erp.dto.TaxRateRequest;
import com.example.erp.dto.TaxRateResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.TaxRate;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.TaxRateRepository;
import com.example.erp.service.TaxRateService;
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
public class TaxRateServiceImpl implements TaxRateService {

    private final TaxRateRepository taxRateRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaxRateResponse> list(TaxRateFilterRequest filter) {
        List<Specification<TaxRate>> conditions = new ArrayList<>();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("code")), like), cb.like(cb.lower(root.get("name")), like)));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<TaxRate> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<TaxRate> page = taxRateRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public TaxRateResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public TaxRateResponse create(TaxRateRequest request) {
        requireCompany(request.getCompanyId());
        if (taxRateRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Tax rate code already taken in this company: " + request.getCode());
        }
        TaxRate taxRate = TaxRate.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .ratePercent(request.getRatePercent())
                .active(request.isActive())
                .build();
        taxRateRepository.save(taxRate);
        return toResponse(taxRate);
    }

    @Override
    @Transactional
    public TaxRateResponse update(Long id, TaxRateRequest request) {
        TaxRate taxRate = find(id);
        requireCompany(request.getCompanyId());
        if (taxRateRepository.existsByCompanyIdAndCodeAndIdNot(request.getCompanyId(), request.getCode(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Tax rate code already taken in this company: " + request.getCode());
        }
        taxRate.setCompanyId(request.getCompanyId());
        taxRate.setCode(request.getCode());
        taxRate.setName(request.getName());
        taxRate.setType(request.getType());
        taxRate.setRatePercent(request.getRatePercent());
        taxRate.setActive(request.isActive());
        taxRateRepository.save(taxRate);
        return toResponse(taxRate);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        taxRateRepository.delete(find(id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private TaxRate find(Long id) {
        return taxRateRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Tax rate not found with id: " + id));
    }

    private TaxRateResponse toResponse(TaxRate taxRate) {
        String companyName = companyRepository.findById(taxRate.getCompanyId()).map(Company::getName).orElse(null);
        return TaxRateResponse.builder()
                .id(taxRate.getId())
                .companyId(taxRate.getCompanyId())
                .companyName(companyName)
                .code(taxRate.getCode())
                .name(taxRate.getName())
                .type(taxRate.getType().name())
                .ratePercent(taxRate.getRatePercent())
                .active(taxRate.isActive())
                .build();
    }
}
