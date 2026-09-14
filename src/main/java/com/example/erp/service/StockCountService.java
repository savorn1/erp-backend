package com.example.erp.service;

import com.example.erp.dto.CreateStockCountRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockCountFilterRequest;
import com.example.erp.dto.StockCountResponse;
import com.example.erp.dto.SubmitStockCountRequest;

public interface StockCountService {

    PageResponse<StockCountResponse> listStockCounts(StockCountFilterRequest filter);

    StockCountResponse getStockCount(Long id);

    StockCountResponse createStockCount(CreateStockCountRequest request, String actingUsername);

    StockCountResponse submitCounts(Long id, SubmitStockCountRequest request);

    StockCountResponse completeStockCount(Long id);

    StockCountResponse reconcileStockCount(Long id, String actingUsername);

    void deleteStockCount(Long id);
}
