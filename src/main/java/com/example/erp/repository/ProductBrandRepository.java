package com.example.erp.repository;

import com.example.erp.entity.ProductBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductBrandRepository extends JpaRepository<ProductBrand, Long>, JpaSpecificationExecutor<ProductBrand> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<ProductBrand> findByNameIgnoreCase(String name);
}
