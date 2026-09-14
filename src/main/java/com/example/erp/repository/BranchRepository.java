package com.example.erp.repository;

import com.example.erp.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
