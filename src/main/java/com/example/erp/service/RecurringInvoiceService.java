package com.example.erp.service;

import com.example.erp.dto.CreateRecurringInvoiceTemplateRequest;
import com.example.erp.dto.GenerateDueInvoicesResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecurringInvoiceTemplateFilterRequest;
import com.example.erp.dto.RecurringInvoiceTemplateResponse;
import com.example.erp.dto.UpdateRecurringInvoiceTemplateRequest;

public interface RecurringInvoiceService {

    PageResponse<RecurringInvoiceTemplateResponse> listTemplates(RecurringInvoiceTemplateFilterRequest filter);

    RecurringInvoiceTemplateResponse getTemplate(Long id);

    RecurringInvoiceTemplateResponse createTemplate(CreateRecurringInvoiceTemplateRequest request, String actingUsername);

    RecurringInvoiceTemplateResponse updateTemplate(Long id, UpdateRecurringInvoiceTemplateRequest request);

    void deleteTemplate(Long id);

    GenerateDueInvoicesResponse generateDueInvoices(Long companyId, String actingUsername);
}
