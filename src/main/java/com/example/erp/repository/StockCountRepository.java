package com.example.erp.repository;

import com.example.erp.entity.StockCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockCountRepository extends JpaRepository<StockCount, Long>, JpaSpecificationExecutor<StockCount> {
}
