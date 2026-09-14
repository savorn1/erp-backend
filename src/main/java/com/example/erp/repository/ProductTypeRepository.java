package com.example.erp.repository;

import com.example.erp.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long>, JpaSpecificationExecutor<ProductType> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
