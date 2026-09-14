package com.example.erp.service;

import com.example.erp.dto.CreateWarehouseRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateWarehouseRequest;
import com.example.erp.dto.UpdateWarehouseStatusRequest;
import com.example.erp.dto.WarehouseFilterRequest;
import com.example.erp.dto.WarehouseResponse;

public interface WarehouseService {

    PageResponse<WarehouseResponse> listWarehouses(WarehouseFilterRequest filter);

    WarehouseResponse getWarehouse(Long id);

    WarehouseResponse createWarehouse(CreateWarehouseRequest request);

    WarehouseResponse updateWarehouse(Long id, UpdateWarehouseRequest request);

    WarehouseResponse updateStatus(Long id, UpdateWarehouseStatusRequest request);

    void deleteWarehouse(Long id);
}
