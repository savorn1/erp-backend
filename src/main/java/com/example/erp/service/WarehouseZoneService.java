package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.WarehouseZoneFilterRequest;
import com.example.erp.dto.WarehouseZoneRequest;
import com.example.erp.dto.WarehouseZoneResponse;

public interface WarehouseZoneService {

    PageResponse<WarehouseZoneResponse> list(WarehouseZoneFilterRequest filter);

    WarehouseZoneResponse get(Long id);

    WarehouseZoneResponse create(WarehouseZoneRequest request);

    WarehouseZoneResponse update(Long id, WarehouseZoneRequest request);

    void delete(Long id);
}
