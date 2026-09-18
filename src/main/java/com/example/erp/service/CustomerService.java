package com.example.erp.service;

import com.example.erp.dto.AddCustomerNoteRequest;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateCustomerRequest;
import com.example.erp.dto.CustomerActivityFilterRequest;
import com.example.erp.dto.CustomerActivityResponse;
import com.example.erp.dto.CustomerFilterRequest;
import com.example.erp.dto.CustomerResponse;
import com.example.erp.dto.ImportResultResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCustomerRequest;
import com.example.erp.dto.UpdateCustomerStatusRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerService {

    PageResponse<CustomerResponse> listCustomers(CustomerFilterRequest filter);

    CustomerResponse getCustomer(Long id);

    CustomerResponse createCustomer(CreateCustomerRequest request, String actingUsername);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

    CustomerResponse updateStatus(Long id, UpdateCustomerStatusRequest request, String actingUsername);

    void deleteCustomer(Long id);

    CustomerResponse adjustBalance(Long id, BalanceAdjustmentRequest request, String actingUsername);

    PageResponse<CustomerActivityResponse> listActivities(Long customerId, CustomerActivityFilterRequest filter);

    CustomerActivityResponse addNote(Long customerId, AddCustomerNoteRequest request, String actingUsername);

    ImportResultResponse importCustomersFromCsv(MultipartFile file, Long companyId, String actingUsername);
}
