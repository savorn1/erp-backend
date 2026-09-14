package com.example.erp.repository;

import com.example.erp.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JournalRepository extends JpaRepository<Journal, Long>, JpaSpecificationExecutor<Journal> {
    boolean existsByCompanyIdAndCode(Long companyId, String code);
    boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);
}
