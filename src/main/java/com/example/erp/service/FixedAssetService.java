package com.example.erp.service;

import com.example.erp.dto.DepreciationEntryFilterRequest;
import com.example.erp.dto.DepreciationEntryResponse;
import com.example.erp.dto.DepreciationRunResponse;
import com.example.erp.dto.DisposeFixedAssetRequest;
import com.example.erp.dto.FixedAssetFilterRequest;
import com.example.erp.dto.FixedAssetRequest;
import com.example.erp.dto.FixedAssetResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RunDepreciationRequest;

public interface FixedAssetService {

    PageResponse<FixedAssetResponse> list(FixedAssetFilterRequest filter);

    FixedAssetResponse get(Long id);

    FixedAssetResponse create(FixedAssetRequest request, String actingUsername);

    FixedAssetResponse update(Long id, FixedAssetRequest request);

    void delete(Long id, String actingUsername);

    FixedAssetResponse dispose(Long id, DisposeFixedAssetRequest request, String actingUsername);

    DepreciationRunResponse runDepreciation(RunDepreciationRequest request, String actingUsername);

    PageResponse<DepreciationEntryResponse> listDepreciationEntries(DepreciationEntryFilterRequest filter);
}
