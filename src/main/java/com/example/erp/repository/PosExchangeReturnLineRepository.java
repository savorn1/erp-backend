package com.example.erp.repository;

import com.example.erp.entity.PosExchangeReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosExchangeReturnLineRepository extends JpaRepository<PosExchangeReturnLine, Long> {
    List<PosExchangeReturnLine> findByPosExchangeId(Long posExchangeId);
}
