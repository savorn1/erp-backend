package com.example.erp.repository;

import com.example.erp.entity.StockCountLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockCountLineRepository extends JpaRepository<StockCountLine, Long> {

    List<StockCountLine> findByStockCountId(Long stockCountId);

    void deleteByStockCountId(Long stockCountId);
}
