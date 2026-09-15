package com.example.erp.repository;

import com.example.erp.entity.PosHeldSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PosHeldSaleRepository extends JpaRepository<PosHeldSale, Long>, JpaSpecificationExecutor<PosHeldSale> {
}
