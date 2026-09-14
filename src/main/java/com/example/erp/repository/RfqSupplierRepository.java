package com.example.erp.repository;

import com.example.erp.entity.RfqSupplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RfqSupplierRepository extends JpaRepository<RfqSupplier, Long> {

    List<RfqSupplier> findByRfqId(Long rfqId);

    Optional<RfqSupplier> findByRfqIdAndSupplierId(Long rfqId, Long supplierId);

    void deleteByRfqId(Long rfqId);
}
