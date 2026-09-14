package com.example.erp.repository;

import com.example.erp.entity.OpportunityActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunityActivityRepository extends JpaRepository<OpportunityActivity, Long> {

    Page<OpportunityActivity> findByOpportunityId(Long opportunityId, Pageable pageable);
}
