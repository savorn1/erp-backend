package com.example.erp.service.impl;

import com.example.erp.dto.CreatePurchaseOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseOrderFilterRequest;
import com.example.erp.dto.PurchaseOrderLineRequest;
import com.example.erp.dto.PurchaseOrderLineResponse;
import com.example.erp.dto.PurchaseOrderResponse;
import com.example.erp.dto.SendDocumentEmailRequest;
import com.example.erp.dto.UpdatePurchaseOrderRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.EmailService;
import com.example.erp.service.PdfRenderService;
import com.example.erp.service.PurchaseOrderService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository lineRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final PdfRenderService pdfRenderService;
    private final EmailService emailService;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final ProductUomRepository productUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ApprovalWorkflowService approvalWorkflowService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> listPurchaseOrders(PurchaseOrderFilterRequest filter) {
        List<Specification<PurchaseOrder>> conditions = new ArrayList<>();
        if (filter.getPoNumber() != null && !filter.getPoNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("poNumber")), "%" + filter.getPoNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<PurchaseOrder> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PurchaseOrder> page = purchaseOrderRepository.findAll(spec, pageable);
        List<PurchaseOrder> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(PurchaseOrder::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> supplierNames = supplierRepository.findAllById(
                content.stream().map(PurchaseOrder::getSupplierId).distinct().toList()
        ).stream().collect(Collectors.toMap(Supplier::getId, Supplier::getName));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(PurchaseOrder::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        // Lines aren't included in list rows (see toSummaryResponse) but are
        // still needed to compute each PO's totalAmount — fetched in one
        // batch per page rather than per row.
        List<Long> poIds = content.stream().map(PurchaseOrder::getId).toList();
        Map<Long, List<PurchaseOrderLine>> linesByPoId = poIds.isEmpty() ? Map.of() : allLinesGroupedByPo(poIds);

        return PageResponse.of(page.map(po -> toSummaryResponse(po,
                companyNames.get(po.getCompanyId()),
                supplierNames.get(po.getSupplierId()),
                warehouseNames.get(po.getWarehouseId()),
                linesByPoId.getOrDefault(po.getId(), List.of()))));
    }

    private Map<Long, List<PurchaseOrderLine>> allLinesGroupedByPo(List<Long> poIds) {
        // No findByPurchaseOrderIdIn on the repository (only findByPurchaseOrderId
        // exists) — fine at this scale (a single page of POs), avoids adding a
        // query method used from exactly one call site.
        Map<Long, List<PurchaseOrderLine>> result = new java.util.HashMap<>();
        for (Long poId : poIds) {
            result.put(poId, lineRepository.findByPurchaseOrderId(poId));
        }
        return result;
    }

    @Override
    public PurchaseOrderResponse getPurchaseOrder(Long id) {
        PurchaseOrder po = find(id);
        List<PurchaseOrderLine> lines = lineRepository.findByPurchaseOrderId(id);
        return toFullResponse(po, lines);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireSupplier(request.getSupplierId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());

        PurchaseOrder po = PurchaseOrder.builder()
                .companyId(request.getCompanyId())
                .supplierId(request.getSupplierId())
                .warehouseId(request.getWarehouseId())
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        purchaseOrderRepository.save(po);
        po.setPoNumber("PO-" + String.format("%06d", po.getId()));
        purchaseOrderRepository.save(po);

        List<PurchaseOrderLine> lines = saveLines(po.getId(), request.getLines());
        return toFullResponse(po, lines);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(Long id, UpdatePurchaseOrderRequest request) {
        PurchaseOrder po = find(id);
        if (!po.getStatus().canTransitionTo(PurchaseOrderStatus.SUBMITTED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase orders can be edited");
        }
        requireCompany(request.getCompanyId());
        requireSupplier(request.getSupplierId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());

        po.setCompanyId(request.getCompanyId());
        po.setSupplierId(request.getSupplierId());
        po.setWarehouseId(request.getWarehouseId());
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDate(request.getExpectedDate());
        po.setNotes(request.getNotes());
        purchaseOrderRepository.save(po);

        lineRepository.deleteByPurchaseOrderId(id);
        List<PurchaseOrderLine> lines = saveLines(id, request.getLines());
        return toFullResponse(po, lines);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse submitPurchaseOrder(Long id) {
        PurchaseOrder po = find(id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase orders can be submitted");
        }
        po.setStatus(PurchaseOrderStatus.SUBMITTED);
        purchaseOrderRepository.save(po);
        return toFullResponse(po, lineRepository.findByPurchaseOrderId(id));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long id, String actingUsername) {
        PurchaseOrder po = find(id);
        if (!po.getStatus().canTransitionTo(PurchaseOrderStatus.APPROVED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only submitted purchase orders can be approved");
        }
        List<PurchaseOrderLine> lines = lineRepository.findByPurchaseOrderId(id);
        ApprovalWorkflowService.ApprovalOutcome outcome = approvalWorkflowService.recordApproval(
                po.getCompanyId(), "PURCHASE_ORDER", po.getId(), computeTotalAmount(lines), actingUsername);
        if (!outcome.finalized()) {
            return toFullResponse(po, lines);
        }

        po.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrderRepository.save(po);
        return toFullResponse(po, lines);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse sendPurchaseOrder(Long id) {
        PurchaseOrder po = find(id);
        if (!po.getStatus().canTransitionTo(PurchaseOrderStatus.SENT)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only approved purchase orders can be sent");
        }
        po.setStatus(PurchaseOrderStatus.SENT);
        purchaseOrderRepository.save(po);
        return toFullResponse(po, lineRepository.findByPurchaseOrderId(id));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder po = find(id);
        if (!po.getStatus().canTransitionTo(PurchaseOrderStatus.CANCELLED)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft, submitted, approved, or sent purchase orders can be cancelled");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(po);
        approvalWorkflowService.clearApprovals("PURCHASE_ORDER", id);
        return toFullResponse(po, lineRepository.findByPurchaseOrderId(id));
    }

    @Override
    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = find(id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase orders can be deleted");
        }
        lineRepository.deleteByPurchaseOrderId(id);
        purchaseOrderRepository.deleteById(id);
    }

    @Override
    public void emailPurchaseOrder(Long id, SendDocumentEmailRequest request) {
        PurchaseOrderResponse po = getPurchaseOrder(id);
        Supplier supplier = supplierRepository.findById(po.getSupplierId()).orElse(null);
        String to = request.getTo() != null && !request.getTo().isBlank() ? request.getTo()
                : supplier != null ? supplier.getEmail() : null;
        if (to == null || to.isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Supplier has no email on file — provide one to send to");
        }
        Company company = companyRepository.findById(po.getCompanyId()).orElse(null);
        String html = buildPurchaseOrderHtml(company, po);
        byte[] pdf = pdfRenderService.renderHtmlToPdf(html);

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject() : "Purchase Order " + po.getPoNumber();
        String body = request.getMessage() != null && !request.getMessage().isBlank()
                ? request.getMessage() : "Please find attached purchase order " + po.getPoNumber() + ".";
        emailService.sendWithAttachment(to, subject, body, pdf, po.getPoNumber() + ".pdf", "application/pdf");
    }

    private String buildPurchaseOrderHtml(Company company, PurchaseOrderResponse po) {
        String metaHtml = "Date: " + po.getOrderDate()
                + (po.getExpectedDate() != null ? "<br/>Expected: " + po.getExpectedDate() : "");

        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr><th>Product</th><th>Qty</th><th class=\"num\">Unit cost</th>")
                .append("<th class=\"num\">Tax</th><th class=\"num\">Line total</th></tr></thead><tbody>");
        for (PurchaseOrderLineResponse line : po.getLines()) {
            table.append("<tr><td>").append(DocumentPdfHtml.escape(line.getProductName())).append("</td>")
                    .append("<td>").append(line.getQuantityOrdered()).append("</td>")
                    .append("<td class=\"num\">").append(line.getUnitCost()).append("</td>")
                    .append("<td class=\"num\">").append(line.getTaxAmount()).append("</td>")
                    .append("<td class=\"num\">").append(line.getLineTotal()).append("</td></tr>");
        }
        table.append("</tbody></table>");

        StringBuilder totals = new StringBuilder("<div class=\"totals\">");
        totals.append("<div><span>Subtotal</span><span>").append(po.getSubtotal()).append("</span></div>");
        if (po.getDiscountAmount() != null && po.getDiscountAmount().signum() > 0) {
            totals.append("<div><span>Discount</span><span>-").append(po.getDiscountAmount()).append("</span></div>");
        }
        if (po.getTaxAmount() != null && po.getTaxAmount().signum() > 0) {
            totals.append("<div><span>Tax</span><span>").append(po.getTaxAmount()).append("</span></div>");
        }
        totals.append("<div class=\"grand\"><span>Total</span><span>").append(po.getTotalAmount()).append("</span></div>");
        totals.append("</div>");

        String partyHtml = DocumentPdfHtml.escape(po.getSupplierName());

        return DocumentPdfHtml.render(company, "PURCHASE ORDER", po.getPoNumber(), metaHtml,
                "Supplier", partyHtml, table.toString(), totals.toString());
    }

    private List<PurchaseOrderLine> saveLines(Long purchaseOrderId, List<PurchaseOrderLineRequest> requests) {
        Map<Long, Product> products = productRepository.findAllById(
                requests.stream().map(PurchaseOrderLineRequest::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<PurchaseOrderLine> lines = requests.stream()
                .map(r -> {
                    UnitSelection unit = resolveLineUnit(products.get(r.getProductId()), r.getUnitOfMeasureId());
                    return PurchaseOrderLine.builder()
                            .purchaseOrderId(purchaseOrderId)
                            .productId(r.getProductId())
                            .unitOfMeasureId(unit.unitOfMeasureId())
                            .conversionFactor(unit.conversionFactor())
                            .quantityOrdered(r.getQuantityOrdered())
                            .unitCost(r.getUnitCost())
                            .discountPercent(r.getDiscountPercent() == null ? BigDecimal.ZERO : r.getDiscountPercent())
                            .taxRate(r.getTaxRate() == null ? BigDecimal.ZERO : r.getTaxRate())
                            .build();
                })
                .toList();
        return lineRepository.saveAll(lines);
    }

    // The product's own base unit always works, factor 1 — no ProductUom row
    // is required for it (avoids a chicken-and-egg problem, since that row is
    // only lazily created the first time someone opens the product's UOMs
    // tab). Any other unit must already be registered as a purchasable
    // ProductUom for this product (doc rule: "Purchase UOM must be
    // configured on the product before purchasing with that UOM").
    private record UnitSelection(Long unitOfMeasureId, BigDecimal conversionFactor) {}

    private UnitSelection resolveLineUnit(Product product, Long requestedUnitOfMeasureId) {
        if (product == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product not found");
        }
        Long unitId = requestedUnitOfMeasureId != null ? requestedUnitOfMeasureId : product.getUnitOfMeasureId();
        if (unitId.equals(product.getUnitOfMeasureId())) {
            return new UnitSelection(unitId, BigDecimal.ONE);
        }
        ProductUom productUom = productUomRepository.findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(product.getId(), unitId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "This unit is not configured for " + product.getName() + " — add it under the product's UOMs first"));
        if (!productUom.isAllowPurchase()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This unit is not allowed for purchasing " + product.getName());
        }
        return new UnitSelection(unitId, productUom.getConversionFactor());
    }

    private void validateLineProducts(List<PurchaseOrderLineRequest> lines, Long companyId) {
        List<Long> productIds = lines.stream().map(PurchaseOrderLineRequest::getProductId).distinct().toList();
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

    private void requireSupplier(Long supplierId, Long companyId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier not found with id: " + supplierId));
        if (!supplier.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Supplier does not belong to the selected company");
        }
    }

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }

    private PurchaseOrder find(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase order not found with id: " + id));
    }

    private PurchaseOrderResponse toSummaryResponse(PurchaseOrder po, String companyName, String supplierName,
                                                      String warehouseName, List<PurchaseOrderLine> lines) {
        return baseResponseBuilder(po, companyName, supplierName, warehouseName, lines).lines(null).build();
    }

    private PurchaseOrderResponse toFullResponse(PurchaseOrder po, List<PurchaseOrderLine> lines) {
        String companyName = companyRepository.findById(po.getCompanyId()).map(Company::getName).orElse(null);
        String supplierName = supplierRepository.findById(po.getSupplierId()).map(Supplier::getName).orElse(null);
        String warehouseName = warehouseRepository.findById(po.getWarehouseId()).map(Warehouse::getName).orElse(null);

        Map<Long, Product> products = productRepository.findAllById(
                lines.stream().map(PurchaseOrderLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                lines.stream().map(this::unitOfMeasureIdOf).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<PurchaseOrderLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    UnitOfMeasure unit = units.get(unitOfMeasureIdOf(line));
                    BigDecimal conversionFactor = conversionFactorOf(line);
                    BigDecimal discountPercent = nonNull(line.getDiscountPercent());
                    BigDecimal taxRate = nonNull(line.getTaxRate());
                    BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
                    BigDecimal discountAmount = lineSubtotal.multiply(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
                    BigDecimal taxAmount = afterDiscount.multiply(taxRate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
                    return PurchaseOrderLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .unitOfMeasureId(unitOfMeasureIdOf(line))
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .conversionFactor(conversionFactor)
                            .quantityOrdered(line.getQuantityOrdered())
                            .unitCost(line.getUnitCost())
                            .discountPercent(discountPercent)
                            .discountAmount(discountAmount)
                            .taxRate(taxRate)
                            .taxAmount(taxAmount)
                            .quantityReceived(line.getQuantityReceived())
                            .baseQuantityOrdered(line.getQuantityOrdered().multiply(conversionFactor))
                            .baseQuantityReceived(line.getQuantityReceived().multiply(conversionFactor))
                            .lineTotal(afterDiscount.add(taxAmount))
                            .build();
                })
                .toList();

        return baseResponseBuilder(po, companyName, supplierName, warehouseName, lines)
                .lines(lineResponses)
                .build();
    }

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Lines saved before discountPercent/taxRate existed read back as null —
    // treat that as 0, same as a freshly-created line with no discount/tax.
    private static BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    // Lines saved before unitOfMeasureId/conversionFactor existed read back
    // as null — treat that as "the product's own base unit, factor 1" so old
    // orders keep meaning exactly what they always did.
    private Long unitOfMeasureIdOf(PurchaseOrderLine line) {
        if (line.getUnitOfMeasureId() != null) return line.getUnitOfMeasureId();
        Product product = productRepository.findById(line.getProductId()).orElse(null);
        return product == null ? null : product.getUnitOfMeasureId();
    }

    private BigDecimal conversionFactorOf(PurchaseOrderLine line) {
        return line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
    }

    // Same computation as baseResponseBuilder's own totalAmount, kept as a
    // standalone helper so approvePurchaseOrder can evaluate an
    // ApprovalRule threshold before building a full response.
    private BigDecimal computeTotalAmount(List<PurchaseOrderLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (PurchaseOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
            BigDecimal lineDiscount = lineSubtotal.multiply(nonNull(line.getDiscountPercent())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(nonNull(line.getTaxRate())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        return subtotal.subtract(discountAmount).add(taxAmount);
    }

    private PurchaseOrderResponse.PurchaseOrderResponseBuilder baseResponseBuilder(
            PurchaseOrder po, String companyName, String supplierName, String warehouseName, List<PurchaseOrderLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        for (PurchaseOrderLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantityOrdered().multiply(line.getUnitCost());
            BigDecimal lineDiscount = lineSubtotal.multiply(nonNull(line.getDiscountPercent())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(lineDiscount);
            BigDecimal lineTax = afterDiscount.multiply(nonNull(line.getTaxRate())).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscount);
            taxAmount = taxAmount.add(lineTax);
        }
        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount);
        Integer approvalsRequired = null;
        Integer approvalsRecorded = null;
        if (po.getStatus() == PurchaseOrderStatus.SUBMITTED) {
            approvalsRequired = approvalWorkflowService.requiredApprovals(po.getCompanyId(), "PURCHASE_ORDER", totalAmount);
            approvalsRecorded = approvalWorkflowService.approvalsRecorded("PURCHASE_ORDER", po.getId());
        }
        return PurchaseOrderResponse.builder()
                .id(po.getId())
                .companyId(po.getCompanyId())
                .companyName(companyName)
                .supplierId(po.getSupplierId())
                .supplierName(supplierName)
                .warehouseId(po.getWarehouseId())
                .warehouseName(warehouseName)
                .poNumber(po.getPoNumber())
                .orderDate(po.getOrderDate())
                .expectedDate(po.getExpectedDate())
                .status(po.getStatus().name())
                .notes(po.getNotes())
                .createdBy(po.getCreatedBy())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .approvalsRequired(approvalsRequired)
                .approvalsRecorded(approvalsRecorded);
    }
}
