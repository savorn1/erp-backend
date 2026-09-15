package com.example.erp.repository;

import com.example.erp.entity.PosExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PosExchangeRepository extends JpaRepository<PosExchange, Long>, JpaSpecificationExecutor<PosExchange> {
}
