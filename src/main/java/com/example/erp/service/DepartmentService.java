package com.example.erp.service;

import com.example.erp.dto.AssignEmployeesRequest;
import com.example.erp.dto.CreateDepartmentRequest;
import com.example.erp.dto.DepartmentFilterRequest;
import com.example.erp.dto.DepartmentResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateDepartmentRequest;
import com.example.erp.dto.UpdateDepartmentStatusRequest;

public interface DepartmentService {

    PageResponse<DepartmentResponse> listDepartments(DepartmentFilterRequest filter);

    DepartmentResponse getDepartment(Long id);

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    DepartmentResponse updateStatus(Long id, UpdateDepartmentStatusRequest request);

    void deleteDepartment(Long id);

    void assignEmployees(Long id, AssignEmployeesRequest request);

    void unassignEmployee(Long id, Long userId);
}
