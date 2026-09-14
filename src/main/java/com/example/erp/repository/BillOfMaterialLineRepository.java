package com.example.erp.repository;

import com.example.erp.entity.BillOfMaterialLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillOfMaterialLineRepository extends JpaRepository<BillOfMaterialLine, Long> {
    List<BillOfMaterialLine> findByBomId(Long bomId);
    void deleteByBomId(Long bomId);
}
