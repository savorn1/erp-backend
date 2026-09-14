package com.example.erp.repository;

import com.example.erp.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MachineRepository extends JpaRepository<Machine, Long>, JpaSpecificationExecutor<Machine> {
}
