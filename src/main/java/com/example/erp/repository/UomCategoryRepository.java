package com.example.erp.repository;

import com.example.erp.entity.UomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UomCategoryRepository extends JpaRepository<UomCategory, Long>, JpaSpecificationExecutor<UomCategory> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<UomCategory> findByCode(String code);
}
