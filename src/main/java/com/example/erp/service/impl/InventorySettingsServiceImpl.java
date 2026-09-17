package com.example.erp.service.impl;

import com.example.erp.dto.InventorySettingsRequest;
import com.example.erp.dto.InventorySettingsResponse;
import com.example.erp.entity.InventorySettings;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.InventorySettingsRepository;
import com.example.erp.service.InventorySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventorySettingsServiceImpl implements InventorySettingsService {

    private final InventorySettingsRepository inventorySettingsRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public InventorySettingsResponse getForCompany(Long companyId) {
        return toResponse(resolveForCompany(companyId));
    }

    @Override
    @Transactional
    public InventorySettingsResponse upsert(InventorySettingsRequest request) {
        companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + request.getCompanyId()));

        InventorySettings settings = inventorySettingsRepository.findByCompanyId(request.getCompanyId())
                .orElseGet(() -> InventorySettings.builder().companyId(request.getCompanyId()).build());
        settings.setAllowOverselling(request.isAllowOverselling());
        settings.setAllowNegativeStock(request.isAllowNegativeStock());
        settings.setShowAvailableStock(request.isShowAvailableStock());
        settings.setReserveStock(request.isReserveStock());
        settings.setBackorderEnabled(request.isBackorderEnabled());
        settings.setOversellingApprovalRequired(request.isOversellingApprovalRequired());
        settings.setStockWarningEnabled(request.isStockWarningEnabled());
        inventorySettingsRepository.save(settings);
        return toResponse(settings);
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySettings resolveForCompany(Long companyId) {
        return inventorySettingsRepository.findByCompanyId(companyId)
                .orElseGet(() -> InventorySettings.builder().companyId(companyId).build());
    }

    private InventorySettingsResponse toResponse(InventorySettings settings) {
        return InventorySettingsResponse.builder()
                .companyId(settings.getCompanyId())
                .allowOverselling(settings.isAllowOverselling())
                .allowNegativeStock(settings.isAllowNegativeStock())
                .showAvailableStock(settings.isShowAvailableStock())
                .reserveStock(settings.isReserveStock())
                .backorderEnabled(settings.isBackorderEnabled())
                .oversellingApprovalRequired(settings.isOversellingApprovalRequired())
                .stockWarningEnabled(settings.isStockWarningEnabled())
                .build();
    }
}
