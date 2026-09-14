package com.example.erp.repository;

import com.example.erp.entity.RfqLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RfqLineRepository extends JpaRepository<RfqLine, Long> {

    List<RfqLine> findByRfqId(Long rfqId);

    void deleteByRfqId(Long rfqId);
}
