package com.example.erp.repository;

import com.example.erp.entity.ProductUomPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductUomPriceRepository extends JpaRepository<ProductUomPrice, Long> {

    List<ProductUomPrice> findByProductUomId(Long productUomId);

    Optional<ProductUomPrice> findByProductUomIdAndPriceGroupId(Long productUomId, Long priceGroupId);

    boolean existsByProductUomIdAndPriceGroupIdAndIdNot(Long productUomId, Long priceGroupId, Long id);

    void deleteByProductUomId(Long productUomId);
}
