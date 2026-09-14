package com.example.erp.service;

import com.example.erp.dto.BillOfMaterialFilterRequest;
import com.example.erp.dto.BillOfMaterialResponse;
import com.example.erp.dto.BomVersionRowResponse;
import com.example.erp.dto.CreateBillOfMaterialRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBillOfMaterialRequest;

import java.util.List;

public interface BillOfMaterialService {

    PageResponse<BillOfMaterialResponse> listBoms(BillOfMaterialFilterRequest filter);

    BillOfMaterialResponse getBom(Long id);

    BillOfMaterialResponse createBom(CreateBillOfMaterialRequest request, String actingUsername);

    BillOfMaterialResponse updateBom(Long id, UpdateBillOfMaterialRequest request);

    BillOfMaterialResponse activateBom(Long id);

    BillOfMaterialResponse deactivateBom(Long id);

    BillOfMaterialResponse createNewVersion(Long id, String actingUsername);

    List<BomVersionRowResponse> getVersionHistory(Long id);

    void deleteBom(Long id);
}
