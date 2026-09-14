package com.example.erp.service.impl;

import com.example.erp.dto.CostCenterFilterRequest;
import com.example.erp.dto.CostCenterRequest;
import com.example.erp.dto.CostCenterResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.CostCenter;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CostCenterRepository;
import com.example.erp.repository.JournalEntryLineRepository;
import com.example.erp.service.CostCenterService;
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
public class CostCenterServiceImpl implements CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final CompanyRepository companyRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CostCenterResponse> list(CostCenterFilterRequest filter) {
        List<Specification<CostCenter>> conditions = new ArrayList<>();
        if (filter.getCode() != null && !filter.getCode().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getActive() != null) conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        Specification<CostCenter> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CostCenter> page = costCenterRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CostCenterResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public CostCenterResponse create(CostCenterRequest request) {
        requireCompany(request.getCompanyId());
        if (costCenterRepository.existsByCompanyIdAndCode(request.getCompanyId(), request.getCode())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A cost center with code '" + request.getCode() + "' already exists for this company");
        }
        CostCenter costCenter = CostCenter.builder()
                .companyId(request.getCompanyId())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        return toResponse(costCenterRepository.save(costCenter));
    }

    @Override
    @Transactional
    public CostCenterResponse update(Long id, CostCenterRequest request) {
        CostCenter costCenter = find(id);
        if (costCenterRepository.existsByCompanyIdAndCodeAndIdNot(request.getCompanyId(), request.getCode(), id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A cost center with code '" + request.getCode() + "' already exists for this company");
        }
        costCenter.setCode(request.getCode());
        costCenter.setName(request.getName());
        costCenter.setDescription(request.getDescription());
        costCenter.setActive(request.isActive());
        return toResponse(costCenterRepository.save(costCenter));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CostCenter costCenter = find(id);
        if (journalEntryLineRepository.existsByCostCenterId(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete a cost center that already has journal entry lines tagged with it");
        }
        costCenterRepository.delete(costCenter);
    }

    private CostCenterResponse toResponse(CostCenter costCenter) {
        Company company = companyRepository.findById(costCenter.getCompanyId()).orElse(null);
        return CostCenterResponse.builder()
                .id(costCenter.getId())
                .companyId(costCenter.getCompanyId())
                .companyName(company == null ? null : company.getName())
                .code(costCenter.getCode())
                .name(costCenter.getName())
                .description(costCenter.getDescription())
                .active(costCenter.isActive())
                .build();
    }

    private CostCenter find(Long id) {
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Cost center not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }
}
