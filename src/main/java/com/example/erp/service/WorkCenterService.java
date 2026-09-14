package com.example.erp.service;

import com.example.erp.dto.CreateWorkCenterRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateWorkCenterRequest;
import com.example.erp.dto.UpdateWorkCenterStatusRequest;
import com.example.erp.dto.WorkCenterFilterRequest;
import com.example.erp.dto.WorkCenterResponse;

public interface WorkCenterService {

    PageResponse<WorkCenterResponse> listWorkCenters(WorkCenterFilterRequest filter);

    WorkCenterResponse getWorkCenter(Long id);

    WorkCenterResponse createWorkCenter(CreateWorkCenterRequest request);

    WorkCenterResponse updateWorkCenter(Long id, UpdateWorkCenterRequest request);

    WorkCenterResponse updateStatus(Long id, UpdateWorkCenterStatusRequest request);

    void deleteWorkCenter(Long id);
}
