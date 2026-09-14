package com.example.erp.service;

import com.example.erp.dto.CreateStockAdjustmentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockAdjustmentFilterRequest;
import com.example.erp.dto.StockAdjustmentResponse;

public interface StockAdjustmentService {

    PageResponse<StockAdjustmentResponse> listStockAdjustments(StockAdjustmentFilterRequest filter);

    StockAdjustmentResponse getStockAdjustment(Long id);

    StockAdjustmentResponse createStockAdjustment(CreateStockAdjustmentRequest request, String actingUsername);

    StockAdjustmentResponse approveStockAdjustment(Long id, String actingUsername);

    StockAdjustmentResponse rejectStockAdjustment(Long id);

    void deleteStockAdjustment(Long id);
}
