package com.example.erp.repository;

import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PosSessionRepository extends JpaRepository<PosSession, Long>, JpaSpecificationExecutor<PosSession> {
    Optional<PosSession> findByRegisterIdAndStatus(Long registerId, PosSessionStatus status);
}
