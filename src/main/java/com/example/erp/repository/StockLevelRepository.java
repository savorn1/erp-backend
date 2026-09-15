package com.example.erp.repository;

import com.example.erp.entity.StockLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Locking sibling of the above — POS checkout is the only flow in this
    // codebase where two actors (two registers) can race to decrement the
    // same product/warehouse simultaneously, so its decrement holds this
    // row lock for the duration of the sale. Every other caller keeps using
    // the unlocked query above, since they don't have that race.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockLevel s where s.productId = :productId and s.warehouseId = :warehouseId")
    List<StockLevel> findByProductIdAndWarehouseIdForUpdate(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
