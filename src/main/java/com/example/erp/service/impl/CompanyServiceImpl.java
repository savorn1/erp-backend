package com.example.erp.service.impl;

import com.example.erp.dto.CompanyFilterRequest;
import com.example.erp.dto.CompanyResponse;
import com.example.erp.dto.CreateCompanyRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCompanyRequest;
import com.example.erp.dto.UpdateCompanyStatusRequest;
import com.example.erp.entity.Company;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.service.CompanyService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> listCompanies(CompanyFilterRequest filter) {
        List<Specification<Company>> conditions = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<Company> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Company> companies = companyRepository.findAll(spec, pageable);
        return PageResponse.of(companies.map(this::toResponse));
    }

    @Override
    public CompanyResponse getCompany(Long id) {
        return toResponse(findCompany(id));
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Company name already taken: " + request.getName());
        }
        Company company = Company.builder()
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .taxId(request.getTaxId())
                .currency(request.getCurrency().toUpperCase())
                .fiscalYearStartMonth(request.getFiscalYearStartMonth())
                .build();
        companyRepository.save(company);
        return toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, UpdateCompanyRequest request) {
        Company company = findCompany(id);
        if (companyRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Company name already taken: " + request.getName());
        }
        company.setName(request.getName());
        company.setAddressLine1(request.getAddressLine1());
        company.setAddressLine2(request.getAddressLine2());
        company.setCity(request.getCity());
        company.setState(request.getState());
        company.setPostalCode(request.getPostalCode());
        company.setCountry(request.getCountry());
        company.setTaxId(request.getTaxId());
        company.setCurrency(request.getCurrency().toUpperCase());
        company.setFiscalYearStartMonth(request.getFiscalYearStartMonth());
        companyRepository.save(company);
        return toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateStatus(Long id, UpdateCompanyStatusRequest request) {
        Company company = findCompany(id);
        company.setActive(request.getActive());
        companyRepository.save(company);
        return toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse updateLogo(Long id, MultipartFile file) {
        Company company = findCompany(id);
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Logo file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Logo must be an image file");
        }

        try {
            Path dir = Paths.get(uploadDir, "companies", String.valueOf(id));
            Files.createDirectories(dir);

            // Best-effort cleanup of the previous logo so replacing it doesn't leave
            // an orphaned file behind — failure here shouldn't block the new upload.
            if (company.getLogoUrl() != null) {
                String previousFilename = company.getLogoUrl().substring(company.getLogoUrl().lastIndexOf('/') + 1);
                Files.deleteIfExists(dir.resolve(previousFilename));
            }

            String filename = "logo-" + System.currentTimeMillis() + extensionFor(contentType, file.getOriginalFilename());
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            company.setLogoUrl("/uploads/companies/" + id + "/" + filename);
            companyRepository.save(company);
            return toResponse(company);
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store company logo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.delete(findCompany(id));
    }

    private String extensionFor(String contentType, String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Company not found with id: " + id));
    }

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .addressLine1(company.getAddressLine1())
                .addressLine2(company.getAddressLine2())
                .city(company.getCity())
                .state(company.getState())
                .postalCode(company.getPostalCode())
                .country(company.getCountry())
                .taxId(company.getTaxId())
                .currency(company.getCurrency())
                .fiscalYearStartMonth(company.getFiscalYearStartMonth())
                .active(company.isActive())
                .build();
    }
}
