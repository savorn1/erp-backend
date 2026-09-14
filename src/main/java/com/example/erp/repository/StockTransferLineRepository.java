package com.example.erp.repository;

import com.example.erp.entity.StockTransferLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferLineRepository extends JpaRepository<StockTransferLine, Long> {

    List<StockTransferLine> findByStockTransferId(Long stockTransferId);

    void deleteByStockTransferId(Long stockTransferId);
}
