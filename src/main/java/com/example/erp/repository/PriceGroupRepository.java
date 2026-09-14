package com.example.erp.repository;

import com.example.erp.entity.PriceGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PriceGroupRepository extends JpaRepository<PriceGroup, Long>, JpaSpecificationExecutor<PriceGroup> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
