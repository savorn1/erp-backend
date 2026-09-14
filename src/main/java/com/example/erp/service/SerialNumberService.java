package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SerialNumberFilterRequest;
import com.example.erp.dto.SerialNumberResponse;

public interface SerialNumberService {

    PageResponse<SerialNumberResponse> list(SerialNumberFilterRequest filter);
}
