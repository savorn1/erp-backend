package com.example.erp.repository;

import com.example.erp.entity.PostingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostingRuleRepository extends JpaRepository<PostingRule, Long> {
    Optional<PostingRule> findByCompanyId(Long companyId);
}
