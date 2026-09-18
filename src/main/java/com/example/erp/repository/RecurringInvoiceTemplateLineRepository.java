package com.example.erp.repository;

import com.example.erp.entity.RecurringInvoiceTemplateLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringInvoiceTemplateLineRepository extends JpaRepository<RecurringInvoiceTemplateLine, Long> {

    List<RecurringInvoiceTemplateLine> findByTemplateId(Long templateId);

    void deleteByTemplateId(Long templateId);
}
