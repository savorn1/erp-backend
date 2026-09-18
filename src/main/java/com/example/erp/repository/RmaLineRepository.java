package com.example.erp.repository;

import com.example.erp.entity.RmaLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RmaLineRepository extends JpaRepository<RmaLine, Long> {

    List<RmaLine> findByRmaId(Long rmaId);

    void deleteByRmaId(Long rmaId);
}
