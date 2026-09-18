package com.example.erp.service.impl;

import com.example.erp.dto.CreateInvoiceRequest;
import com.example.erp.dto.CreateRecurringInvoiceTemplateRequest;
import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.GenerateDueInvoicesResponse;
import com.example.erp.dto.GenerateDueInvoicesResult;
import com.example.erp.dto.InvoiceResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecurringInvoiceTemplateFilterRequest;
import com.example.erp.dto.RecurringInvoiceTemplateLineRequest;
import com.example.erp.dto.RecurringInvoiceTemplateLineResponse;
import com.example.erp.dto.RecurringInvoiceTemplateResponse;
import com.example.erp.dto.SalesOrderLineRequest;
import com.example.erp.dto.SalesOrderResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdateRecurringInvoiceTemplateRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.PaymentTerms;
import com.example.erp.entity.Product;
import com.example.erp.entity.RecurringInvoiceFrequency;
import com.example.erp.entity.RecurringInvoiceTemplate;
import com.example.erp.entity.RecurringInvoiceTemplateLine;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.RecurringInvoiceTemplateLineRepository;
import com.example.erp.repository.RecurringInvoiceTemplateRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.InvoiceService;
import com.example.erp.service.RecurringInvoiceService;
import com.example.erp.service.SalesOrderService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringInvoiceServiceImpl implements RecurringInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(RecurringInvoiceServiceImpl.class);

    private final RecurringInvoiceTemplateRepository templateRepository;
    private final RecurringInvoiceTemplateLineRepository lineRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SalesOrderService salesOrderService;
    private final InvoiceService invoiceService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecurringInvoiceTemplateResponse> listTemplates(RecurringInvoiceTemplateFilterRequest filter) {
        List<Specification<RecurringInvoiceTemplate>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<RecurringInvoiceTemplate> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<RecurringInvoiceTemplate> page = templateRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public RecurringInvoiceTemplateResponse getTemplate(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public RecurringInvoiceTemplateResponse createTemplate(CreateRecurringInvoiceTemplateRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireCustomer(request.getCustomerId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());

        RecurringInvoiceTemplate template = RecurringInvoiceTemplate.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .warehouseId(request.getWarehouseId())
                .name(request.getName())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .nextRunDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .autoApproveInvoice(request.getAutoApproveInvoice() == null || request.getAutoApproveInvoice())
                .autoEmailInvoice(request.getAutoEmailInvoice() != null && request.getAutoEmailInvoice())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        templateRepository.save(template);

        List<RecurringInvoiceTemplateLine> lines = saveLines(template.getId(), request.getLines());
        return toResponse(template, lines);
    }

    @Override
    @Transactional
    public RecurringInvoiceTemplateResponse updateTemplate(Long id, UpdateRecurringInvoiceTemplateRequest request) {
        RecurringInvoiceTemplate template = find(id);
        requireCustomer(request.getCustomerId(), template.getCompanyId());
        requireWarehouse(request.getWarehouseId(), template.getCompanyId());
        validateLineProducts(request.getLines(), template.getCompanyId());

        template.setCustomerId(request.getCustomerId());
        template.setWarehouseId(request.getWarehouseId());
        template.setName(request.getName());
        template.setFrequency(request.getFrequency());
        template.setNextRunDate(request.getNextRunDate());
        template.setEndDate(request.getEndDate());
        template.setActive(request.isActive());
        template.setAutoApproveInvoice(request.isAutoApproveInvoice());
        template.setAutoEmailInvoice(request.isAutoEmailInvoice());
        template.setNotes(request.getNotes());
        templateRepository.save(template);

        lineRepository.deleteByTemplateId(id);
        List<RecurringInvoiceTemplateLine> lines = saveLines(id, request.getLines());
        return toResponse(template, lines);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        find(id);
        lineRepository.deleteByTemplateId(id);
        templateRepository.deleteById(id);
    }

    @Override
    public GenerateDueInvoicesResponse generateDueInvoices(Long companyId, String actingUsername) {
        LocalDate today = LocalDate.now();
        List<RecurringInvoiceTemplate> due = companyId != null
                ? templateRepository.findByActiveTrueAndNextRunDateLessThanEqualAndCompanyId(today, companyId)
                : templateRepository.findByActiveTrueAndNextRunDateLessThanEqual(today);

        List<GenerateDueInvoicesResult> results = new ArrayList<>();
        int successCount = 0;
        for (RecurringInvoiceTemplate template : due) {
            try {
                results.add(generateOne(template, today, actingUsername));
                successCount++;
            } catch (Exception e) {
                results.add(GenerateDueInvoicesResult.builder()
                        .templateId(template.getId())
                        .templateName(template.getName())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        return GenerateDueInvoicesResponse.builder()
                .processedCount(due.size())
                .successCount(successCount)
                .failureCount(due.size() - successCount)
                .results(results)
                .build();
    }

    // Not @Transactional: each nested call (salesOrderService/invoiceService)
    // already commits through its own service-level transaction, so one
    // template's failure can never roll back another's already-saved work —
    // the same per-item isolation CSV import relies on.
    private GenerateDueInvoicesResult generateOne(RecurringInvoiceTemplate template, LocalDate today, String actingUsername) {
        List<RecurringInvoiceTemplateLine> lines = lineRepository.findByTemplateId(template.getId());

        CreateSalesOrderRequest soRequest = new CreateSalesOrderRequest();
        soRequest.setCompanyId(template.getCompanyId());
        soRequest.setCustomerId(template.getCustomerId());
        soRequest.setWarehouseId(template.getWarehouseId());
        soRequest.setOrderDate(today);
        soRequest.setNotes("Generated from recurring invoice template: " + template.getName());
        soRequest.setLines(lines.stream().map(l -> {
            SalesOrderLineRequest lineRequest = new SalesOrderLineRequest();
            lineRequest.setProductId(l.getProductId());
            lineRequest.setQuantityOrdered(l.getQuantity());
            lineRequest.setUnitPrice(l.getUnitPrice());
            lineRequest.setDiscountPercent(l.getDiscountPercent());
            lineRequest.setTaxRate(l.getTaxRate());
            return lineRequest;
        }).toList());

        SalesOrderResponse so = salesOrderService.createSalesOrder(soRequest, actingUsername);
        salesOrderService.submitSalesOrder(so.getId());
        salesOrderService.approveSalesOrder(so.getId(), actingUsername);

        Customer customer = customerRepository.findById(template.getCustomerId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + template.getCustomerId()));
        LocalDate dueDate = today.plusDays(dueDateOffsetDays(customer.getPaymentTerms()));

        CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
        invoiceRequest.setInvoiceDate(today);
        invoiceRequest.setDueDate(dueDate);
        InvoiceResponse invoice = invoiceService.createFromSalesOrder(so.getId(), invoiceRequest, actingUsername);

        if (template.isAutoApproveInvoice()) {
            invoiceService.approveInvoice(invoice.getId(), actingUsername);
        }
        if (template.isAutoEmailInvoice()) {
            try {
                invoiceService.emailInvoice(invoice.getId(), new SendDocumentEmailRequest());
            } catch (Exception e) {
                log.warn("Recurring invoice {} generated but the auto-email failed: {}", invoice.getInvoiceNumber(), e.getMessage());
            }
        }

        LocalDate nextRunDate = advance(template.getNextRunDate(), template.getFrequency());
        template.setNextRunDate(nextRunDate);
        template.setLastGeneratedDate(today);
        if (template.getEndDate() != null && nextRunDate.isAfter(template.getEndDate())) {
            template.setActive(false);
        }
        templateRepository.save(template);

        return GenerateDueInvoicesResult.builder()
                .templateId(template.getId())
                .templateName(template.getName())
                .success(true)
                .invoiceNumber(invoice.getInvoiceNumber())
                .build();
    }

    private LocalDate advance(LocalDate date, RecurringInvoiceFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
        };
    }

    private int dueDateOffsetDays(PaymentTerms terms) {
        if (terms == null) return 30;
        return switch (terms) {
            case DUE_ON_RECEIPT, COD -> 0;
            case NET_15 -> 15;
            case NET_30 -> 30;
            case NET_45 -> 45;
            case NET_60 -> 60;
        };
    }

    private List<RecurringInvoiceTemplateLine> saveLines(Long templateId, List<RecurringInvoiceTemplateLineRequest> requests) {
        List<RecurringInvoiceTemplateLine> lines = requests.stream()
                .map(r -> RecurringInvoiceTemplateLine.builder()
                        .templateId(templateId)
                        .productId(r.getProductId())
                        .quantity(r.getQuantity())
                        .unitPrice(r.getUnitPrice())
                        .discountPercent(r.getDiscountPercent())
                        .taxRate(r.getTaxRate())
                        .build())
                .toList();
        return lineRepository.saveAll(lines);
    }

    private void validateLineProducts(List<RecurringInvoiceTemplateLineRequest> lines, Long companyId) {
        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(RecurringInvoiceTemplateLineRequest::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        for (RecurringInvoiceTemplateLineRequest line : lines) {
            Product product = products.get(line.getProductId());
            if (product == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + line.getProductId());
            }
            if (!product.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product " + product.getSku() + " does not belong to the selected company");
            }
        }
    }

    private RecurringInvoiceTemplate find(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Recurring invoice template not found with id: " + id));
    }

    private RecurringInvoiceTemplateResponse toResponse(RecurringInvoiceTemplate template) {
        return toResponse(template, lineRepository.findByTemplateId(template.getId()));
    }

    private RecurringInvoiceTemplateResponse toResponse(RecurringInvoiceTemplate template, List<RecurringInvoiceTemplateLine> lines) {
        String companyName = companyRepository.findById(template.getCompanyId()).map(Company::getName).orElse(null);
        String customerName = customerRepository.findById(template.getCustomerId()).map(Customer::getName).orElse(null);
        String warehouseName = warehouseRepository.findById(template.getWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(RecurringInvoiceTemplateLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<RecurringInvoiceTemplateLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return RecurringInvoiceTemplateLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .taxRate(line.getTaxRate())
                            .build();
                })
                .toList();

        return RecurringInvoiceTemplateResponse.builder()
                .id(template.getId())
                .companyId(template.getCompanyId())
                .companyName(companyName)
                .customerId(template.getCustomerId())
                .customerName(customerName)
                .warehouseId(template.getWarehouseId())
                .warehouseName(warehouseName)
                .name(template.getName())
                .frequency(template.getFrequency())
                .startDate(template.getStartDate())
                .nextRunDate(template.getNextRunDate())
                .lastGeneratedDate(template.getLastGeneratedDate())
                .endDate(template.getEndDate())
                .active(template.isActive())
                .autoApproveInvoice(template.isAutoApproveInvoice())
                .autoEmailInvoice(template.isAutoEmailInvoice())
                .notes(template.getNotes())
                .createdBy(template.getCreatedBy())
                .lines(lineResponses)
                .build();
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireCustomer(Long customerId, Long companyId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company");
        }
    }

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }
}
