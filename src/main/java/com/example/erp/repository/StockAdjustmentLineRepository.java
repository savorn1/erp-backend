package com.example.erp.repository;

import com.example.erp.entity.StockAdjustmentLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAdjustmentLineRepository extends JpaRepository<StockAdjustmentLine, Long> {

    List<StockAdjustmentLine> findByStockAdjustmentId(Long stockAdjustmentId);

    void deleteByStockAdjustmentId(Long stockAdjustmentId);
}
