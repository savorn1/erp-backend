package com.example.erp.service.impl;

import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SalesOrderFilterRequest;
import com.example.erp.dto.SalesOrderLineRequest;
import com.example.erp.dto.SalesOrderLineResponse;
import com.example.erp.dto.SalesOrderResponse;
import com.example.erp.dto.UpdateSalesOrderRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.Product;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductPriceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.InventorySettingsService;
import com.example.erp.service.SalesOrderService;
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
    private final StockLevelRepository stockLevelRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final ProductPriceRepository productPriceRepository;
    private final PriceGroupRepository priceGroupRepository;
    private final StockAvailabilityService stockAvailabilityService;
    private final InventorySettingsService inventorySettingsService;
    private final ApprovalWorkflowService approvalWorkflowService;

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

        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());
        SalesOrder so = SalesOrder.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .warehouseId(request.getWarehouseId())
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
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
        requireForeignCurrencyPair(request.getForeignCurrency(), request.getExchangeRate());

        so.setCompanyId(request.getCompanyId());
        so.setCustomerId(request.getCustomerId());
        so.setWarehouseId(request.getWarehouseId());
        so.setOrderDate(request.getOrderDate());
        so.setExpectedDate(request.getExpectedDate());
        so.setNotes(request.getNotes());
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
        if (so.getStatus() != SalesOrderStatus.DRAFT) {
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
        if (so.getStatus() != SalesOrderStatus.SUBMITTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only submitted sales orders can be approved");
        }
        List<SalesOrderLine> lines = lineRepository.findBySalesOrderId(id);

        ApprovalWorkflowService.ApprovalOutcome outcome = approvalWorkflowService.recordApproval(
                so.getCompanyId(), "SALES_ORDER", so.getId(), computeTotalAmount(lines), actingUsername);
        if (!outcome.finalized()) {
            return toFullResponse(so, lines);
        }

        InventorySettings settings = inventorySettingsService.resolveForCompany(so.getCompanyId());

        for (SalesOrderLine line : lines) {
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
        if (so.getStatus() != SalesOrderStatus.DRAFT && so.getStatus() != SalesOrderStatus.SUBMITTED
                && so.getStatus() != SalesOrderStatus.CONFIRMED) {
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
        so.setStatus(SalesOrderStatus.CANCELLED);
        salesOrderRepository.save(so);
        approvalWorkflowService.clearApprovals("SALES_ORDER", id);
        return toFullResponse(so, lineRepository.findBySalesOrderId(id));
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
                    BigDecimal unitPrice = r.getUnitPrice() != null ? r.getUnitPrice() : resolveUnitPrice(r.getProductId(), priceGroupId, product);
                    BigDecimal taxRate = r.getTaxRate() != null ? r.getTaxRate()
                            : (product == null ? BigDecimal.ZERO : product.getTaxRate());
                    return SalesOrderLine.builder()
                            .salesOrderId(salesOrderId)
                            .productId(r.getProductId())
                            .quantityOrdered(r.getQuantityOrdered())
                            .unitPrice(unitPrice)
                            .discountPercent(r.getDiscountPercent() == null ? BigDecimal.ZERO : r.getDiscountPercent())
                            .taxRate(taxRate)
                            .build();
                })
                .toList();
        return lineRepository.saveAll(lines);
    }

    // Cascade: explicit per-product ProductPrice override wins; otherwise, if
    // the price group defines a default discountPercent, apply it off the
    // product's sellingPrice; otherwise fall back to the plain sellingPrice.
    private BigDecimal resolveUnitPrice(Long productId, Long priceGroupId, Product product) {
        BigDecimal sellingPrice = product == null ? BigDecimal.ZERO : product.getSellingPrice();
        if (priceGroupId == null) {
            return sellingPrice;
        }
        var override = productPriceRepository.findByProductIdAndPriceGroupId(productId, priceGroupId);
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

        List<SalesOrderLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
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
                            .quantityOrdered(line.getQuantityOrdered())
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
        return SalesOrderResponse.builder()
                .id(so.getId())
                .companyId(so.getCompanyId())
                .companyName(companyName)
                .customerId(so.getCustomerId())
                .customerName(customerName)
                .warehouseId(so.getWarehouseId())
                .warehouseName(warehouseName)
                .soNumber(so.getSoNumber())
                .orderDate(so.getOrderDate())
                .expectedDate(so.getExpectedDate())
                .status(so.getStatus().name())
                .notes(so.getNotes())
                .createdBy(so.getCreatedBy())
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
