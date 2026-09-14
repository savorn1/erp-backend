package com.example.erp.repository;

import com.example.erp.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
