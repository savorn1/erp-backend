package com.example.erp.repository;

import com.example.erp.entity.ManufacturingOrderMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManufacturingOrderMaterialRepository extends JpaRepository<ManufacturingOrderMaterial, Long> {
    List<ManufacturingOrderMaterial> findByManufacturingOrderId(Long manufacturingOrderId);
    void deleteByManufacturingOrderId(Long manufacturingOrderId);
}
