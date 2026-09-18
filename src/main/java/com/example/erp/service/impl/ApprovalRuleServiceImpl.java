package com.example.erp.service.impl;

import com.example.erp.dto.ApprovalRuleFilterRequest;
import com.example.erp.dto.ApprovalRuleRequest;
import com.example.erp.dto.ApprovalRuleResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.ApprovalRule;
import com.example.erp.entity.Company;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ApprovalRuleRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.service.ApprovalRuleService;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApprovalRuleServiceImpl implements ApprovalRuleService {

    private static final Set<String> VALID_DOCUMENT_TYPES = Set.of("SALES_ORDER", "PURCHASE_ORDER");

    private final ApprovalRuleRepository approvalRuleRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ApprovalRuleResponse> listRules(ApprovalRuleFilterRequest filter) {
        List<Specification<ApprovalRule>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getDocumentType() != null && !filter.getDocumentType().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("documentType"), filter.getDocumentType()));
        }
        Specification<ApprovalRule> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<ApprovalRule> page = approvalRuleRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public ApprovalRuleResponse createRule(ApprovalRuleRequest request) {
        requireCompany(request.getCompanyId());
        requireValidDocumentType(request.getDocumentType());

        ApprovalRule rule = ApprovalRule.builder()
                .companyId(request.getCompanyId())
                .documentType(request.getDocumentType())
                .minAmount(request.getMinAmount())
                .requiredApprovals(request.getRequiredApprovals())
                .active(request.isActive())
                .build();
        approvalRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Override
    @Transactional
    public ApprovalRuleResponse updateRule(Long id, ApprovalRuleRequest request) {
        ApprovalRule rule = find(id);
        requireValidDocumentType(request.getDocumentType());
        rule.setDocumentType(request.getDocumentType());
        rule.setMinAmount(request.getMinAmount());
        rule.setRequiredApprovals(request.getRequiredApprovals());
        rule.setActive(request.isActive());
        approvalRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        ApprovalRule rule = find(id);
        approvalRuleRepository.delete(rule);
    }

    private void requireValidDocumentType(String documentType) {
        if (!VALID_DOCUMENT_TYPES.contains(documentType)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Unsupported document type: " + documentType);
        }
    }

    private ApprovalRule find(Long id) {
        return approvalRuleRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Approval rule not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private ApprovalRuleResponse toResponse(ApprovalRule rule) {
        String companyName = companyRepository.findById(rule.getCompanyId()).map(Company::getName).orElse(null);
        return ApprovalRuleResponse.builder()
                .id(rule.getId())
                .companyId(rule.getCompanyId())
                .companyName(companyName)
                .documentType(rule.getDocumentType())
                .minAmount(rule.getMinAmount())
                .requiredApprovals(rule.getRequiredApprovals())
                .active(rule.isActive())
                .build();
    }
}
