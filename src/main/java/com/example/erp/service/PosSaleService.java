package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosCheckoutRequest;
import com.example.erp.dto.PosSaleFilterRequest;
import com.example.erp.dto.PosSaleResponse;
import com.example.erp.dto.VoidPosSaleRequest;

public interface PosSaleService {

    PageResponse<PosSaleResponse> list(PosSaleFilterRequest filter);

    PosSaleResponse get(Long id);

    PosSaleResponse checkout(PosCheckoutRequest request, String actingUsername);

    PosSaleResponse voidSale(Long id, VoidPosSaleRequest request, String actingUsername);
}
