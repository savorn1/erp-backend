package com.example.erp.service;

import com.example.erp.dto.CreateRfqRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecordRfqQuotationRequest;
import com.example.erp.dto.RfqFilterRequest;
import com.example.erp.dto.RfqResponse;
import com.example.erp.dto.UpdateRfqRequest;

public interface RfqService {

    PageResponse<RfqResponse> listRfqs(RfqFilterRequest filter);

    RfqResponse getRfq(Long id);

    RfqResponse createRfq(CreateRfqRequest request, String actingUsername);

    RfqResponse updateRfq(Long id, UpdateRfqRequest request);

    RfqResponse sendRfq(Long id);

    RfqResponse recordQuotation(Long id, Long supplierId, RecordRfqQuotationRequest request);

    RfqResponse selectSupplier(Long id, Long supplierId, String actingUsername);

    RfqResponse cancelRfq(Long id);

    void deleteRfq(Long id);
}
