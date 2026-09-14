package com.example.erp.service;

import com.example.erp.dto.JournalFilterRequest;
import com.example.erp.dto.JournalRequest;
import com.example.erp.dto.JournalResponse;
import com.example.erp.dto.PageResponse;

public interface JournalService {

    PageResponse<JournalResponse> list(JournalFilterRequest filter);

    JournalResponse get(Long id);

    JournalResponse create(JournalRequest request);

    JournalResponse update(Long id, JournalRequest request);

    void delete(Long id);
}
