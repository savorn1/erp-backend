package com.example.erp.service;

import com.example.erp.dto.CreateRmaRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ResolveRmaRequest;
import com.example.erp.dto.RmaFilterRequest;
import com.example.erp.dto.RmaResponse;

public interface RmaService {

    PageResponse<RmaResponse> listRmas(RmaFilterRequest filter);

    RmaResponse getRma(Long id);

    RmaResponse createRma(CreateRmaRequest request, String actingUsername);

    RmaResponse approveRma(Long id);

    RmaResponse rejectRma(Long id);

    RmaResponse resolveRma(Long id, ResolveRmaRequest request, String actingUsername);

    RmaResponse cancelRma(Long id);

    void deleteRma(Long id);
}
