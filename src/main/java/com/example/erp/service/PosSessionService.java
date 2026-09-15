package com.example.erp.service;

import com.example.erp.dto.ClosePosSessionRequest;
import com.example.erp.dto.OpenPosSessionRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosSessionFilterRequest;
import com.example.erp.dto.PosSessionResponse;

public interface PosSessionService {

    PageResponse<PosSessionResponse> list(PosSessionFilterRequest filter);

    PosSessionResponse get(Long id);

    PosSessionResponse open(OpenPosSessionRequest request, String actingUsername);

    PosSessionResponse close(Long id, ClosePosSessionRequest request, String actingUsername);
}
