package com.example.erp.service;

import com.example.erp.dto.AuditLogFilterRequest;
import com.example.erp.dto.AuditLogResponse;
import com.example.erp.dto.PageResponse;

public interface AuditLogService {

    PageResponse<AuditLogResponse> list(AuditLogFilterRequest filter);

    // Never throws — a logging failure must never surface to the caller
    // (AuditLogFilter runs after the real response is already produced;
    // AuthServiceImpl calls this from the middle of a real login/logout).
    void record(Long companyId, String module, String action, String httpMethod, String path,
                Long sourceId, String actingUsername, Integer statusCode, String description);
}
