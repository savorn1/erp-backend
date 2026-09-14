package com.example.erp.repository;

import com.example.erp.entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerTypeRepository extends JpaRepository<CustomerType, Long>, JpaSpecificationExecutor<CustomerType> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
