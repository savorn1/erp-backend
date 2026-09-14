package com.example.erp.repository;

import com.example.erp.entity.Routing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoutingRepository extends JpaRepository<Routing, Long>, JpaSpecificationExecutor<Routing> {
}
