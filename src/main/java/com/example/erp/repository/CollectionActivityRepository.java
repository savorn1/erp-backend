package com.example.erp.repository;

import com.example.erp.entity.CollectionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CollectionActivityRepository extends JpaRepository<CollectionActivity, Long>, JpaSpecificationExecutor<CollectionActivity> {

    List<CollectionActivity> findByInvoiceId(Long invoiceId);
}
