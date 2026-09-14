package com.example.erp.repository;

import com.example.erp.entity.DeliveryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryLineRepository extends JpaRepository<DeliveryLine, Long> {

    List<DeliveryLine> findByDeliveryId(Long deliveryId);
}
