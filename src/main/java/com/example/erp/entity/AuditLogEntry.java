package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// One recorded action of real compliance interest — see AuditLogFilter for
// exactly what qualifies (approvals, users/custom-roles writes) and
// AuthServiceImpl for login/logout, which are recorded directly since they
// sit outside /api/admin/** entirely. Append-only, same convention as
// JournalEntry: plain FK columns, no JPA relationships, never updated or
// deleted after creation.
@Entity
@Table(name = "audit_log_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable — login/logout aren't scoped to a company.
    @Column(name = "company_id")
    private Long companyId;

    // Same vocabulary RequestModuleAction derives for permission checks —
    // null for login/logout.
    private String module;

    // "APPROVE" / "WRITE" (from PermissionAction) or "LOGIN" / "LOGOUT" —
    // a plain String rather than the PermissionAction enum since login/logout
    // don't fit that enum's meaning.
    @Column(nullable = false)
    private String action;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(columnDefinition = "text")
    private String path;

    // Best-effort — parsed from the response body's `data.id` when present
    // and numeric. Null whenever that doesn't apply (login/logout, a list
    // endpoint, a non-JSON/unparseable body).
    @Column(name = "source_id")
    private Long sourceId;

    // Nullable — a failed-login attempt has no authenticated user yet.
    @Column(name = "acting_username")
    private String actingUsername;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
