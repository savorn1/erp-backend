package com.example.erp.repository;

import com.example.erp.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long>, JpaSpecificationExecutor<CommissionRule> {

    Optional<CommissionRule> findByCompanyIdAndUserIdAndActiveTrue(Long companyId, Long userId);

    Optional<CommissionRule> findByCompanyIdAndUserIdIsNullAndActiveTrue(Long companyId);

    boolean existsByCompanyIdAndUserId(Long companyId, Long userId);

    boolean existsByCompanyIdAndUserIdIsNull(Long companyId);

    boolean existsByCompanyIdAndUserIdAndIdNot(Long companyId, Long userId, Long id);

    boolean existsByCompanyIdAndUserIdIsNullAndIdNot(Long companyId, Long id);
}
