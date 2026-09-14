package com.example.erp.repository;

import com.example.erp.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long>, JpaSpecificationExecutor<ProductPrice> {

    Optional<ProductPrice> findByProductIdAndPriceGroupId(Long productId, Long priceGroupId);

    boolean existsByProductIdAndPriceGroupIdAndIdNot(Long productId, Long priceGroupId, Long id);
}
