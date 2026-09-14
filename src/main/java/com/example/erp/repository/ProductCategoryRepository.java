package com.example.erp.repository;

import com.example.erp.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
