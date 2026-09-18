package com.example.erp.service;

import com.example.erp.dto.ConvertOpportunityToQuotationRequest;
import com.example.erp.dto.CreateQuotationRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QuotationFilterRequest;
import com.example.erp.dto.QuotationResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdateQuotationRequest;

public interface QuotationService {

    PageResponse<QuotationResponse> listQuotations(QuotationFilterRequest filter);

    QuotationResponse getQuotation(Long id);

    QuotationResponse createQuotation(CreateQuotationRequest request, String actingUsername);

    QuotationResponse createFromOpportunity(Long opportunityId, ConvertOpportunityToQuotationRequest request, String actingUsername);

    QuotationResponse updateQuotation(Long id, UpdateQuotationRequest request);

    QuotationResponse sendQuotation(Long id);

    QuotationResponse acceptQuotation(Long id);

    QuotationResponse rejectQuotation(Long id);

    void deleteQuotation(Long id);

    void emailQuotation(Long id, SendDocumentEmailRequest request);
}
