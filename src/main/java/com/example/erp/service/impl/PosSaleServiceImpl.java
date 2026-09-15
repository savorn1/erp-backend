package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosCheckoutLineRequest;
import com.example.erp.dto.PosCheckoutRequest;
import com.example.erp.dto.PosPaymentLineResponse;
import com.example.erp.dto.PosSaleFilterRequest;
import com.example.erp.dto.PosSaleLineResponse;
import com.example.erp.dto.PosSaleResponse;
import com.example.erp.dto.PosTenderRequest;
import com.example.erp.dto.VoidPosSaleRequest;
import com.example.erp.entity.Customer;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PosPaymentLine;
import com.example.erp.entity.PosSale;
import com.example.erp.entity.PosSaleLine;
import com.example.erp.entity.PosSaleStatus;
import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.Register;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.PosPaymentLineRepository;
import com.example.erp.repository.PosSaleLineRepository;
import com.example.erp.repository.PosSaleRepository;
import com.example.erp.repository.PosSessionRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.RegisterRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PosSaleService;
import com.example.erp.service.impl.PosPricingService.PricedLine;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosSaleServiceImpl implements PosSaleService {

    private static final String WALK_IN_CUSTOMER_NAME = "Walk-in Customer";

    private final PosSaleRepository posSaleRepository;
    private final PosSaleLineRepository posSaleLineRepository;
    private final PosPaymentLineRepository posPaymentLineRepository;
    private final PosSessionRepository posSessionRepository;
    private final RegisterRepository registerRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final AutoPostingService autoPostingService;
    private final PosPricingService posPricingService;
    private final PosStockService posStockService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PosSaleResponse> list(PosSaleFilterRequest filter) {
        List<Specification<PosSale>> conditions = new ArrayList<>();
        if (filter.getSaleNumber() != null && !filter.getSaleNumber().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("saleNumber")), "%" + filter.getSaleNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getRegisterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("registerId"), filter.getRegisterId()));
        if (filter.getPosSessionId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("posSessionId"), filter.getPosSessionId()));
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), PosSaleStatus.valueOf(filter.getStatus())));
        }
        if (filter.getDateFrom() != null) {
            LocalDateTime from = filter.getDateFrom().atStartOfDay();
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("saleDate"), from));
        }
        if (filter.getDateTo() != null) {
            LocalDateTime to = filter.getDateTo().atTime(23, 59, 59);
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("saleDate"), to));
        }
        Specification<PosSale> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PosSale> page = posSaleRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(sale -> toResponse(sale, null)));
    }

    @Override
    @Transactional(readOnly = true)
    public PosSaleResponse get(Long id) {
        return toResponse(find(id), null);
    }

    @Override
    @Transactional
    public PosSaleResponse checkout(PosCheckoutRequest request, String actingUsername) {
        PosSession session = posSessionRepository.findById(request.getPosSessionId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "POS session not found with id: " + request.getPosSessionId()));
        if (session.getStatus() != PosSessionStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This session is closed — open a new one to keep selling");
        }
        Register register = registerRepository.findById(session.getRegisterId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Register not found with id: " + session.getRegisterId()));

        Long customerId = resolveCustomer(register.getCompanyId(), request.getCustomerId());

        // ── Price/tax the lines ─────────────────────────────────────────────
        List<PricedLine> pricedLines = new ArrayList<>();
        for (PosCheckoutLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(register.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to this register's company: " + product.getName());
            }
            pricedLines.add(posPricingService.price(product, lineRequest.getQuantity(), lineRequest.getDiscountPercent()));
        }

        BigDecimal subtotal = pricedLines.stream().map(l -> l.quantity().multiply(l.unitPrice())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = pricedLines.stream()
                .map(l -> l.quantity().multiply(l.unitPrice()).subtract(l.net()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = pricedLines.stream().map(PricedLine::tax).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = pricedLines.stream().map(PricedLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cogsAmount = pricedLines.stream().map(l -> l.quantity().multiply(l.costPrice())).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Validate and apply tenders ──────────────────────────────────────
        BigDecimal nonCashTotal = request.getTenders().stream()
                .filter(t -> t.getMethod() != PaymentMethod.CASH)
                .map(PosTenderRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (nonCashTotal.compareTo(totalAmount) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Non-cash tenders exceed the sale total");
        }
        BigDecimal remainingForCash = totalAmount.subtract(nonCashTotal);
        BigDecimal cashTenderedRaw = request.getTenders().stream()
                .filter(t -> t.getMethod() == PaymentMethod.CASH)
                .map(PosTenderRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cashTenderedRaw.compareTo(remainingForCash) < 0) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient payment — " + remainingForCash + " still due");
        }
        BigDecimal appliedCash = remainingForCash;
        BigDecimal changeDue = cashTenderedRaw.subtract(remainingForCash);

        // ── Save the sale header ────────────────────────────────────────────
        PosSale sale = PosSale.builder()
                .companyId(register.getCompanyId())
                .warehouseId(register.getWarehouseId())
                .registerId(register.getId())
                .posSessionId(session.getId())
                .customerId(customerId)
                .saleDate(LocalDateTime.now())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .createdBy(actingUsername)
                .build();
        posSaleRepository.save(sale);
        sale.setSaleNumber("POS-" + String.format("%06d", sale.getId()));
        posSaleRepository.save(sale);

        // ── Lines + stock decrement (pessimistic-locked) ────────────────────
        for (PricedLine line : pricedLines) {
            posSaleLineRepository.save(PosSaleLine.builder()
                    .posSaleId(sale.getId())
                    .productId(line.productId())
                    .quantity(line.quantity())
                    .unitPrice(line.unitPrice())
                    .discountPercent(line.discountPercent())
                    .taxRate(line.taxRate())
                    .lineTotal(line.lineTotal())
                    .build());
            posStockService.decrease(register.getCompanyId(), register.getWarehouseId(), line.productId(), line.quantity(),
                    "POS_SALE", sale.getId(), actingUsername);
        }

        // ── Tender lines (non-cash as requested, one applied-cash line) ─────
        List<PosPaymentLine> paymentLines = new ArrayList<>();
        for (PosTenderRequest tender : request.getTenders()) {
            if (tender.getMethod() == PaymentMethod.CASH) continue;
            paymentLines.add(posPaymentLineRepository.save(PosPaymentLine.builder()
                    .posSaleId(sale.getId())
                    .method(tender.getMethod())
                    .amount(tender.getAmount())
                    .reference(tender.getReference())
                    .build()));
        }
        if (appliedCash.signum() > 0) {
            paymentLines.add(posPaymentLineRepository.save(PosPaymentLine.builder()
                    .posSaleId(sale.getId())
                    .method(PaymentMethod.CASH)
                    .amount(appliedCash)
                    .build()));
        }

        // ── Auto-post ────────────────────────────────────────────────────────
        autoPostingService.postPosSale(register.getCompanyId(), sale.getSaleDate().toLocalDate(), appliedCash, nonCashTotal,
                subtotal.subtract(discountAmount), taxAmount, cogsAmount, sale.getId(), actingUsername);

        PosSaleResponse response = toResponse(sale, posSaleLineRepository.findByPosSaleId(sale.getId()), paymentLines);
        response.setChangeDue(changeDue);
        return response;
    }

    @Override
    @Transactional
    public PosSaleResponse voidSale(Long id, VoidPosSaleRequest request, String actingUsername) {
        PosSale sale = find(id);
        if (sale.getStatus() != PosSaleStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a completed sale can be voided");
        }
        List<PosSaleLine> lines = posSaleLineRepository.findByPosSaleId(id);
        for (PosSaleLine line : lines) {
            posStockService.increase(sale.getCompanyId(), sale.getWarehouseId(), line.getProductId(), line.getQuantity(),
                    "POS_SALE_VOID", sale.getId(), actingUsername);
        }
        autoPostingService.reverseAutoEntry("POS_SALE", sale.getId(), actingUsername);

        sale.setStatus(PosSaleStatus.VOIDED);
        sale.setVoidedBy(actingUsername);
        sale.setVoidedAt(LocalDateTime.now());
        sale.setVoidReason(request.getReason());
        posSaleRepository.save(sale);
        return toResponse(sale, lines);
    }

    private Long resolveCustomer(Long companyId, Long requestedCustomerId) {
        if (requestedCustomerId != null) {
            Customer customer = customerRepository.findById(requestedCustomerId)
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + requestedCustomerId));
            if (!customer.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to this register's company");
            }
            return customer.getId();
        }
        return customerRepository.findByCompanyIdAndName(companyId, WALK_IN_CUSTOMER_NAME)
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .companyId(companyId)
                        .name(WALK_IN_CUSTOMER_NAME)
                        .build()))
                .getId();
    }

    private PosSaleResponse toResponse(PosSale sale, List<PosSaleLine> lines) {
        return toResponse(sale, lines, null);
    }

    private PosSaleResponse toResponse(PosSale sale, List<PosSaleLine> preloadedLines, List<PosPaymentLine> preloadedPayments) {
        List<PosSaleLine> lines = preloadedLines != null ? preloadedLines : posSaleLineRepository.findByPosSaleId(sale.getId());
        List<PosPaymentLine> payments = preloadedPayments != null ? preloadedPayments : posPaymentLineRepository.findByPosSaleId(sale.getId());

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(PosSaleLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        Warehouse warehouse = warehouseRepository.findById(sale.getWarehouseId()).orElse(null);
        Register register = registerRepository.findById(sale.getRegisterId()).orElse(null);
        Customer customer = customerRepository.findById(sale.getCustomerId()).orElse(null);

        List<PosSaleLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return PosSaleLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .taxRate(line.getTaxRate())
                            .lineTotal(line.getLineTotal())
                            .returnedQuantity(line.getReturnedQuantity())
                            .build();
                })
                .toList();

        List<PosPaymentLineResponse> paymentResponses = payments.stream()
                .map(p -> PosPaymentLineResponse.builder()
                        .id(p.getId())
                        .method(p.getMethod().name())
                        .amount(p.getAmount())
                        .reference(p.getReference())
                        .build())
                .toList();

        return PosSaleResponse.builder()
                .id(sale.getId())
                .companyId(sale.getCompanyId())
                .warehouseId(sale.getWarehouseId())
                .warehouseName(warehouse == null ? null : warehouse.getName())
                .registerId(sale.getRegisterId())
                .registerName(register == null ? null : register.getName())
                .posSessionId(sale.getPosSessionId())
                .customerId(sale.getCustomerId())
                .customerName(customer == null ? null : customer.getName())
                .saleNumber(sale.getSaleNumber())
                .saleDate(sale.getSaleDate())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .status(sale.getStatus().name())
                .voidedBy(sale.getVoidedBy())
                .voidedAt(sale.getVoidedAt())
                .voidReason(sale.getVoidReason())
                .createdBy(sale.getCreatedBy())
                .lines(lineResponses)
                .payments(paymentResponses)
                .build();
    }

    private PosSale find(Long id) {
        return posSaleRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POS sale not found with id: " + id));
    }
}
