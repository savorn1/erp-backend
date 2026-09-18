package com.example.erp.repository;

import com.example.erp.entity.RmaRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RmaRequestRepository extends JpaRepository<RmaRequest, Long>, JpaSpecificationExecutor<RmaRequest> {
}
