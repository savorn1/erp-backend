package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockMovementFilterRequest;
import com.example.erp.dto.StockMovementResponse;

public interface StockMovementService {

    PageResponse<StockMovementResponse> list(StockMovementFilterRequest filter);
}
