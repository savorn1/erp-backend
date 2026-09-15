package com.example.erp.repository;

import com.example.erp.entity.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomRoleRepository extends JpaRepository<CustomRole, Long>, JpaSpecificationExecutor<CustomRole> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
