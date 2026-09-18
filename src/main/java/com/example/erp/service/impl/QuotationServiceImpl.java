package com.example.erp.service.impl;

import com.example.erp.dto.ConvertOpportunityToQuotationRequest;
import com.example.erp.dto.CreateQuotationRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QuotationFilterRequest;
import com.example.erp.dto.QuotationLineRequest;
import com.example.erp.dto.QuotationLineResponse;
import com.example.erp.dto.QuotationResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdateQuotationRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Opportunity;
import com.example.erp.entity.OpportunityActivity;
import com.example.erp.entity.OpportunityActivityType;
import com.example.erp.entity.OpportunityStage;
import com.example.erp.entity.Product;
import com.example.erp.entity.Quotation;
import com.example.erp.entity.QuotationLine;
import com.example.erp.entity.QuotationStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.OpportunityActivityRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.QuotationLineRepository;
import com.example.erp.repository.QuotationRepository;
import com.example.erp.service.EmailService;
import com.example.erp.service.PdfRenderService;
import com.example.erp.service.QuotationService;
import com.example.erp.util.DocumentPdfHtml;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final QuotationLineRepository quotationLineRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityActivityRepository opportunityActivityRepository;
    private final PdfRenderService pdfRenderService;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuotationResponse> listQuotations(QuotationFilterRequest filter) {
        List<Specification<Quotation>> conditions = new ArrayList<>();
        if (filter.getQuotationNumber() != null && !filter.getQuotationNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("quotationNumber")), "%" + filter.getQuotationNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getOpportunityId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("opportunityId"), filter.getOpportunityId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Quotation> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Quotation> page = quotationRepository.findAll(spec, pageable);
        List<Quotation> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Quotation::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> opportunityNames = opportunityRepository.findAllById(
                content.stream().map(Quotation::getOpportunityId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Opportunity::getId, Opportunity::getName));
        Map<Long, String> customerNames = customerRepository.findAllById(
                content.stream().map(Quotation::getCustomerId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Customer::getId, Customer::getName));

        List<Long> quotationIds = content.stream().map(Quotation::getId).toList();
        Map<Long, List<QuotationLine>> linesByQuotationId = quotationIds.isEmpty() ? Map.of() : allLinesGroupedByQuotation(quotationIds);

        return PageResponse.of(page.map(q -> toSummaryResponse(q,
                companyNames.get(q.getCompanyId()),
                q.getOpportunityId() == null ? null : opportunityNames.get(q.getOpportunityId()),
                q.getCustomerId() == null ? null : customerNames.get(q.getCustomerId()),
                linesByQuotationId.getOrDefault(q.getId(), List.of()))));
    }

    private Map<Long, List<QuotationLine>> allLinesGroupedByQuotation(List<Long> quotationIds) {
        Map<Long, List<QuotationLine>> result = new java.util.HashMap<>();
        for (Long quotationId : quotationIds) {
            result.put(quotationId, quotationLineRepository.findByQuotationId(quotationId));
        }
        return result;
    }

    @Override
    public QuotationResponse getQuotation(Long id) {
        Quotation quotation = find(id);
        List<QuotationLine> lines = quotationLineRepository.findByQuotationId(id);
        return toFullResponse(quotation, lines);
    }

    @Override
    @Transactional
    public QuotationResponse createQuotation(CreateQuotationRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        if (request.getCustomerId() != null) {
            requireCustomer(request.getCustomerId(), request.getCompanyId());
        }
        validateLineProducts(request.getLines(), request.getCompanyId());
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        Quotation quotation = Quotation.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .quotationDate(request.getQuotationDate())
                .validUntil(request.getValidUntil())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .foreignCurrency(request.getForeignCurrency())
                .exchangeRate(request.getExchangeRate())
                .build();
        quotationRepository.save(quotation);
        quotation.setQuotationNumber("QT-" + String.format("%06d", quotation.getId()));
        quotationRepository.save(quotation);

        List<QuotationLine> lines = saveLines(quotation.getId(), request.getLines());
        return toFullResponse(quotation, lines);
    }

    @Override
    @Transactional
    public QuotationResponse createFromOpportunity(Long opportunityId, ConvertOpportunityToQuotationRequest request, String actingUsername) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Opportunity not found with id: " + opportunityId));
        if (opportunity.getStage() == OpportunityStage.CLOSED_WON || opportunity.getStage() == OpportunityStage.CLOSED_LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot create a quotation for a closed opportunity");
        }
        validateLineProducts(request.getLines(), opportunity.getCompanyId());
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        Quotation quotation = Quotation.builder()
                .companyId(opportunity.getCompanyId())
                .opportunityId(opportunity.getId())
                .customerId(opportunity.getCustomerId())
                .quotationDate(request.getQuotationDate())
                .validUntil(request.getValidUntil())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .foreignCurrency(request.getForeignCurrency())
                .exchangeRate(request.getExchangeRate())
                .build();
        quotationRepository.save(quotation);
        quotation.setQuotationNumber("QT-" + String.format("%06d", quotation.getId()));
        quotationRepository.save(quotation);

        List<QuotationLine> lines = saveLines(quotation.getId(), request.getLines());

        opportunityActivityRepository.save(OpportunityActivity.builder()
                .opportunityId(opportunity.getId())
                .type(OpportunityActivityType.QUOTATION_CREATED)
                .description("Quotation " + quotation.getQuotationNumber() + " created")
                .createdBy(actingUsername)
                .build());

        return toFullResponse(quotation, lines);
    }

    @Override
    @Transactional
    public QuotationResponse updateQuotation(Long id, UpdateQuotationRequest request) {
        Quotation quotation = find(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft quotations can be edited");
        }
        if (request.getCustomerId() != null) {
            requireCustomer(request.getCustomerId(), quotation.getCompanyId());
        }
        validateLineProducts(request.getLines(), quotation.getCompanyId());
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        quotation.setCustomerId(request.getCustomerId());
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setNotes(request.getNotes());
        quotation.setForeignCurrency(request.getForeignCurrency());
        quotation.setExchangeRate(request.getExchangeRate());
        quotationRepository.save(quotation);

        quotationLineRepository.deleteByQuotationId(id);
        List<QuotationLine> lines = saveLines(id, request.getLines());
        return toFullResponse(quotation, lines);
    }

    @Override
    @Transactional
    public QuotationResponse sendQuotation(Long id) {
        Quotation quotation = find(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft quotations can be sent");
        }
        quotation.setStatus(QuotationStatus.SENT);
        quotationRepository.save(quotation);
        return toFullResponse(quotation, quotationLineRepository.findByQuotationId(id));
    }

    @Override
    @Transactional
    public QuotationResponse acceptQuotation(Long id) {
        Quotation quotation = find(id);
        if (quotation.getStatus() != QuotationStatus.SENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only sent quotations can be accepted");
        }
        quotation.setStatus(QuotationStatus.ACCEPTED);
        quotationRepository.save(quotation);
        return toFullResponse(quotation, quotationLineRepository.findByQuotationId(id));
    }

    @Override
    @Transactional
    public QuotationResponse rejectQuotation(Long id) {
        Quotation quotation = find(id);
        if (quotation.getStatus() != QuotationStatus.SENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only sent quotations can be rejected");
        }
        quotation.setStatus(QuotationStatus.REJECTED);
        quotationRepository.save(quotation);
        return toFullResponse(quotation, quotationLineRepository.findByQuotationId(id));
    }

    @Override
    @Transactional
    public void deleteQuotation(Long id) {
        Quotation quotation = find(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft quotations can be deleted");
        }
        quotationLineRepository.deleteByQuotationId(id);
        quotationRepository.deleteById(id);
    }

    @Override
    public void emailQuotation(Long id, SendDocumentEmailRequest request) {
        QuotationResponse quotation = getQuotation(id);
        Customer customer = quotation.getCustomerId() != null
                ? customerRepository.findById(quotation.getCustomerId()).orElse(null) : null;
        String to = request.getTo() != null && !request.getTo().isBlank() ? request.getTo()
                : customer != null ? customer.getEmail() : null;
        if (to == null || to.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer has no email on file — provide one to send to");
        }
        Company company = companyRepository.findById(quotation.getCompanyId()).orElse(null);
        String html = buildQuotationHtml(company, quotation);
        byte[] pdf = pdfRenderService.renderHtmlToPdf(html);

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject() : "Quotation " + quotation.getQuotationNumber();
        String body = request.getMessage() != null && !request.getMessage().isBlank()
                ? request.getMessage() : "Please find attached quotation " + quotation.getQuotationNumber() + ".";
        emailService.sendWithAttachment(to, subject, body, pdf, quotation.getQuotationNumber() + ".pdf", "application/pdf");
    }

    private String buildQuotationHtml(Company company, QuotationResponse quotation) {
        String metaHtml = "Date: " + quotation.getQuotationDate()
                + (quotation.getValidUntil() != null ? "<br/>Valid until: " + quotation.getValidUntil() : "");

        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr><th>Product</th><th>Qty</th><th class=\"num\">Unit price</th>")
                .append("<th class=\"num\">Line total</th></tr></thead><tbody>");
        for (QuotationLineResponse line : quotation.getLines()) {
            table.append("<tr><td>").append(DocumentPdfHtml.escape(line.getProductName())).append("</td>")
                    .append("<td>").append(line.getQuantity()).append("</td>")
                    .append("<td class=\"num\">").append(line.getUnitPrice()).append("</td>")
                    .append("<td class=\"num\">").append(line.getLineTotal()).append("</td></tr>");
        }
        table.append("</tbody></table>");

        StringBuilder totals = new StringBuilder("<div class=\"totals\">");
        totals.append("<div class=\"grand\"><span>Total</span><span>").append(quotation.getTotalAmount()).append("</span></div>");
        totals.append("</div>");

        String partyHtml = DocumentPdfHtml.escape(quotation.getCustomerName());

        return DocumentPdfHtml.render(company, "QUOTATION", quotation.getQuotationNumber(), metaHtml,
                "Prepared for", partyHtml, table.toString(), totals.toString());
    }

    private List<QuotationLine> saveLines(Long quotationId, List<QuotationLineRequest> requests) {
        List<QuotationLine> lines = requests.stream()
                .map(r -> QuotationLine.builder()
                        .quotationId(quotationId)
                        .productId(r.getProductId())
                        .quantity(r.getQuantity())
                        .unitPrice(r.getUnitPrice())
                        .build())
                .toList();
        return quotationLineRepository.saveAll(lines);
    }

    private void validateLineProducts(List<QuotationLineRequest> lines, Long companyId) {
        List<Long> productIds = lines.stream().map(QuotationLineRequest::getProductId).distinct().toList();
        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (Long productId : productIds) {
            Product product = products.get(productId);
            if (product == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId);
            }
            if (!product.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
            }
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireCustomer(Long customerId, Long companyId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company");
        }
    }

    private Quotation find(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quotation not found with id: " + id));
    }

    private QuotationResponse toSummaryResponse(Quotation quotation, String companyName, String opportunityName,
                                                 String customerName, List<QuotationLine> lines) {
        return baseResponseBuilder(quotation, companyName, opportunityName, customerName, lines).lines(null).build();
    }

    private QuotationResponse toFullResponse(Quotation quotation, List<QuotationLine> lines) {
        String companyName = companyRepository.findById(quotation.getCompanyId()).map(Company::getName).orElse(null);
        String opportunityName = quotation.getOpportunityId() == null ? null
                : opportunityRepository.findById(quotation.getOpportunityId()).map(Opportunity::getName).orElse(null);
        String customerName = quotation.getCustomerId() == null ? null
                : customerRepository.findById(quotation.getCustomerId()).map(Customer::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(QuotationLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<QuotationLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return QuotationLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .lineTotal(line.getQuantity().multiply(line.getUnitPrice()))
                            .build();
                })
                .toList();

        return baseResponseBuilder(quotation, companyName, opportunityName, customerName, lines)
                .lines(lineResponses)
                .build();
    }

    private QuotationResponse.QuotationResponseBuilder baseResponseBuilder(
            Quotation quotation, String companyName, String opportunityName, String customerName, List<QuotationLine> lines) {
        BigDecimal totalAmount = lines.stream()
                .map(l -> l.getQuantity().multiply(l.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal foreignTotalAmount = quotation.getExchangeRate() != null
                ? totalAmount.divide(quotation.getExchangeRate(), 4, RoundingMode.HALF_UP)
                : null;
        return QuotationResponse.builder()
                .id(quotation.getId())
                .companyId(quotation.getCompanyId())
                .companyName(companyName)
                .opportunityId(quotation.getOpportunityId())
                .opportunityName(opportunityName)
                .customerId(quotation.getCustomerId())
                .customerName(customerName)
                .quotationNumber(quotation.getQuotationNumber())
                .quotationDate(quotation.getQuotationDate())
                .validUntil(quotation.getValidUntil())
                .status(quotation.getStatus().name())
                .notes(quotation.getNotes())
                .createdBy(quotation.getCreatedBy())
                .totalAmount(totalAmount)
                .foreignCurrency(quotation.getForeignCurrency())
                .exchangeRate(quotation.getExchangeRate())
                .foreignTotalAmount(foreignTotalAmount);
    }

    private void requireForeignCurrencyPair(String foreignCurrency, BigDecimal exchangeRate) {
        boolean hasCurrency = foreignCurrency != null && !foreignCurrency.isBlank();
        boolean hasRate = exchangeRate != null;
        if (hasCurrency != hasRate) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Both foreign currency and exchange rate are required together");
        }
    }
}
