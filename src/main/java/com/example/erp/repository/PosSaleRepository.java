package com.example.erp.repository;

import com.example.erp.entity.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PosSaleRepository extends JpaRepository<PosSale, Long>, JpaSpecificationExecutor<PosSale> {
    List<PosSale> findByPosSessionId(Long posSessionId);
}
