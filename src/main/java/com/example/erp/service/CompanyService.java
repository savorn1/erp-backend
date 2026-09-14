package com.example.erp.service;

import com.example.erp.dto.CompanyFilterRequest;
import com.example.erp.dto.CompanyResponse;
import com.example.erp.dto.CreateCompanyRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCompanyRequest;
import com.example.erp.dto.UpdateCompanyStatusRequest;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {

    PageResponse<CompanyResponse> listCompanies(CompanyFilterRequest filter);

    CompanyResponse getCompany(Long id);

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyResponse updateCompany(Long id, UpdateCompanyRequest request);

    CompanyResponse updateStatus(Long id, UpdateCompanyStatusRequest request);

    CompanyResponse updateLogo(Long id, MultipartFile file);

    void deleteCompany(Long id);
}
