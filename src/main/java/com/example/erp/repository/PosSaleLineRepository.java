package com.example.erp.repository;

import com.example.erp.entity.PosSaleLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosSaleLineRepository extends JpaRepository<PosSaleLine, Long> {
    List<PosSaleLine> findByPosSaleId(Long posSaleId);
}
