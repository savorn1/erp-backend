package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RegisterFilterRequest;
import com.example.erp.dto.RegisterRequest;
import com.example.erp.dto.RegisterResponse;

public interface RegisterService {

    PageResponse<RegisterResponse> list(RegisterFilterRequest filter);

    RegisterResponse get(Long id);

    RegisterResponse create(RegisterRequest request);

    RegisterResponse update(Long id, RegisterRequest request);

    void delete(Long id);
}
