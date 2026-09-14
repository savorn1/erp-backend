package com.example.erp.service;

import com.example.erp.dto.BranchFilterRequest;
import com.example.erp.dto.BranchResponse;
import com.example.erp.dto.CreateBranchRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBranchRequest;
import com.example.erp.dto.UpdateBranchStatusRequest;

public interface BranchService {

    PageResponse<BranchResponse> listBranches(BranchFilterRequest filter);

    BranchResponse getBranch(Long id);

    BranchResponse createBranch(CreateBranchRequest request);

    BranchResponse updateBranch(Long id, UpdateBranchRequest request);

    BranchResponse updateStatus(Long id, UpdateBranchStatusRequest request);

    void deleteBranch(Long id);
}
