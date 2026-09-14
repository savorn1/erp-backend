package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseBinFilterRequest;
import com.example.erp.dto.WarehouseBinRequest;
import com.example.erp.dto.WarehouseBinResponse;

public interface WarehouseBinService {

    PageResponse<WarehouseBinResponse> list(WarehouseBinFilterRequest filter);

    WarehouseBinResponse get(Long id);

    WarehouseBinResponse create(WarehouseBinRequest request);

    WarehouseBinResponse update(Long id, WarehouseBinRequest request);

    void delete(Long id);
}
