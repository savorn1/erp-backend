package com.example.erp.repository;

import com.example.erp.entity.SupplierActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierActivityRepository extends JpaRepository<SupplierActivity, Long> {

    Page<SupplierActivity> findBySupplierId(Long supplierId, Pageable pageable);
}
