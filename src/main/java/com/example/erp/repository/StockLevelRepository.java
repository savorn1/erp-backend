package com.example.erp.repository;

import com.example.erp.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long>, JpaSpecificationExecutor<StockLevel> {

    Optional<StockLevel> findByProductIdAndWarehouseIdAndBinId(Long productId, Long warehouseId, Long binId);

    Optional<StockLevel> findByProductIdAndWarehouseIdAndBinIdIsNull(Long productId, Long warehouseId);

    // Every bin (plus the unbinned row, if any) holding this product at this
    // warehouse — used when a caller doesn't pin down a specific bin, so
    // "available"/"decrease" can span all of them instead of only matching
    // the unbinned row.
    List<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
