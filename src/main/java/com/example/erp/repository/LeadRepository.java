package com.example.erp.repository;

import com.example.erp.entity.Lead;
import com.example.erp.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    List<Lead> findByStatusNotIn(List<LeadStatus> statuses);
}
