package com.example.erp.repository;

import com.example.erp.entity.PosHeldSaleLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosHeldSaleLineRepository extends JpaRepository<PosHeldSaleLine, Long> {
    List<PosHeldSaleLine> findByPosHeldSaleId(Long posHeldSaleId);
    void deleteByPosHeldSaleId(Long posHeldSaleId);
}
