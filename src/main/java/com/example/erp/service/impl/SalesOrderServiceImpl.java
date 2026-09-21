package com.example.erp.service.impl;

import com.example.erp.dto.CreateSalesOrderFromQuotationRequest;
import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SalesOrderFilterRequest;
import com.example.erp.dto.SalesOrderLineRequest;
import com.example.erp.dto.SalesOrderLineResponse;
import com.example.erp.dto.SalesOrderResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdateSalesOrderRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.Lead;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Quotation;
import com.example.erp.entity.QuotationLine;
import com.example.erp.entity.QuotationStatus;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.User;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductPriceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.QuotationLineRepository;
import com.example.erp.repository.QuotationRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.EmailService;
import com.example.erp.service.InventorySettingsService;
import com.example.erp.service.PdfRenderService;
import com.example.erp.service.SalesOrderService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository lineRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final ProductUomRepository productUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final StockLevelRepository stockLevelRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final ProductPriceRepository productPriceRepository;
    private final PriceGroupRepository priceGroupRepository;
    private final StockAvailabilityService stockAvailabilityService;
    private final InventorySettingsService inventorySettingsService;
    private final ApprovalWorkflowService approvalWorkflowService;
    private final QuotationRepository quotationRepository;
    private final QuotationLineRepository quotationLineRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final PdfRenderService pdfRenderService;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SalesOrderResponse> listSalesOrders(SalesOrderFilterRequest filter) {
        List<Specification<SalesOrder>> conditions = new ArrayList<>();
        if (filter.getSoNumber() != null && !filter.getSoNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("soNumber")), "%" + filter.getSoNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<SalesOrder> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<SalesOrder> page = salesOrderRepository.findAll(spec, pageable);
        List<SalesOrder> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(SalesOrder::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> customerNames = customerRepository.findAllById(
                content.stream().map(SalesOrder::getCustomerId).distinct().toList()
        ).stream().collect(Collectors.toMap(Customer::getId, Customer::getName));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(SalesOrder::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        Map<Long, List<SalesOrderLine>> linesBySoId = new HashMap<>();
        for (SalesOrder so : content) {
            linesBySoId.put(so.getId(), lineRepository.findBySalesOrderId(so.getId()));
        }

        return PageResponse.of(page.map(so -> toSummaryResponse(so,
                companyNames.get(so.getCompanyId()),
                customerNames.get(so.getCustomerId()),
                warehouseNames.get(so.getWarehouseId()),
                linesBySoId.getOrDefault(so.getId(), List.of()))));
    }

    @Override
    public SalesOrderResponse getSalesOrder(Long id) {
        SalesOrder so = find(id);
        List<SalesOrderLine> lines = lineRepository.findBySalesOrderId(id);
        return toFullResponse(so, lines);
    }

    @Override
    @Transactional
    public SalesOrderResponse createSalesOrder(CreateSalesOrderRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireCustomer(request.getCustomerId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());
        if (request.getSalesRepUserId() != null) {
            requireSalesRep(request.getSalesRepUserId(), request.getCompanyId());
        }

        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());
        SalesOrder so = SalesOrder.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .warehouseId(request.getWarehouseId())
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .salesRepUserId(request.getSalesRepUserId())
                .foreignCurrency(request.getForeignCurrency())
                .exchangeRate(request.getExchangeRate())
                .createdBy(actingUsername)
                .build();
        salesOrderRepository.save(so);
        so.setSoNumber("SO-" + String.format("%06d", so.getId()));
        salesOrderRepository.save(so);

        List<SalesOrderLine> lines = saveLines(so.getId(), request.getCustomerId(), request.getLines());
        return toFullResponse(so, lines);
    }

    // Mirrors QuotationServiceImpl.createFromLead one hop further down
    // the pipeline — the one conversion this codebase was missing. Lives here
    // (not on QuotationService) so the target document's own service owns its
    // own construction, same as createSalesOrder itself; QuotationController
    // exposes the trigger endpoint since the source document is where the
    // "convert" action naturally lives.
    @Override
    @Transactional
    public SalesOrderResponse createFromSalesQuotation(Long quotationId, CreateSalesOrderFromQuotationRequest request, String actingUsername) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Quotation not found with id: " + quotationId));
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only accepted quotations can be converted to a sales order");
        }
        requireQuotationNotExpired(quotation);
        if (quotation.getCustomerId() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quotation has no customer to create a sales order for");
        }
        requireWarehouse(request.getWarehouseId(), quotation.getCompanyId());

        List<QuotationLine> quotationLines = quotationLineRepository.findByQuotationId(quotationId);
        if (quotationLines.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quotation has no lines to convert");
        }

        Long salesRepUserId = request.getSalesRepUserId();
        if (salesRepUserId == null && quotation.getLeadId() != null) {
            salesRepUserId = leadRepository.findById(quotation.getLeadId())
                    .map(Lead::getAssignedToUserId)
                    .orElse(null);
        }
        if (salesRepUserId != null) {
            requireSalesRep(salesRepUserId, quotation.getCompanyId());
        }

        SalesOrder so = SalesOrder.builder()
                .companyId(quotation.getCompanyId())
                .customerId(quotation.getCustomerId())
                .quotationId(quotation.getId())
                .warehouseId(request.getWarehouseId())
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .salesRepUserId(salesRepUserId)
                .foreignCurrency(quotation.getForeignCurrency())
                .exchangeRate(quotation.getExchangeRate())
                .createdBy(actingUsername)
                .build();
        salesOrderRepository.save(so);
        so.setSoNumber("SO-" + String.format("%06d", so.getId()));
        salesOrderRepository.save(so);

        List<SalesOrderLineRequest> lineRequests = quotationLines.stream()
                .map(l -> {
                    SalesOrderLineRequest lineRequest = new SalesOrderLineRequest();
                    lineRequest.setProductId(l.getProductId());
                    // Carry the quoted unit across — the price was quoted per this unit,
                    // so dropping it here would silently reprice the order.
                    lineRequest.setUnitOfMeasureId(l.getUnitOfMeasureId());
                    lineRequest.setQuantityOrdered(l.getQuantity());
                    lineRequest.setUnitPrice(l.getUnitPrice());
                    return lineRequest;
                })
                .toList();
        List<SalesOrderLine> lines = saveLines(so.getId(), quotation.getCustomerId(), lineRequests);
        return toFullResponse(so, lines);
    }

    @Override
    @Transactional
    public SalesOrderResponse updateSalesOrder(Long id, UpdateSalesOrderRequest request) {
        SalesOrder so = find(id);
        if (so.getStatus() != SalesOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft sales orders can be edited");
        }
        requireCompany(request.getCompanyId());
        requireCustomer(request.getCustomerId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());
        if (request.getSalesRepUserId() != null) {
            requireSalesRep(request.getSalesRepUserId(), request.getCompanyId());
        }
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        so.setCompanyId(request.getCompanyId());
        so.setCustomerId(request.getCustomerId());
        so.setWarehouseId(request.getWarehouseId());
        so.setOrderDate(request.getOrderDate());
        so.setExpectedDate(request.getExpectedDate());
        so.setNotes(request.getNotes());
        so.setSalesRepUserId(request.getSalesRepUserId());
        so.setForeignCurrency(request.getForeignCurrency());
        so.setExchangeRate(request.getExchangeRate());
        salesOrderRepository.save(so);

        lineRepository.deleteBySalesOrderId(id);
        List<SalesOrderLine> lines = saveLines(id, request.getCustomerId(), request.getLines());
        return toFullResponse(so, lines);
    }

    @Override
    @Transactional
    public SalesOrderResponse submitSalesOrder(Long id) {
        SalesOrder so = find(id);
        if (!so.getStatus().canTransitionTo(SalesOrderStatus.SUBMITTED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft sales orders can be submitted");
        }
        so.setStatus(SalesOrderStatus.SUBMITTED);
        salesOrderRepository.save(so);
        return toFullResponse(so, lineRepository.findBySalesOrderId(id));
    }

    // Approval is the gate where stock availability is actually checked and
    // (when InventorySettings.reserveStock is on) where a real reservation
    // is created — see StockAvailabilityService.check for the precedence
    // (allowOverselling/oversellingApprovalRequired/allowNegativeStock/
    // backorderEnabled). Confirming here is what "Order confirmation"
    // means: the order is now eligible for delivery (see
    // DeliveryServiceImpl.createDelivery's status guard).
    @Override
    @Transactional
    public SalesOrderResponse approveSalesOrder(Long id, String actingUsername) {
        SalesOrder so = find(id);
        if (!so.getStatus().canTransitionTo(SalesOrderStatus.CONFIRMED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only submitted sales orders can be approved");
        }
        List<SalesOrderLine> lines = lineRepository.findBySalesOrderId(id);

        ApprovalWorkflowService.ApprovalOutcome outcome = approvalWorkflowService.recordApproval(
                so.getCompanyId(), "SALES_ORDER", so.getId(), computeTotalAmount(lines), actingUsername);
        if (!outcome.finalized()) {
            return toFullResponse(so, lines);
        }

        InventorySettings settings = inventorySettingsService.resolveForCompany(so.getCompanyId());

        Map<Long, Product> lineProducts = productRepository.findAllById(
                lines.stream().map(SalesOrderLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        for (SalesOrderLine line : lines) {
            // Services and other non-stockable lines have no stock to check or
            // reserve — running them through the availability check would fail
            // every order on "Insufficient available stock (available 0)".
            Product product = lineProducts.get(line.getProductId());
            if (product != null && !product.isStockable()) {
                continue;
            }
            BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
            BigDecimal backordered = stockAvailabilityService.check(so.getCompanyId(), line.getProductId(), so.getWarehouseId(), remaining, actingUsername);
            if (backordered.signum() > 0) {
                line.setBackorderedQuantity(backordered);
                lineRepository.save(line);
            }
            if (settings.isReserveStock()) {
                reserve(line.getProductId(), so.getCompanyId(), so.getWarehouseId(), remaining);
            }
        }

        so.setStatus(SalesOrderStatus.CONFIRMED);
        salesOrderRepository.save(so);
        return toFullResponse(so, lines);
    }

    // Adjusts the unbinned "pool" row's reservedQuantity by `delta` (positive
    // to reserve, negative to release), floored at zero — same unbinned-row
    // convention PosStockService uses for restoring stock without exact bin
    // provenance.
    private void adjustReservation(Long productId, Long companyId, Long warehouseId, BigDecimal delta) {
        StockLevel unbinned = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder()
                        .companyId(companyId)
                        .productId(productId)
                        .warehouseId(warehouseId)
                        .build());
        BigDecimal updated = unbinned.getReservedQuantity().add(delta);
        unbinned.setReservedQuantity(updated.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : updated);
        stockLevelRepository.save(unbinned);
    }

    private void reserve(Long productId, Long companyId, Long warehouseId, BigDecimal quantity) {
        adjustReservation(productId, companyId, warehouseId, quantity);
    }

    private void release(Long productId, Long companyId, Long warehouseId, BigDecimal quantity) {
        adjustReservation(productId, companyId, warehouseId, quantity.negate());
    }

    @Override
    @Transactional
    public SalesOrderResponse cancelSalesOrder(Long id) {
        SalesOrder so = find(id);
        if (!so.getStatus().canTransitionTo(SalesOrderStatus.CANCELLED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft, submitted, or confirmed sales orders can be cancelled");
        }
        // Only a CONFIRMED order ever reserved anything — DRAFT/SUBMITTED
        // never passed through approveSalesOrder.
        if (so.getStatus() == SalesOrderStatus.CONFIRMED && inventorySettingsService.resolveForCompany(so.getCompanyId()).isReserveStock()) {
            for (SalesOrderLine line : lineRepository.findBySalesOrderId(id)) {
                BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
                if (remaining.signum() > 0) {
                    release(line.getProductId(), so.getCompanyId(), so.getWarehouseId(), remaining);
                }
            }
        }
        // Captured before the overwrite — this is the only record of how far the workflow got.
        so.setCancelledFromStatus(so.getStatus());
        so.setStatus(SalesOrderStatus.CANCELLED);
        salesOrderRepository.save(so);
        approvalWorkflowService.clearApprovals("SALES_ORDER", id);
        return toFullResponse(so, lineRepository.findBySalesOrderId(id));
    }

    // Cancels only the undelivered remainder of one line (e.g. one
    // out-of-stock item on an otherwise fine order) rather than the whole
    // order — reuses cancelSalesOrder's own release arithmetic. No new
    // line-status concept: reducing quantityOrdered down to quantityDelivered
    // is enough to mark "nothing more coming" without touching what already
    // shipped. Only meaningful on a CONFIRMED order — DRAFT/SUBMITTED lines
    // are edited via updateSalesOrder instead, and once DELIVERED/CANCELLED
    // there's nothing left to cancel.
    @Override
    @Transactional
    public SalesOrderResponse cancelSalesOrderLine(Long id, Long lineId) {
        SalesOrder so = find(id);
        if (so.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only confirmed sales orders support cancelling a line");
        }
        SalesOrderLine line = lineRepository.findById(lineId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sales order line not found with id: " + lineId));
        if (!line.getSalesOrderId().equals(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Line does not belong to this sales order");
        }
        BigDecimal remaining = line.getQuantityOrdered().subtract(line.getQuantityDelivered());
        if (remaining.signum() <= 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Nothing remaining to cancel on this line");
        }
        if (inventorySettingsService.resolveForCompany(so.getCompanyId()).isReserveStock()) {
            release(line.getProductId(), so.getCompanyId(), so.getWarehouseId(), remaining);
        }
        line.setQuantityOrdered(line.getQuantityDelivered());
        lineRepository.save(line);
        return toFullResponse(so, lineRepository.findBySalesOrderId(id));
    }

    @Override
    public void emailSalesOrder(Long id, SendDocumentEmailRequest request) {
        SalesOrderResponse so = getSalesOrder(id);
        Customer customer = so.getCustomerId() != null
                ? customerRepository.findById(so.getCustomerId()).orElse(null) : null;
        String to = request.getTo() != null && !request.getTo().isBlank() ? request.getTo()
                : customer != null ? customer.getEmail() : null;
        if (to == null || to.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer has no email on file — provide one to send to");
        }
        Company company = companyRepository.findById(so.getCompanyId()).orElse(null);
        String html = buildSalesOrderHtml(company, so);
        byte[] pdf = pdfRenderService.renderHtmlToPdf(html);

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject() : "Sales order " + so.getSoNumber();
        String body = request.getMessage() != null && !request.getMessage().isBlank()
                ? request.getMessage() : "Please find attached sales order " + so.getSoNumber() + ".";
        emailService.sendWithAttachment(to, subject, body, pdf, so.getSoNumber() + ".pdf", "application/pdf");
    }

    private String buildSalesOrderHtml(Company company, SalesOrderResponse so) {
        String metaHtml = "Order date: " + so.getOrderDate()
                + (so.getExpectedDate() != null ? "<br/>Expected: " + so.getExpectedDate() : "");

        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr><th>Product</th><th>Qty</th><th class=\"num\">Unit price</th>")
                .append("<th class=\"num\">Line total</th></tr></thead><tbody>");
        for (SalesOrderLineResponse line : so.getLines()) {
            table.append("<tr><td>").append(DocumentPdfHtml.escape(line.getProductName())).append("</td>")
                    .append("<td>").append(line.getQuantityOrdered()).append("</td>")
                    .append("<td class=\"num\">").append(line.getUnitPrice()).append("</td>")
                    .append("<td class=\"num\">").append(line.getLineTotal()).append("</td></tr>");
        }
        table.append("</tbody></table>");

        StringBuilder totals = new StringBuilder("<div class=\"totals\">");
        totals.append("<div class=\"grand\"><span>Total</span><span>").append(so.getTotalAmount()).append("</span></div>");
        totals.append("</div>");

        String partyHtml = DocumentPdfHtml.escape(so.getCustomerName());

        return DocumentPdfHtml.render(company, "SALES ORDER", so.getSoNumber(), metaHtml,
                "Prepared for", partyHtml, table.toString(), totals.toString());
    }

    private void requireQuotationNotExpired(Quotation quotation) {
        if (quotation.getValidUntil() != null && quotation.getValidUntil().isBefore(LocalDate.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quotation has expired");
        }
    }

    @Override
    @Transactional
    public void deleteSalesOrder(Long id) {
        SalesOrder so = find(id);
        if (so.getStatus() != SalesOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft sales orders can be deleted");
        }
        lineRepository.deleteBySalesOrderId(id);
        salesOrderRepository.deleteById(id);
    }

    // unitPrice falls back to the customer's price group's own price for the
    // product (Customer -> CustomerGroup -> PriceGroup -> ProductPrice), and
    // only then to the product's own list price; taxRate falls back straight
    // to the product's tax rate. Both only when the request omits them — the
    // "Pricing" behavior.
    private List<SalesOrderLine> saveLines(Long salesOrderId, Long customerId, List<SalesOrderLineRequest> requests) {
        Map<Long, Product> products = productRepository.findAllById(
                requests.stream().map(SalesOrderLineRequest::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        Long priceGroupId = customerRepository.findById(customerId)
                .map(Customer::getCustomerGroupId)
                .filter(Objects::nonNull)
                .flatMap(customerGroupRepository::findById)
                .map(CustomerGroup::getPriceGroupId)
                .orElse(null);

        List<SalesOrderLine> lines = requests.stream()
                .map(r -> {
                    Product product = products.get(r.getProductId());
                    UnitSelection unit = resolveLineUnit(product, r.getUnitOfMeasureId());
                    BigDecimal unitPrice = r.getUnitPrice() != null
                            ? r.getUnitPrice()
                            : resolveUnitPrice(r.getProductId(), priceGroupId, product, unit);
                    BigDecimal taxRate = r.getTaxRate() != null ? r.getTaxRate()
                            : (product == null ? BigDecimal.ZERO : product.getTaxRate());
                    return SalesOrderLine.builder()
                            .salesOrderId(salesOrderId)
                            .productId(r.getProductId())
                            .unitOfMeasureId(unit.unitOfMeasureId())
                            .conversionFactor(unit.conversionFactor())
                            .quantityOrdered(r.getQuantityOrdered())
                            .unitPrice(unitPrice)
                            .discountPercent(r.getDiscountPercent() == null ? BigDecimal.ZERO : r.getDiscountPercent())
                            .taxRate(taxRate)
                            .build();
                })
                .toList();
        return lineRepository.saveAll(lines);
    }

    private record UnitSelection(Long unitOfMeasureId, BigDecimal conversionFactor) {}

    // Mirrors PurchaseOrderServiceImpl.resolveLineUnit, but gated on allowSales
    // rather than allowPurchase. The factor is snapshotted onto the line because
    // deliveries convert through it to move stock — see DeliveryServiceImpl.
    private UnitSelection resolveLineUnit(Product product, Long requestedUnitOfMeasureId) {
        // Deliberately tolerant: callers that require a real product validate it
        // separately, and a product with no base unit configured predates UoM setup.
        // Falling back to "no conversion" keeps this from changing which lines are
        // allowed to save — it only adds the unit when there is one to add.
        Long unitId = requestedUnitOfMeasureId != null ? requestedUnitOfMeasureId
                : (product == null ? null : product.getUnitOfMeasureId());
        if (product == null || unitId == null || unitId.equals(product.getUnitOfMeasureId())) {
            return new UnitSelection(unitId, BigDecimal.ONE);
        }
        ProductUom productUom = productUomRepository
                .findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(product.getId(), unitId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "This unit is not configured for " + product.getName() + " — add it under the product's UOMs first"));
        if (!productUom.isAllowSales()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This unit is not allowed for selling " + product.getName());
        }
        return new UnitSelection(unitId, productUom.getConversionFactor());
    }

    // Lines saved before unitOfMeasureId/conversionFactor existed read back as
    // the product's own base unit at factor 1.
    private BigDecimal conversionFactorOf(SalesOrderLine line) {
        return line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
    }

    /**
     * Scales a base-unit price to the line's unit.
     *
     * <p>Everything {@link #resolveUnitPrice} can return — the product's
     * sellingPrice, a ProductPrice override, a price-group discount — is per
     * one <em>base</em> unit, while {@code quantityOrdered} and therefore
     * {@code quantityOrdered * unitPrice} are in the line's unit. Without this
     * a case of twelve bottles was ordered, shipped as twelve (the stock path
     * does apply the factor) and billed as one.
     *
     * <p>A price supplied on the request is left alone: whoever typed it was
     * looking at the line's unit, so it is already in the right terms.
     *
     * <p>Scale 4 matches the column and keeps sub-cent base prices — a price
     * per gram against a kilogram factor — from rounding away to zero.
     */
    private static BigDecimal priceInLineUnit(BigDecimal basePrice, BigDecimal conversionFactor) {
        BigDecimal factor = conversionFactor == null ? BigDecimal.ONE : conversionFactor;
        return basePrice.multiply(factor).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * The price for one of the line's units.
     *
     * <p>A ProductPrice recorded against that exact unit wins and is used
     * as-is — that is the whole point of per-unit pricing, where a case can
     * cost less than twelve bottles, so scaling it would undo the discount
     * someone deliberately entered.
     *
     * <p>Everything else in the cascade is a base-unit figure and gets
     * multiplied by the conversion factor.
     */
    private BigDecimal resolveUnitPrice(Long productId, Long priceGroupId, Product product, UnitSelection unit) {
        boolean nonBaseUnit = product != null
                && unit.unitOfMeasureId() != null
                && !unit.unitOfMeasureId().equals(product.getUnitOfMeasureId());
        if (priceGroupId != null && nonBaseUnit) {
            var perUnit = productPriceRepository
                    .findByProductIdAndPriceGroupIdAndUnitOfMeasureId(productId, priceGroupId, unit.unitOfMeasureId());
            if (perUnit.isPresent()) {
                return perUnit.get().getPrice();
            }
        }
        return priceInLineUnit(resolveBaseUnitPrice(productId, priceGroupId, product), unit.conversionFactor());
    }

    // Cascade: explicit per-product ProductPrice override wins; otherwise, if
    // the price group defines a default discountPercent, apply it off the
    // product's sellingPrice; otherwise fall back to the plain sellingPrice.
    // Always per one base unit — the caller scales.
    private BigDecimal resolveBaseUnitPrice(Long productId, Long priceGroupId, Product product) {
        BigDecimal sellingPrice = product == null ? BigDecimal.ZERO : product.getSellingPrice();
        if (priceGroupId == null) {
            return sellingPrice;
        }
        var override = productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(productId, priceGroupId);
        if (override.isPresent()) {
            return override.get().getPrice();
        }
        BigDecimal discountPercent = priceGroupRepository.findById(priceGroupId)
                .map(PriceGroup::getDiscountPercent)
                .orElse(null);
        if (discountPercent != null) {
            BigDecimal factor = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100)));
            return sellingPrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        return sellingPrice;
    }

    private void validateLineProducts(List<SalesOrderLineRequest> lines, Long companyId) {
        List<Long> productIds = lines.stream().map(SalesOrderLineRequest::getProductId).distinct().toList();
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

    // Both null (base currency only) or both set — never just one.
    private void requireForeignCurrencyPair(String foreignCurrency, BigDecimal exchangeRate) {
        boolean hasCurrency = foreignCurrency != null && !foreignCurrency.isBlank();
        boolean hasRate = exchangeRate != null;
        if (hasCurrency != hasRate) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Both foreign currency and exchange rate are required together");
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

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }

    // Mirrors LeadServiceImpl's company-scoping
    // guard for assignedToUserId — null companyId on the user means unscoped
    // (always allowed).
    private void requireSalesRep(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));
        if (user.getCompanyId() != null && !user.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sales rep does not belong to the selected company");
        }
    }

    private SalesOrder find(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Sales order not found with id: " + id));
    }

    private SalesOrderResponse toSummaryResponse(SalesOrder so, String companyName, String customerName,
                                                  String warehouseName, List<SalesOrderLine> lines) {
        return baseResponseBuilder(so, companyName, customerName, warehouseName, lines).lines(null).build();
    }

    private SalesOrderResponse toFullResponse(SalesOrder so, List<SalesOrderLine> lines) {
        String companyName = companyRepository.findById(so.getCompanyId()).map(Company::getName).orElse(null);
        String customerName = customerRepository.findById(so.getCustomerId()).map(Customer::getName).orElse(null);
        String warehouseName = warehouseRepository.findById(so.getWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(SalesOrderLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                lines.stream().map(SalesOrderLine::getUnitOfMeasureId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<SalesOrderLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    UnitOfMeasure unit = units.get(line.getUnitOfMeasureId());
                    BigDecimal conversionFactor = conversionFactorOf(line);
                    BigDecimal discountPercent = nonNull(line.getDiscountPercent());
                    BigDecimal taxRate = nonNull(line.getTaxRate());
                    BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
                    BigDecimal discountAmount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                    BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    return SalesOrderLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .unitOfMeasureId(line.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .conversionFactor(conversionFactor)
                            .quantityOrdered(line.getQuantityOrdered())
                            .baseQuantityOrdered(line.getQuantityOrdered().multiply(conversionFactor))
                            .baseQuantityDelivered(nonNull(line.getQuantityDelivered()).multiply(conversionFactor))
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(discountPercent)
                            .discountAmount(discountAmount)
                            .taxRate(taxRate)
                            .taxAmount(taxAmount)
                            .quantityDelivered(line.getQuantityDelivered())
                            .backorderedQuantity(nonNull(line.getBackorderedQuantity()))
                            .lineTotal(afterDiscount.add(taxAmount))
                            .build();
                })
                .toList();

        return baseResponseBuilder(so, companyName, customerName, warehouseName, lines)
                .lines(lineResponses)
                .build();
    }

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Legacy sales_order_lines rows predate discountPercent/taxRate and read
    // back as null — treat that as 0, same as a freshly-created line with no
    // discount/tax.
    private static BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    // Same computation as baseResponseBuilder's own totalAmount, kept as a
    // standalone helper so approveSalesOrder can evaluate an ApprovalRule
    // threshold before building a full response.
    private BigDecimal computeTotalAmount(List<SalesOrderLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (SalesOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
            BigDecimal lineDiscount = lineSubtotal.multiply(nonNull(line.getDiscountPercent())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(nonNull(line.getTaxRate())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        return subtotal.subtract(discountAmount).add(taxAmount);
    }

    private SalesOrderResponse.SalesOrderResponseBuilder baseResponseBuilder(
            SalesOrder so, String companyName, String customerName, String warehouseName, List<SalesOrderLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (SalesOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitPrice());
            BigDecimal lineDiscount = lineSubtotal.multiply(nonNull(line.getDiscountPercent())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(nonNull(line.getTaxRate())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
        BigDecimal foreignTotalAmount = so.getExchangeRate() != null
                ? totalAmount.divide(so.getExchangeRate(), 4, RoundingMode.HALF_UP)
                : null;
        Integer approvalsRequired = null;
        Integer approvalsRecorded = null;
        if (so.getStatus() == SalesOrderStatus.SUBMITTED) {
            approvalsRequired = approvalWorkflowService.requiredApprovals(so.getCompanyId(), "SALES_ORDER", totalAmount);
            approvalsRecorded = approvalWorkflowService.approvalsRecorded("SALES_ORDER", so.getId());
        }
        String salesRepName = so.getSalesRepUserId() == null ? null
                : userRepository.findById(so.getSalesRepUserId()).map(User::getUsername).orElse(null);
        return SalesOrderResponse.builder()
                .id(so.getId())
                .companyId(so.getCompanyId())
                .companyName(companyName)
                .customerId(so.getCustomerId())
                .customerName(customerName)
                .quotationId(so.getQuotationId())
                .warehouseId(so.getWarehouseId())
                .warehouseName(warehouseName)
                .soNumber(so.getSoNumber())
                .orderDate(so.getOrderDate())
                .expectedDate(so.getExpectedDate())
                .status(so.getStatus().name())
                .cancelledFromStatus(so.getCancelledFromStatus() == null ? null : so.getCancelledFromStatus().name())
                .notes(so.getNotes())
                .createdBy(so.getCreatedBy())
                .salesRepUserId(so.getSalesRepUserId())
                .salesRepName(salesRepName)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .foreignCurrency(so.getForeignCurrency())
                .exchangeRate(so.getExchangeRate())
                .foreignTotalAmount(foreignTotalAmount)
                .approvalsRequired(approvalsRequired)
                .approvalsRecorded(approvalsRecorded);
    }
}
