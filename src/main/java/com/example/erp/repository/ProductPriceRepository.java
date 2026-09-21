package com.example.erp.repository;

import com.example.erp.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long>, JpaSpecificationExecutor<ProductPrice> {

    // A null unitOfMeasureId is the product's base unit, so the two lookups
    // below are genuinely different queries — `... AndUnitOfMeasureId(null)`
    // would generate `= null`, which matches nothing in SQL.
    Optional<ProductPrice> findByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(Long productId, Long priceGroupId);

    Optional<ProductPrice> findByProductIdAndPriceGroupIdAndUnitOfMeasureId(Long productId, Long priceGroupId, Long unitOfMeasureId);

    boolean existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(Long productId, Long priceGroupId);

    boolean existsByProductIdAndPriceGroupIdAndUnitOfMeasureId(Long productId, Long priceGroupId, Long unitOfMeasureId);

    boolean existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNullAndIdNot(Long productId, Long priceGroupId, Long id);

    boolean existsByProductIdAndPriceGroupIdAndUnitOfMeasureIdAndIdNot(Long productId, Long priceGroupId, Long unitOfMeasureId, Long id);
}
