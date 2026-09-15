package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosCheckoutLineRequest;
import com.example.erp.dto.PosExchangeFilterRequest;
import com.example.erp.dto.PosExchangeNewLineResponse;
import com.example.erp.dto.PosExchangeRequest;
import com.example.erp.dto.PosExchangeResponse;
import com.example.erp.dto.PosExchangeReturnLineRequest;
import com.example.erp.dto.PosExchangeReturnLineResponse;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PosExchange;
import com.example.erp.entity.PosExchangeNewLine;
import com.example.erp.entity.PosExchangeReturnLine;
import com.example.erp.entity.PosSale;
import com.example.erp.entity.PosSaleLine;
import com.example.erp.entity.PosSaleStatus;
import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.Register;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PosExchangeNewLineRepository;
import com.example.erp.repository.PosExchangeRepository;
import com.example.erp.repository.PosExchangeReturnLineRepository;
import com.example.erp.repository.PosSaleLineRepository;
import com.example.erp.repository.PosSaleRepository;
import com.example.erp.repository.PosSessionRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.RegisterRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PosExchangeService;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosExchangeServiceImpl implements PosExchangeService {

    private final PosExchangeRepository posExchangeRepository;
    private final PosExchangeReturnLineRepository posExchangeReturnLineRepository;
    private final PosExchangeNewLineRepository posExchangeNewLineRepository;
    private final PosSaleRepository posSaleRepository;
    private final PosSaleLineRepository posSaleLineRepository;
    private final PosSessionRepository posSessionRepository;
    private final RegisterRepository registerRepository;
    private final ProductRepository productRepository;
    private final AutoPostingService autoPostingService;
    private final PosPricingService posPricingService;
    private final PosStockService posStockService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PosExchangeResponse> list(PosExchangeFilterRequest filter) {
        List<Specification<PosExchange>> conditions = new ArrayList<>();
        if (filter.getExchangeNumber() != null && !filter.getExchangeNumber().isBlank()) {
            conditions.add((root, query, cb) -> cb.like(cb.lower(root.get("exchangeNumber")), "%" + filter.getExchangeNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getRegisterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("registerId"), filter.getRegisterId()));
        if (filter.getOriginalPosSaleId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("originalPosSaleId"), filter.getOriginalPosSaleId()));
        }
        Specification<PosExchange> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PosExchange> page = posExchangeRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PosExchangeResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PosExchangeResponse createExchange(PosExchangeRequest request, String actingUsername) {
        PosSession session = posSessionRepository.findById(request.getPosSessionId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "POS session not found with id: " + request.getPosSessionId()));
        if (session.getStatus() != PosSessionStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This session is closed — open a new one to process exchanges");
        }
        Register register = registerRepository.findById(session.getRegisterId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Register not found with id: " + session.getRegisterId()));

        PosSale originalSale = posSaleRepository.findById(request.getOriginalPosSaleId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Original sale not found with id: " + request.getOriginalPosSaleId()));
        if (originalSale.getStatus() != PosSaleStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a completed sale can be exchanged against");
        }
        if (!originalSale.getCompanyId().equals(register.getCompanyId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Original sale does not belong to this register's company");
        }

        // ── Save the header first, so line-level stock movements have a
        //    sourceId to reference ────────────────────────────────────────
        PosExchange exchange = PosExchange.builder()
                .companyId(register.getCompanyId())
                .warehouseId(register.getWarehouseId())
                .registerId(register.getId())
                .posSessionId(session.getId())
                .originalPosSaleId(originalSale.getId())
                .exchangeDate(LocalDateTime.now())
                .createdBy(actingUsername)
                .build();
        posExchangeRepository.save(exchange);
        exchange.setExchangeNumber("EXC-" + String.format("%06d", exchange.getId()));
        posExchangeRepository.save(exchange);

        // ── Return lines: price at the original line's own snapshot, bump
        //    returnedQuantity, restore stock ─────────────────────────────
        BigDecimal returnValue = BigDecimal.ZERO;
        BigDecimal returnTaxValue = BigDecimal.ZERO;
        BigDecimal returnCogs = BigDecimal.ZERO;
        List<PosExchangeReturnLine> returnLines = new ArrayList<>();
        for (PosExchangeReturnLineRequest lineRequest : request.getReturnLines()) {
            PosSaleLine originalLine = posSaleLineRepository.findById(lineRequest.getPosSaleLineId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Sale line not found with id: " + lineRequest.getPosSaleLineId()));
            if (!originalLine.getPosSaleId().equals(originalSale.getId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Sale line " + originalLine.getId() + " does not belong to the selected sale");
            }
            BigDecimal remaining = originalLine.getQuantity().subtract(originalLine.getReturnedQuantity());
            if (lineRequest.getQuantity().compareTo(remaining) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Only " + remaining + " remains returnable on line " + originalLine.getId());
            }
            if (lineRequest.getQuantity().signum() <= 0) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Return quantity must be greater than zero");
            }

            BigDecimal quantity = lineRequest.getQuantity();
            BigDecimal gross = quantity.multiply(originalLine.getUnitPrice());
            BigDecimal discount = gross.multiply(originalLine.getDiscountPercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(discount);
            BigDecimal tax = net.multiply(originalLine.getTaxRate()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal lineValue = net.add(tax);

            originalLine.setReturnedQuantity(originalLine.getReturnedQuantity().add(quantity));
            posSaleLineRepository.save(originalLine);

            returnLines.add(posExchangeReturnLineRepository.save(PosExchangeReturnLine.builder()
                    .posExchangeId(exchange.getId())
                    .originalPosSaleLineId(originalLine.getId())
                    .productId(originalLine.getProductId())
                    .quantity(quantity)
                    .unitPrice(originalLine.getUnitPrice())
                    .discountPercent(originalLine.getDiscountPercent())
                    .taxRate(originalLine.getTaxRate())
                    .net(net)
                    .tax(tax)
                    .lineValue(lineValue)
                    .build()));

            Product product = productRepository.findById(originalLine.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + originalLine.getProductId()));
            returnCogs = returnCogs.add(quantity.multiply(product.getCostPrice()));

            posStockService.increase(originalSale.getCompanyId(), originalSale.getWarehouseId(), originalLine.getProductId(), quantity,
                    "POS_EXCHANGE_RETURN", exchange.getId(), actingUsername);

            returnValue = returnValue.add(net);
            returnTaxValue = returnTaxValue.add(tax);
        }

        // ── New lines: priced at today's product price, stock decremented ──
        BigDecimal newValue = BigDecimal.ZERO;
        BigDecimal newTaxValue = BigDecimal.ZERO;
        BigDecimal newCogs = BigDecimal.ZERO;
        List<PosExchangeNewLine> newLines = new ArrayList<>();
        for (PosCheckoutLineRequest lineRequest : request.getNewLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(register.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to this register's company: " + product.getName());
            }
            PricedLine priced = posPricingService.price(product, lineRequest.getQuantity(), lineRequest.getDiscountPercent());

            newLines.add(posExchangeNewLineRepository.save(PosExchangeNewLine.builder()
                    .posExchangeId(exchange.getId())
                    .productId(priced.productId())
                    .quantity(priced.quantity())
                    .unitPrice(priced.unitPrice())
                    .discountPercent(priced.discountPercent())
                    .taxRate(priced.taxRate())
                    .net(priced.net())
                    .tax(priced.tax())
                    .lineTotal(priced.lineTotal())
                    .costPrice(priced.costPrice())
                    .build()));

            posStockService.decrease(register.getCompanyId(), register.getWarehouseId(), priced.productId(), priced.quantity(),
                    "POS_EXCHANGE_NEW", exchange.getId(), actingUsername);

            newValue = newValue.add(priced.net());
            newTaxValue = newTaxValue.add(priced.tax());
            newCogs = newCogs.add(priced.quantity().multiply(priced.costPrice()));
        }

        // ── Net settlement ───────────────────────────────────────────────
        BigDecimal netAmount = newValue.add(newTaxValue).subtract(returnValue.add(returnTaxValue));
        BigDecimal changeDue = BigDecimal.ZERO;
        PaymentMethod settlementMethod = null;
        if (netAmount.signum() != 0) {
            settlementMethod = request.getSettlementMethod();
            if (settlementMethod == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "A settlement method is required — there is a balance of " + netAmount);
            }
            if (settlementMethod == PaymentMethod.CASH && netAmount.signum() > 0) {
                BigDecimal cashTendered = request.getCashTendered() == null ? netAmount : request.getCashTendered();
                if (cashTendered.compareTo(netAmount) < 0) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Insufficient payment — " + netAmount + " still due");
                }
                changeDue = cashTendered.subtract(netAmount);
            }
        }

        exchange.setReturnValue(returnValue);
        exchange.setReturnTaxValue(returnTaxValue);
        exchange.setNewValue(newValue);
        exchange.setNewTaxValue(newTaxValue);
        exchange.setNetAmount(netAmount);
        exchange.setSettlementMethod(settlementMethod);
        exchange.setSettlementAmount(netAmount);
        exchange.setSettlementReference(request.getSettlementReference());
        posExchangeRepository.save(exchange);

        // ── Auto-post: one netted, sign-aware entry ─────────────────────
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal bankAmount = BigDecimal.ZERO;
        if (netAmount.signum() != 0) {
            if (settlementMethod == PaymentMethod.CASH) {
                cashAmount = netAmount;
            } else {
                bankAmount = netAmount;
            }
        }
        autoPostingService.postPosExchange(register.getCompanyId(), exchange.getExchangeDate().toLocalDate(),
                newValue.subtract(returnValue), newTaxValue.subtract(returnTaxValue), newCogs.subtract(returnCogs),
                cashAmount, bankAmount, exchange.getId(), actingUsername);

        PosExchangeResponse response = toResponse(exchange, returnLines, newLines);
        response.setChangeDue(changeDue);
        return response;
    }

    private PosExchangeResponse toResponse(PosExchange exchange) {
        return toResponse(exchange, posExchangeReturnLineRepository.findByPosExchangeId(exchange.getId()),
                posExchangeNewLineRepository.findByPosExchangeId(exchange.getId()));
    }

    private PosExchangeResponse toResponse(PosExchange exchange, List<PosExchangeReturnLine> returnLines, List<PosExchangeNewLine> newLines) {
        List<Long> productIds = new ArrayList<>();
        productIds.addAll(returnLines.stream().map(PosExchangeReturnLine::getProductId).toList());
        productIds.addAll(newLines.stream().map(PosExchangeNewLine::getProductId).toList());
        Map<Long, Product> products = productIds.isEmpty() ? Map.of() : productRepository.findAllById(productIds.stream().distinct().toList())
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        Register register = registerRepository.findById(exchange.getRegisterId()).orElse(null);
        PosSale originalSale = posSaleRepository.findById(exchange.getOriginalPosSaleId()).orElse(null);

        List<PosExchangeReturnLineResponse> returnLineResponses = returnLines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return PosExchangeReturnLineResponse.builder()
                            .id(line.getId())
                            .originalPosSaleLineId(line.getOriginalPosSaleLineId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .taxRate(line.getTaxRate())
                            .lineValue(line.getLineValue())
                            .build();
                })
                .toList();

        List<PosExchangeNewLineResponse> newLineResponses = newLines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return PosExchangeNewLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .taxRate(line.getTaxRate())
                            .lineTotal(line.getLineTotal())
                            .build();
                })
                .toList();

        return PosExchangeResponse.builder()
                .id(exchange.getId())
                .companyId(exchange.getCompanyId())
                .warehouseId(exchange.getWarehouseId())
                .registerId(exchange.getRegisterId())
                .registerName(register == null ? null : register.getName())
                .posSessionId(exchange.getPosSessionId())
                .originalPosSaleId(exchange.getOriginalPosSaleId())
                .originalSaleNumber(originalSale == null ? null : originalSale.getSaleNumber())
                .exchangeNumber(exchange.getExchangeNumber())
                .exchangeDate(exchange.getExchangeDate())
                .returnValue(exchange.getReturnValue())
                .returnTaxValue(exchange.getReturnTaxValue())
                .newValue(exchange.getNewValue())
                .newTaxValue(exchange.getNewTaxValue())
                .netAmount(exchange.getNetAmount())
                .settlementMethod(exchange.getSettlementMethod() == null ? null : exchange.getSettlementMethod().name())
                .settlementAmount(exchange.getSettlementAmount())
                .settlementReference(exchange.getSettlementReference())
                .createdBy(exchange.getCreatedBy())
                .returnLines(returnLineResponses)
                .newLines(newLineResponses)
                .build();
    }

    private PosExchange find(Long id) {
        return posExchangeRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POS exchange not found with id: " + id));
    }
}
