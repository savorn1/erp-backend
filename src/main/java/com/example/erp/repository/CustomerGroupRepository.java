package com.example.erp.repository;

import com.example.erp.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long>, JpaSpecificationExecutor<CustomerGroup> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<CustomerGroup> findByNameIgnoreCase(String name);
}
