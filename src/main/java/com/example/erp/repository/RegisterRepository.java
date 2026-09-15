package com.example.erp.repository;

import com.example.erp.entity.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegisterRepository extends JpaRepository<Register, Long>, JpaSpecificationExecutor<Register> {
    boolean existsByCompanyIdAndCode(Long companyId, String code);
    boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);
}
