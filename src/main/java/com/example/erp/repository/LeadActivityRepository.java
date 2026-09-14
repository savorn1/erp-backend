package com.example.erp.repository;

import com.example.erp.entity.LeadActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadActivityRepository extends JpaRepository<LeadActivity, Long> {

    Page<LeadActivity> findByLeadId(Long leadId, Pageable pageable);
}
