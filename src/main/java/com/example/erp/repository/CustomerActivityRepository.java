package com.example.erp.repository;

import com.example.erp.entity.CustomerActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerActivityRepository extends JpaRepository<CustomerActivity, Long> {

    Page<CustomerActivity> findByCustomerId(Long customerId, Pageable pageable);
}
