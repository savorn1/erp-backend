package com.example.erp.repository;

import com.example.erp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    boolean existsByCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyIdAndNameAndIdNot(Long companyId, String name, Long id);

    // Used by PosSaleServiceImpl.resolveCustomer to find-or-create the
    // per-company synthetic "Walk-in Customer" row.
    Optional<Customer> findByCompanyIdAndName(Long companyId, String name);
}
