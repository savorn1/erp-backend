package com.example.erp.service.impl;

import com.example.erp.dto.AddSupplierNoteRequest;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateSupplierRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SupplierActivityFilterRequest;
import com.example.erp.dto.SupplierActivityResponse;
import com.example.erp.dto.SupplierFilterRequest;
import com.example.erp.dto.SupplierResponse;
import com.example.erp.dto.UpdateSupplierRequest;
import com.example.erp.dto.UpdateSupplierStatusRequest;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.Company;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.SupplierActivity;
import com.example.erp.entity.SupplierActivityType;
import com.example.erp.entity.SupplierType;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.SupplierActivityRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.SupplierTypeRepository;
import com.example.erp.service.SupplierService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierActivityRepository activityRepository;
    private final CompanyRepository companyRepository;
    private final SupplierTypeRepository supplierTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> listSuppliers(SupplierFilterRequest filter) {
        List<Specification<Supplier>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSupplierTypeId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierTypeId"), filter.getSupplierTypeId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Supplier> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Supplier> page = supplierRepository.findAll(spec, pageable);
        List<Supplier> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Supplier::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> typeNames = supplierTypeRepository.findAllById(
                content.stream().map(Supplier::getSupplierTypeId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(SupplierType::getId, SupplierType::getName));

        return PageResponse.of(page.map(s -> toResponse(s,
                companyNames.get(s.getCompanyId()),
                s.getSupplierTypeId() == null ? null : typeNames.get(s.getSupplierTypeId()))));
    }

    @Override
    public SupplierResponse getSupplier(Long id) {
        Supplier supplier = find(id);
        return toResponse(supplier,
                companyNameOf(supplier.getCompanyId()),
                supplier.getSupplierTypeId() == null ? null : supplierTypeRepository.findById(supplier.getSupplierTypeId()).map(SupplierType::getName).orElse(null));
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request, String actingUsername) {
        Company company = requireCompany(request.getCompanyId());
        validateSupplierType(request.getSupplierTypeId());
        if (supplierRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Supplier name already taken in this company: " + request.getName());
        }

        Supplier supplier = Supplier.builder()
                .companyId(request.getCompanyId())
                .supplierTypeId(request.getSupplierTypeId())
                .name(request.getName())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .paymentTerms(request.getPaymentTerms())
                .creditLimit(request.getCreditLimit())
                .build();
        supplierRepository.save(supplier);

        recordActivity(supplier.getId(), SupplierActivityType.CREATED, "Supplier created", null, actingUsername);

        return getSupplier(supplier.getId());
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        Supplier supplier = find(id);
        requireCompany(request.getCompanyId());
        validateSupplierType(request.getSupplierTypeId());
        if (supplierRepository.existsByCompanyIdAndNameAndIdNot(request.getCompanyId(), request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Supplier name already taken in this company: " + request.getName());
        }

        supplier.setCompanyId(request.getCompanyId());
        supplier.setSupplierTypeId(request.getSupplierTypeId());
        supplier.setName(request.getName());
        supplier.setContactName(request.getContactName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddressLine1(request.getAddressLine1());
        supplier.setAddressLine2(request.getAddressLine2());
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setCountry(request.getCountry());
        supplier.setPaymentTerms(request.getPaymentTerms());
        supplier.setCreditLimit(request.getCreditLimit());
        supplierRepository.save(supplier);
        return getSupplier(id);
    }

    @Override
    @Transactional
    public SupplierResponse updateStatus(Long id, UpdateSupplierStatusRequest request, String actingUsername) {
        Supplier supplier = find(id);
        var previous = supplier.getStatus();
        supplier.setStatus(request.getStatus());
        supplierRepository.save(supplier);
        if (previous != request.getStatus()) {
            recordActivity(id, SupplierActivityType.STATUS_CHANGE,
                    "Status changed from " + previous + " to " + request.getStatus(), null, actingUsername);
        }
        return getSupplier(id);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        find(id);
        supplierRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SupplierResponse adjustBalance(Long id, BalanceAdjustmentRequest request, String actingUsername) {
        Supplier supplier = find(id);
        boolean isCharge = request.getType() == BalanceAdjustmentType.CHARGE;
        BigDecimal delta = isCharge ? request.getAmount() : request.getAmount().negate();
        supplier.setCurrentBalance(supplier.getCurrentBalance().add(delta));
        supplierRepository.save(supplier);

        String description = (isCharge ? "Charged " : "Payment made ") + formatAmount(request.getAmount())
                + (request.getNote() != null && !request.getNote().isBlank() ? " — " + request.getNote() : "");
        recordActivity(id, SupplierActivityType.BALANCE_ADJUSTMENT, description, delta, actingUsername);

        return getSupplier(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierActivityResponse> listActivities(Long supplierId, SupplierActivityFilterRequest filter) {
        find(supplierId);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());
        Page<SupplierActivity> page = activityRepository.findBySupplierId(supplierId, pageable);
        return PageResponse.of(page.map(this::toActivityResponse));
    }

    @Override
    @Transactional
    public SupplierActivityResponse addNote(Long supplierId, AddSupplierNoteRequest request, String actingUsername) {
        find(supplierId);
        SupplierActivity activity = recordActivity(supplierId, SupplierActivityType.NOTE, request.getDescription(), null, actingUsername);
        return toActivityResponse(activity);
    }

    private SupplierActivity recordActivity(Long supplierId, SupplierActivityType type, String description,
                                             BigDecimal amount, String actingUsername) {
        SupplierActivity activity = SupplierActivity.builder()
                .supplierId(supplierId)
                .type(type)
                .description(description)
                .amount(amount)
                .createdBy(actingUsername)
                .build();
        return activityRepository.save(activity);
    }

    private void validateSupplierType(Long supplierTypeId) {
        if (supplierTypeId == null) return;
        if (!supplierTypeRepository.existsById(supplierTypeId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Supplier type not found with id: " + supplierTypeId);
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String formatAmount(BigDecimal amount) {
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Supplier find(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Supplier not found with id: " + id));
    }

    private SupplierResponse toResponse(Supplier supplier, String companyName, String supplierTypeName) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .companyId(supplier.getCompanyId())
                .companyName(companyName)
                .supplierTypeId(supplier.getSupplierTypeId())
                .supplierTypeName(supplierTypeName)
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .addressLine1(supplier.getAddressLine1())
                .addressLine2(supplier.getAddressLine2())
                .city(supplier.getCity())
                .state(supplier.getState())
                .postalCode(supplier.getPostalCode())
                .country(supplier.getCountry())
                .paymentTerms(supplier.getPaymentTerms().name())
                .creditLimit(supplier.getCreditLimit())
                .status(supplier.getStatus().name())
                .currentBalance(supplier.getCurrentBalance())
                .build();
    }

    private SupplierActivityResponse toActivityResponse(SupplierActivity activity) {
        return SupplierActivityResponse.builder()
                .id(activity.getId())
                .supplierId(activity.getSupplierId())
                .type(activity.getType().name())
                .description(activity.getDescription())
                .amount(activity.getAmount())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
