package com.example.erp.repository;

import com.example.erp.entity.DepreciationEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DepreciationEntryRepository extends JpaRepository<DepreciationEntry, Long>, JpaSpecificationExecutor<DepreciationEntry> {
    boolean existsByAssetId(Long assetId);
    List<DepreciationEntry> findByDepreciationRunId(Long depreciationRunId);
}
