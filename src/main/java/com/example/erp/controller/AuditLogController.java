package com.example.erp.controller;

import com.example.erp.dto.AuditLogFilterRequest;
import com.example.erp.dto.AuditLogResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only — entries are only ever written by AuditLogFilter/AuthServiceImpl.
@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>> list(@ModelAttribute AuditLogFilterRequest filter) {
        return ResponseEntity.ok(auditLogService.list(filter));
    }
}
