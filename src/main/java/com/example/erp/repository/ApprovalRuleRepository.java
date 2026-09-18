package com.example.erp.repository;

import com.example.erp.entity.ApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, Long>, JpaSpecificationExecutor<ApprovalRule> {

    List<ApprovalRule> findByCompanyIdAndDocumentTypeAndActiveTrue(Long companyId, String documentType);
}
