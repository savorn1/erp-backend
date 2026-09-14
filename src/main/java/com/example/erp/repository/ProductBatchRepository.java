package com.example.erp.repository;

import com.example.erp.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    Optional<ProductBatch> findByProductIdAndBatchNumber(Long productId, String batchNumber);
}
