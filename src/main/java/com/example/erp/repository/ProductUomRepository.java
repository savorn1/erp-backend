package com.example.erp.repository;

import com.example.erp.entity.ProductUom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductUomRepository extends JpaRepository<ProductUom, Long> {

    List<ProductUom> findByProductIdAndVariantIdIsNull(Long productId);

    // Used to demote stale base rows when a product's own unitOfMeasureId
    // changes — see ProductServiceImpl.reconcileBaseUnitChange.
    List<ProductUom> findByProductIdAndBaseUnitTrue(Long productId);

    List<ProductUom> findByProductIdAndVariantId(Long productId, Long variantId);

    Optional<ProductUom> findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(Long productId, Long unitOfMeasureId);

    Optional<ProductUom> findByProductIdAndVariantIdAndUnitOfMeasureId(Long productId, Long variantId, Long unitOfMeasureId);

    boolean existsByProductIdAndVariantIdIsNullAndUnitOfMeasureId(Long productId, Long unitOfMeasureId);

    boolean existsByProductIdAndVariantIdAndUnitOfMeasureId(Long productId, Long variantId, Long unitOfMeasureId);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    // Used to surface a product's default purchase/sales unit (falls back to
    // the product's own unitOfMeasureId when no row is flagged default yet —
    // see ProductServiceImpl).
    Optional<ProductUom> findByProductIdAndVariantIdIsNullAndDefaultPurchaseTrue(Long productId);

    Optional<ProductUom> findByProductIdAndVariantIdIsNullAndDefaultSalesTrue(Long productId);

    List<ProductUom> findByProductIdInAndVariantIdIsNullAndDefaultPurchaseTrue(List<Long> productIds);

    List<ProductUom> findByProductIdInAndVariantIdIsNullAndDefaultSalesTrue(List<Long> productIds);
}
