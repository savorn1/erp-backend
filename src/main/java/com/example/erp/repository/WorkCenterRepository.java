package com.example.erp.repository;

import com.example.erp.entity.WorkCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, Long>, JpaSpecificationExecutor<WorkCenter> {
}
