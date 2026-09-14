package com.example.erp.service;

import com.example.erp.dto.CollectionActivityFilterRequest;
import com.example.erp.dto.CollectionActivityRequest;
import com.example.erp.dto.CollectionActivityResponse;
import com.example.erp.dto.PageResponse;

public interface CollectionActivityService {

    PageResponse<CollectionActivityResponse> list(CollectionActivityFilterRequest filter);

    CollectionActivityResponse get(Long id);

    CollectionActivityResponse create(CollectionActivityRequest request, String actingUsername);

    CollectionActivityResponse update(Long id, CollectionActivityRequest request);

    void delete(Long id);
}
