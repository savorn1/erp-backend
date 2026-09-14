package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.StockLevelFilterRequest;
import com.example.erp.dto.StockLevelResponse;

public interface StockLevelService {

    PageResponse<StockLevelResponse> list(StockLevelFilterRequest filter);
}
