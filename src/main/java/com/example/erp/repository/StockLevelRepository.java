package com.example.erp.repository;

import com.example.erp.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long>, JpaSpecificationExecutor<StockLevel> {

    Optional<StockLevel> findByProductIdAndWarehouseIdAndBinId(Long productId, Long warehouseId, Long binId);

    Optional<StockLevel> findByProductIdAndWarehouseIdAndBinIdIsNull(Long productId, Long warehouseId);
}
