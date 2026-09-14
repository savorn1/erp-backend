package com.example.erp.repository;

import com.example.erp.entity.QuotationLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationLineRepository extends JpaRepository<QuotationLine, Long> {

    List<QuotationLine> findByQuotationId(Long quotationId);

    void deleteByQuotationId(Long quotationId);
}
