package com.example.erp.repository;

import com.example.erp.entity.PosExchangeNewLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosExchangeNewLineRepository extends JpaRepository<PosExchangeNewLine, Long> {
    List<PosExchangeNewLine> findByPosExchangeId(Long posExchangeId);
}
