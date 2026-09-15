package com.example.erp.repository;

import com.example.erp.entity.FixedAsset;
import com.example.erp.entity.FixedAssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long>, JpaSpecificationExecutor<FixedAsset> {
    boolean existsByCompanyIdAndAssetCode(Long companyId, String assetCode);
    boolean existsByCompanyIdAndAssetCodeAndIdNot(Long companyId, String assetCode, Long id);
    List<FixedAsset> findByCompanyIdAndStatus(Long companyId, FixedAssetStatus status);
}
