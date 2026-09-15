package com.example.erp.repository;

import com.example.erp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByCompanyIdAndSku(Long companyId, String sku);

    boolean existsByCompanyIdAndSkuAndIdNot(Long companyId, String sku, Long id);

    boolean existsByCompanyIdAndBarcode(Long companyId, String barcode);

    boolean existsByCompanyIdAndBarcodeAndIdNot(Long companyId, String barcode, Long id);

    boolean existsByUnitOfMeasureId(Long unitOfMeasureId);
}
