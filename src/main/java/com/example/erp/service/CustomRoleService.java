package com.example.erp.service;

import com.example.erp.dto.CustomRoleFilterRequest;
import com.example.erp.dto.CustomRoleRequest;
import com.example.erp.dto.CustomRoleResponse;
import com.example.erp.dto.PageResponse;

public interface CustomRoleService {

    PageResponse<CustomRoleResponse> list(CustomRoleFilterRequest filter);

    CustomRoleResponse get(Long id);

    CustomRoleResponse create(CustomRoleRequest request);

    CustomRoleResponse update(Long id, CustomRoleRequest request);

    void delete(Long id);
}
