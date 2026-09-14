package com.example.erp.service;

import com.example.erp.dto.AddSupplierNoteRequest;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateSupplierRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierActivityFilterRequest;
import com.example.erp.dto.SupplierActivityResponse;
import com.example.erp.dto.SupplierFilterRequest;
import com.example.erp.dto.SupplierResponse;
import com.example.erp.dto.UpdateSupplierRequest;
import com.example.erp.dto.UpdateSupplierStatusRequest;

public interface SupplierService {

    PageResponse<SupplierResponse> listSuppliers(SupplierFilterRequest filter);

    SupplierResponse getSupplier(Long id);

    SupplierResponse createSupplier(CreateSupplierRequest request, String actingUsername);

    SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request);

    SupplierResponse updateStatus(Long id, UpdateSupplierStatusRequest request, String actingUsername);

    void deleteSupplier(Long id);

    SupplierResponse adjustBalance(Long id, BalanceAdjustmentRequest request, String actingUsername);

    PageResponse<SupplierActivityResponse> listActivities(Long supplierId, SupplierActivityFilterRequest filter);

    SupplierActivityResponse addNote(Long supplierId, AddSupplierNoteRequest request, String actingUsername);
}
