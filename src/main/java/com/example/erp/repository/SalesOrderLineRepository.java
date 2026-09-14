package com.example.erp.repository;

import com.example.erp.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {

    List<SalesOrderLine> findBySalesOrderId(Long salesOrderId);

    void deleteBySalesOrderId(Long salesOrderId);
}
