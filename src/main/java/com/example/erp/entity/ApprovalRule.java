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

import java.math.BigDecimal;

// How many distinct people must approve a document before it actually
// finalizes — see ApprovalWorkflowService.recordApproval. Multiple rules
// per (companyId, documentType) at different minAmount thresholds are
// allowed; the strictest matching tier (highest minAmount <= the
// document's total) applies. No matching active rule at all means the
// original single-click behavior (1 approval finalizes immediately).
@Entity
@Table(name = "approval_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // "SALES_ORDER" | "PURCHASE_ORDER" — validated against a small
    // allow-list in ApprovalRuleServiceImpl, same convention as
    // AttachmentServiceImpl's polymorphic ownerType.
    @Column(name = "document_type", nullable = false)
    private String documentType;

    // Null means "applies regardless of amount."
    @Column(name = "min_amount", precision = 19, scale = 4)
    private BigDecimal minAmount;

    @Column(name = "required_approvals", nullable = false)
    private int requiredApprovals;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
