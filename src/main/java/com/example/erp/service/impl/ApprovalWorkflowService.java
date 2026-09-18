package com.example.erp.service.impl;

import com.example.erp.entity.ApprovalRecord;
import com.example.erp.entity.ApprovalRule;
import com.example.erp.exception.AppException;
import com.example.erp.repository.ApprovalRecordRepository;
import com.example.erp.repository.ApprovalRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

// Not controller-facing — shared by SalesOrderServiceImpl/
// PurchaseOrderServiceImpl so a document under a multi-level ApprovalRule
// stays in its existing SUBMITTED status (no new intermediate status
// anywhere) until enough distinct people have approved it. With no
// matching active rule, requiredApprovals defaults to 1 — the exact
// single-click behavior every document had before this feature existed.
@Component
@RequiredArgsConstructor
public class ApprovalWorkflowService {

    private final ApprovalRuleRepository approvalRuleRepository;
    private final ApprovalRecordRepository approvalRecordRepository;

    public record ApprovalOutcome(boolean finalized, int approvalsRecorded, int approvalsRequired) {
    }

    public ApprovalOutcome recordApproval(Long companyId, String documentType, Long documentId,
                                           BigDecimal documentAmount, String actingUsername) {
        int required = resolveRequiredApprovals(companyId, documentType, documentAmount);
        if (approvalRecordRepository.existsByDocumentTypeAndDocumentIdAndApprovedBy(documentType, documentId, actingUsername)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "You have already approved this document");
        }
        int existing = approvalRecordRepository.findByDocumentTypeAndDocumentIdOrderByLevelAsc(documentType, documentId).size();
        approvalRecordRepository.save(ApprovalRecord.builder()
                .companyId(companyId)
                .documentType(documentType)
                .documentId(documentId)
                .level(existing + 1)
                .approvedBy(actingUsername)
                .build());
        int recorded = existing + 1;
        return new ApprovalOutcome(recorded >= required, recorded, required);
    }

    // Read-only view of where a document stands — used by
    // SalesOrderResponse/PurchaseOrderResponse to show "1 of 2 approvals"
    // without recording anything.
    public int requiredApprovals(Long companyId, String documentType, BigDecimal documentAmount) {
        return resolveRequiredApprovals(companyId, documentType, documentAmount);
    }

    public int approvalsRecorded(String documentType, Long documentId) {
        return approvalRecordRepository.findByDocumentTypeAndDocumentIdOrderByLevelAsc(documentType, documentId).size();
    }

    public void clearApprovals(String documentType, Long documentId) {
        approvalRecordRepository.deleteByDocumentTypeAndDocumentId(documentType, documentId);
    }

    private int resolveRequiredApprovals(Long companyId, String documentType, BigDecimal documentAmount) {
        List<ApprovalRule> rules = approvalRuleRepository.findByCompanyIdAndDocumentTypeAndActiveTrue(companyId, documentType);
        BigDecimal amount = documentAmount == null ? BigDecimal.ZERO : documentAmount;
        return rules.stream()
                .filter(r -> r.getMinAmount() == null || amount.compareTo(r.getMinAmount()) >= 0)
                .max(Comparator.comparing(r -> r.getMinAmount() == null ? BigDecimal.valueOf(-1) : r.getMinAmount()))
                .map(ApprovalRule::getRequiredApprovals)
                .orElse(1);
    }
}
