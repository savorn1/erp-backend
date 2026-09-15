package com.example.erp.service;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosHeldSaleFilterRequest;
import com.example.erp.dto.PosHeldSaleResponse;
import com.example.erp.dto.PosHoldRequest;

public interface PosHeldSaleService {

    PageResponse<PosHeldSaleResponse> list(PosHeldSaleFilterRequest filter);

    PosHeldSaleResponse hold(PosHoldRequest request, String actingUsername);

    // Returns the held sale (fully resolved for rebuilding a cart) and
    // deletes it — a held sale can only be resumed once.
    PosHeldSaleResponse resume(Long id);

    void discard(Long id);
}
