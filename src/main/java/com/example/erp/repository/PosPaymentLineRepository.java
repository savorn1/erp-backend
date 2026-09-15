package com.example.erp.repository;

import com.example.erp.entity.PosPaymentLine;
import com.example.erp.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosPaymentLineRepository extends JpaRepository<PosPaymentLine, Long> {
    List<PosPaymentLine> findByPosSaleId(Long posSaleId);
    List<PosPaymentLine> findByPosSaleIdInAndMethod(List<Long> posSaleIds, PaymentMethod method);
}
