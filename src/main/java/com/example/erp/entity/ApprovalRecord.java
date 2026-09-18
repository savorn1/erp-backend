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

// One person's approval of one document instance — see
// ApprovalWorkflowService.recordApproval. A given (documentType,
// documentId, approvedBy) can only exist once: the same person can't
// supply two of an ApprovalRule's required distinct approvals on their own.
@Entity
@Table(name = "approval_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    // 1-based order this approval was recorded in.
    @Column(nullable = false)
    private int level;

    @Column(name = "approved_by", nullable = false)
    private String approvedBy;

    @CreationTimestamp
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
