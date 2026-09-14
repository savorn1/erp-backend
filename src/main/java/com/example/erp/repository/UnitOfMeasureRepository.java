package com.example.erp.repository;

import com.example.erp.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long>, JpaSpecificationExecutor<UnitOfMeasure> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCategoryId(Long categoryId);

    List<UnitOfMeasure> findByCategoryId(Long categoryId);
}
