package com.example.erp.service.impl;

import com.example.erp.dto.AuditLogFilterRequest;
import com.example.erp.dto.AuditLogResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.AuditLogEntry;
import com.example.erp.entity.Company;
import com.example.erp.repository.AuditLogRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.service.AuditLogService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(AuditLogFilterRequest filter) {
        List<Specification<AuditLogEntry>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getModule() != null && !filter.getModule().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("module"), filter.getModule()));
        }
        if (filter.getAction() != null && !filter.getAction().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("action"), filter.getAction()));
        }
        if (filter.getActingUsername() != null && !filter.getActingUsername().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("actingUsername")), "%" + filter.getActingUsername().toLowerCase() + "%"));
        }
        if (filter.getDateFrom() != null) {
            LocalDateTime from = filter.getDateFrom().atStartOfDay();
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (filter.getDateTo() != null) {
            LocalDateTime to = filter.getDateTo().atTime(23, 59, 59);
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        Specification<AuditLogEntry> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<AuditLogEntry> page = auditLogRepository.findAll(spec, pageable);
        List<AuditLogEntry> content = page.getContent();
        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(AuditLogEntry::getCompanyId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));

        return PageResponse.of(page.map(entry -> toResponse(entry, companyNames.get(entry.getCompanyId()))));
    }

    @Override
    @Transactional
    public void record(Long companyId, String module, String action, String httpMethod, String path,
                        Long sourceId, String actingUsername, Integer statusCode, String description) {
        try {
            auditLogRepository.save(AuditLogEntry.builder()
                    .companyId(companyId)
                    .module(module)
                    .action(action)
                    .httpMethod(httpMethod)
                    .path(path)
                    .sourceId(sourceId)
                    .actingUsername(actingUsername)
                    .statusCode(statusCode)
                    .description(description)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to record audit log entry for {} {}: {}", httpMethod, path, e.getMessage());
        }
    }

    private AuditLogResponse toResponse(AuditLogEntry entry, String companyName) {
        return AuditLogResponse.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .companyName(companyName)
                .module(entry.getModule())
                .action(entry.getAction())
                .httpMethod(entry.getHttpMethod())
                .path(entry.getPath())
                .sourceId(entry.getSourceId())
                .actingUsername(entry.getActingUsername())
                .statusCode(entry.getStatusCode())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
