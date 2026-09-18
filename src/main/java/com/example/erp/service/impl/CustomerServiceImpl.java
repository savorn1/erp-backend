package com.example.erp.service.impl;

import com.example.erp.dto.AddCustomerNoteRequest;
import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateCustomerRequest;
import com.example.erp.dto.CustomerActivityFilterRequest;
import com.example.erp.dto.CustomerActivityResponse;
import com.example.erp.dto.CustomerFilterRequest;
import com.example.erp.dto.CustomerResponse;
import com.example.erp.dto.ImportResultResponse;
import com.example.erp.dto.ImportRowError;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCustomerRequest;
import com.example.erp.dto.UpdateCustomerStatusRequest;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.CustomerActivity;
import com.example.erp.entity.CustomerActivityType;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.CustomerType;
import com.example.erp.entity.PaymentTerms;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerActivityRepository;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.CustomerTypeRepository;
import com.example.erp.service.CustomerService;
import com.example.erp.util.CsvUtils;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerActivityRepository activityRepository;
    private final CompanyRepository companyRepository;
    private final CustomerTypeRepository customerTypeRepository;
    private final CustomerGroupRepository customerGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> listCustomers(CustomerFilterRequest filter) {
        List<Specification<Customer>> conditions = new ArrayList<>();
        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerTypeId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerTypeId"), filter.getCustomerTypeId()));
        }
        if (filter.getCustomerGroupId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerGroupId"), filter.getCustomerGroupId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Customer> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Customer> page = customerRepository.findAll(spec, pageable);
        List<Customer> content = page.getContent();

        // Batch-resolved rather than looked up per row — same reasoning as
        // BranchServiceImpl/ProductServiceImpl (N+1 avoidance on plain FK columns).
        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Customer::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> typeNames = customerTypeRepository.findAllById(
                content.stream().map(Customer::getCustomerTypeId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(CustomerType::getId, CustomerType::getName));
        Map<Long, String> groupNames = customerGroupRepository.findAllById(
                content.stream().map(Customer::getCustomerGroupId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(CustomerGroup::getId, CustomerGroup::getName));

        return PageResponse.of(page.map(c -> toResponse(c,
                companyNames.get(c.getCompanyId()),
                c.getCustomerTypeId() == null ? null : typeNames.get(c.getCustomerTypeId()),
                c.getCustomerGroupId() == null ? null : groupNames.get(c.getCustomerGroupId()))));
    }

    @Override
    public CustomerResponse getCustomer(Long id) {
        Customer customer = find(id);
        return toResponse(customer,
                companyNameOf(customer.getCompanyId()),
                customer.getCustomerTypeId() == null ? null : customerTypeRepository.findById(customer.getCustomerTypeId()).map(CustomerType::getName).orElse(null),
                customer.getCustomerGroupId() == null ? null : customerGroupRepository.findById(customer.getCustomerGroupId()).map(CustomerGroup::getName).orElse(null));
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String actingUsername) {
        Company company = requireCompany(request.getCompanyId());
        validateReferences(request.getCustomerTypeId(), request.getCustomerGroupId());
        if (customerRepository.existsByCompanyIdAndName(request.getCompanyId(), request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Customer name already taken in this company: " + request.getName());
        }

        Customer customer = Customer.builder()
                .companyId(request.getCompanyId())
                .customerTypeId(request.getCustomerTypeId())
                .customerGroupId(request.getCustomerGroupId())
                .name(request.getName())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .billingAddressLine1(request.getBillingAddressLine1())
                .billingAddressLine2(request.getBillingAddressLine2())
                .billingCity(request.getBillingCity())
                .billingState(request.getBillingState())
                .billingPostalCode(request.getBillingPostalCode())
                .billingCountry(request.getBillingCountry())
                .shippingAddressLine1(request.getShippingAddressLine1())
                .shippingAddressLine2(request.getShippingAddressLine2())
                .shippingCity(request.getShippingCity())
                .shippingState(request.getShippingState())
                .shippingPostalCode(request.getShippingPostalCode())
                .shippingCountry(request.getShippingCountry())
                .creditLimit(request.getCreditLimit())
                .paymentTerms(request.getPaymentTerms())
                .build();
        customerRepository.save(customer);

        recordActivity(customer.getId(), CustomerActivityType.CREATED, "Customer created", null, actingUsername);

        return getCustomer(customer.getId());
    }

    @Override
    public ImportResultResponse importCustomersFromCsv(MultipartFile file, Long companyId, String actingUsername) {
        requireCompany(companyId);
        List<CSVRecord> records = CsvUtils.parse(file);
        List<ImportRowError> errors = new ArrayList<>();
        int successCount = 0;

        for (CSVRecord record : records) {
            int rowNumber = (int) record.getRecordNumber() + 1;
            try {
                CreateCustomerRequest request = new CreateCustomerRequest();
                request.setCompanyId(companyId);
                request.setName(CsvUtils.getRequired(record, "name"));
                request.setContactName(CsvUtils.getOptional(record, "contactName"));
                request.setPhone(CsvUtils.getOptional(record, "phone"));
                request.setEmail(CsvUtils.getOptional(record, "email"));
                String customerTypeName = CsvUtils.getOptional(record, "customerType");
                if (customerTypeName != null) {
                    CustomerType type = customerTypeRepository.findByNameIgnoreCase(customerTypeName)
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer type not found: " + customerTypeName));
                    request.setCustomerTypeId(type.getId());
                }
                String customerGroupName = CsvUtils.getOptional(record, "customerGroup");
                if (customerGroupName != null) {
                    CustomerGroup group = customerGroupRepository.findByNameIgnoreCase(customerGroupName)
                            .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer group not found: " + customerGroupName));
                    request.setCustomerGroupId(group.getId());
                }
                request.setBillingAddressLine1(CsvUtils.getOptional(record, "billingAddressLine1"));
                request.setBillingAddressLine2(CsvUtils.getOptional(record, "billingAddressLine2"));
                request.setBillingCity(CsvUtils.getOptional(record, "billingCity"));
                request.setBillingState(CsvUtils.getOptional(record, "billingState"));
                request.setBillingPostalCode(CsvUtils.getOptional(record, "billingPostalCode"));
                request.setBillingCountry(CsvUtils.getOptional(record, "billingCountry"));
                request.setShippingAddressLine1(CsvUtils.getOptional(record, "shippingAddressLine1"));
                request.setShippingAddressLine2(CsvUtils.getOptional(record, "shippingAddressLine2"));
                request.setShippingCity(CsvUtils.getOptional(record, "shippingCity"));
                request.setShippingState(CsvUtils.getOptional(record, "shippingState"));
                request.setShippingPostalCode(CsvUtils.getOptional(record, "shippingPostalCode"));
                request.setShippingCountry(CsvUtils.getOptional(record, "shippingCountry"));
                request.setCreditLimit(CsvUtils.getDecimal(record, "creditLimit", BigDecimal.ZERO));
                String paymentTerms = CsvUtils.getOptional(record, "paymentTerms");
                if (paymentTerms != null) {
                    request.setPaymentTerms(PaymentTerms.valueOf(paymentTerms.toUpperCase()));
                }

                createCustomer(request, actingUsername);
                successCount++;
            } catch (Exception e) {
                errors.add(ImportRowError.builder().rowNumber(rowNumber).message(e.getMessage()).build());
            }
        }

        return ImportResultResponse.builder()
                .totalRows(records.size())
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = find(id);
        requireCompany(request.getCompanyId());
        validateReferences(request.getCustomerTypeId(), request.getCustomerGroupId());
        if (customerRepository.existsByCompanyIdAndNameAndIdNot(request.getCompanyId(), request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Customer name already taken in this company: " + request.getName());
        }

        customer.setCompanyId(request.getCompanyId());
        customer.setCustomerTypeId(request.getCustomerTypeId());
        customer.setCustomerGroupId(request.getCustomerGroupId());
        customer.setName(request.getName());
        customer.setContactName(request.getContactName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setBillingAddressLine1(request.getBillingAddressLine1());
        customer.setBillingAddressLine2(request.getBillingAddressLine2());
        customer.setBillingCity(request.getBillingCity());
        customer.setBillingState(request.getBillingState());
        customer.setBillingPostalCode(request.getBillingPostalCode());
        customer.setBillingCountry(request.getBillingCountry());
        customer.setShippingAddressLine1(request.getShippingAddressLine1());
        customer.setShippingAddressLine2(request.getShippingAddressLine2());
        customer.setShippingCity(request.getShippingCity());
        customer.setShippingState(request.getShippingState());
        customer.setShippingPostalCode(request.getShippingPostalCode());
        customer.setShippingCountry(request.getShippingCountry());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setPaymentTerms(request.getPaymentTerms());
        customerRepository.save(customer);
        return getCustomer(id);
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(Long id, UpdateCustomerStatusRequest request, String actingUsername) {
        Customer customer = find(id);
        var previous = customer.getStatus();
        customer.setStatus(request.getStatus());
        customerRepository.save(customer);
        if (previous != request.getStatus()) {
            recordActivity(id, CustomerActivityType.STATUS_CHANGE,
                    "Status changed from " + previous + " to " + request.getStatus(), null, actingUsername);
        }
        return getCustomer(id);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        find(id);
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CustomerResponse adjustBalance(Long id, BalanceAdjustmentRequest request, String actingUsername) {
        Customer customer = find(id);
        boolean isCharge = request.getType() == BalanceAdjustmentType.CHARGE;
        BigDecimal delta = isCharge ? request.getAmount() : request.getAmount().negate();
        customer.setCurrentBalance(customer.getCurrentBalance().add(delta));
        customerRepository.save(customer);

        String description = (isCharge ? "Charged " : "Payment received ") + formatAmount(request.getAmount())
                + (request.getNote() != null && !request.getNote().isBlank() ? " — " + request.getNote() : "");
        recordActivity(id, CustomerActivityType.BALANCE_ADJUSTMENT, description, delta, actingUsername);

        return getCustomer(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerActivityResponse> listActivities(Long customerId, CustomerActivityFilterRequest filter) {
        find(customerId);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());
        Page<CustomerActivity> page = activityRepository.findByCustomerId(customerId, pageable);
        return PageResponse.of(page.map(this::toActivityResponse));
    }

    @Override
    @Transactional
    public CustomerActivityResponse addNote(Long customerId, AddCustomerNoteRequest request, String actingUsername) {
        find(customerId);
        CustomerActivity activity = recordActivity(customerId, CustomerActivityType.NOTE, request.getDescription(), null, actingUsername);
        return toActivityResponse(activity);
    }

    private CustomerActivity recordActivity(Long customerId, CustomerActivityType type, String description,
                                             BigDecimal amount, String actingUsername) {
        CustomerActivity activity = CustomerActivity.builder()
                .customerId(customerId)
                .type(type)
                .description(description)
                .amount(amount)
                .createdBy(actingUsername)
                .build();
        return activityRepository.save(activity);
    }

    private void validateReferences(Long customerTypeId, Long customerGroupId) {
        if (customerTypeId != null && !customerTypeRepository.existsById(customerTypeId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer type not found with id: " + customerTypeId);
        }
        if (customerGroupId != null && !customerGroupRepository.existsById(customerGroupId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer group not found with id: " + customerGroupId);
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

    private Customer find(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Customer not found with id: " + id));
    }

    private CustomerResponse toResponse(Customer customer, String companyName, String customerTypeName, String customerGroupName) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .companyId(customer.getCompanyId())
                .companyName(companyName)
                .customerTypeId(customer.getCustomerTypeId())
                .customerTypeName(customerTypeName)
                .customerGroupId(customer.getCustomerGroupId())
                .customerGroupName(customerGroupName)
                .name(customer.getName())
                .contactName(customer.getContactName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .billingAddressLine1(customer.getBillingAddressLine1())
                .billingAddressLine2(customer.getBillingAddressLine2())
                .billingCity(customer.getBillingCity())
                .billingState(customer.getBillingState())
                .billingPostalCode(customer.getBillingPostalCode())
                .billingCountry(customer.getBillingCountry())
                .shippingAddressLine1(customer.getShippingAddressLine1())
                .shippingAddressLine2(customer.getShippingAddressLine2())
                .shippingCity(customer.getShippingCity())
                .shippingState(customer.getShippingState())
                .shippingPostalCode(customer.getShippingPostalCode())
                .shippingCountry(customer.getShippingCountry())
                .creditLimit(customer.getCreditLimit())
                .paymentTerms(customer.getPaymentTerms().name())
                .status(customer.getStatus().name())
                .currentBalance(customer.getCurrentBalance())
                .build();
    }

    private CustomerActivityResponse toActivityResponse(CustomerActivity activity) {
        return CustomerActivityResponse.builder()
                .id(activity.getId())
                .customerId(activity.getCustomerId())
                .type(activity.getType().name())
                .description(activity.getDescription())
                .amount(activity.getAmount())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
