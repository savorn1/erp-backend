package com.example.erp.repository;

import com.example.erp.entity.InventorySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventorySettingsRepository extends JpaRepository<InventorySettings, Long> {
    Optional<InventorySettings> findByCompanyId(Long companyId);
}
