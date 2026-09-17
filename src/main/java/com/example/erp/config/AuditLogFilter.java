package com.example.erp.config;

import com.example.erp.entity.PermissionAction;
import com.example.erp.security.RequestModuleAction;
import com.example.erp.service.AuditLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

// Not a @Component — same reason JwtAuthenticationFilter isn't one:
// registering a Filter bean lets Spring Boot's own servlet-filter
// auto-registration add it a second time outside the Security chain,
// running it twice per request. Constructed manually in SecurityConfig
// instead, positioned after JwtAuthenticationFilter so
// SecurityContextHolder is already populated by the time this runs.
//
// Only observes: wraps the response so the body can be read after the real
// handler has already written it, then restores it unchanged via
// copyBodyToResponse() in a finally block — this can never alter what the
// client receives or affect the wrapped request's own transaction.
//
// Scope: only requests already classified APPROVE, plus WRITE requests to
// the users/custom-roles modules — see RequestModuleAction for the
// module/action vocabulary this reuses from PermissionAuthorizationManager.
// Login/logout are outside /api/admin/** entirely and are recorded directly
// by AuthServiceImpl instead.
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditLogFilter(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().contains("/api/admin/")) {
            chain.doFilter(request, response);
            return;
        }
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrappedResponse);
        } finally {
            recordIfNeeded(request, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void recordIfNeeded(HttpServletRequest request, ContentCachingResponseWrapper response) {
        String module = RequestModuleAction.moduleOf(request.getRequestURI(), request.getContextPath());
        if (module == null) return;
        PermissionAction action = RequestModuleAction.actionOf(request);
        boolean isSecurityModuleWrite = (module.equals("users") || module.equals("custom-roles")) && action == PermissionAction.WRITE;
        if (action != PermissionAction.APPROVE && !isSecurityModuleWrite) return;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actingUsername = authentication == null ? null : authentication.getName();
        JsonNode data = readResponseData(response);
        Long sourceId = numberField(data, "id");
        Long companyId = numberField(data, "companyId");

        auditLogService.record(companyId, module, action.name(), request.getMethod(), request.getRequestURI(),
                sourceId, actingUsername, response.getStatus(), request.getMethod() + " " + request.getRequestURI());
    }

    private JsonNode readResponseData(ContentCachingResponseWrapper response) {
        try {
            byte[] body = response.getContentAsByteArray();
            if (body.length == 0) return null;
            return objectMapper.readTree(body).path("data");
        } catch (Exception e) {
            return null;
        }
    }

    private Long numberField(JsonNode data, String field) {
        if (data == null) return null;
        JsonNode node = data.path(field);
        return node.isNumber() ? node.asLong() : null;
    }
}
