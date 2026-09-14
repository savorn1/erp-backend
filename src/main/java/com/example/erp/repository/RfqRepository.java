package com.example.erp.repository;

import com.example.erp.entity.Rfq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RfqRepository extends JpaRepository<Rfq, Long>, JpaSpecificationExecutor<Rfq> {
}
