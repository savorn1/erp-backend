package com.example.erp.repository;

import com.example.erp.entity.RecurringInvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface RecurringInvoiceTemplateRepository extends JpaRepository<RecurringInvoiceTemplate, Long>, JpaSpecificationExecutor<RecurringInvoiceTemplate> {

    List<RecurringInvoiceTemplate> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);

    List<RecurringInvoiceTemplate> findByActiveTrueAndNextRunDateLessThanEqualAndCompanyId(LocalDate date, Long companyId);
}
