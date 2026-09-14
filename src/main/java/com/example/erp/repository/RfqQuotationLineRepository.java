package com.example.erp.repository;

import com.example.erp.entity.RfqQuotationLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RfqQuotationLineRepository extends JpaRepository<RfqQuotationLine, Long> {

    List<RfqQuotationLine> findByRfqId(Long rfqId);

    List<RfqQuotationLine> findByRfqIdAndSupplierId(Long rfqId, Long supplierId);

    void deleteByRfqIdAndSupplierId(Long rfqId, Long supplierId);

    void deleteByRfqId(Long rfqId);
}
