package com.example.erp.service.impl;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosCheckoutLineRequest;
import com.example.erp.dto.PosHeldSaleFilterRequest;
import com.example.erp.dto.PosHeldSaleLineResponse;
import com.example.erp.dto.PosHeldSaleResponse;
import com.example.erp.dto.PosHoldRequest;
import com.example.erp.entity.Customer;
import com.example.erp.entity.PosHeldSale;
import com.example.erp.entity.PosHeldSaleLine;
import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.Register;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.PosHeldSaleLineRepository;
import com.example.erp.repository.PosHeldSaleRepository;
import com.example.erp.repository.PosSessionRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.RegisterRepository;
import com.example.erp.service.PosHeldSaleService;
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
public class PosHeldSaleServiceImpl implements PosHeldSaleService {

    private final PosHeldSaleRepository posHeldSaleRepository;
    private final PosHeldSaleLineRepository posHeldSaleLineRepository;
    private final PosSessionRepository posSessionRepository;
    private final RegisterRepository registerRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PosHeldSaleResponse> list(PosHeldSaleFilterRequest filter) {
        List<Specification<PosHeldSale>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getRegisterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("registerId"), filter.getRegisterId()));
        if (filter.getPosSessionId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("posSessionId"), filter.getPosSessionId()));
        Specification<PosHeldSale> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PosHeldSale> page = posHeldSaleRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public PosHeldSaleResponse hold(PosHoldRequest request, String actingUsername) {
        PosSession session = posSessionRepository.findById(request.getPosSessionId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "POS session not found with id: " + request.getPosSessionId()));
        if (session.getStatus() != PosSessionStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This session is closed — open a new one to hold a sale");
        }
        Register register = registerRepository.findById(session.getRegisterId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Register not found with id: " + session.getRegisterId()));

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + request.getCustomerId()));
            if (!customer.getCompanyId().equals(register.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to this register's company");
            }
        }

        PosHeldSale held = PosHeldSale.builder()
                .companyId(register.getCompanyId())
                .warehouseId(register.getWarehouseId())
                .registerId(register.getId())
                .posSessionId(session.getId())
                .customerId(request.getCustomerId())
                .heldAt(LocalDateTime.now())
                .heldBy(actingUsername)
                .note(request.getNote())
                .build();
        posHeldSaleRepository.save(held);
        held.setHeldNumber("HOLD-" + String.format("%06d", held.getId()));
        posHeldSaleRepository.save(held);

        List<PosHeldSaleLine> lines = new ArrayList<>();
        for (PosCheckoutLineRequest lineRequest : request.getLines()) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + lineRequest.getProductId()));
            if (!product.getCompanyId().equals(register.getCompanyId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to this register's company: " + product.getName());
            }
            lines.add(posHeldSaleLineRepository.save(PosHeldSaleLine.builder()
                    .posHeldSaleId(held.getId())
                    .productId(lineRequest.getProductId())
                    .quantity(lineRequest.getQuantity())
                    .discountPercent(lineRequest.getDiscountPercent() == null ? BigDecimal.ZERO : lineRequest.getDiscountPercent())
                    .build()));
        }

        return toResponse(held, lines);
    }

    @Override
    @Transactional
    public PosHeldSaleResponse resume(Long id) {
        PosHeldSale held = find(id);
        PosHeldSaleResponse response = toResponse(held, null);
        posHeldSaleLineRepository.deleteByPosHeldSaleId(id);
        posHeldSaleRepository.delete(held);
        return response;
    }

    @Override
    @Transactional
    public void discard(Long id) {
        PosHeldSale held = find(id);
        posHeldSaleLineRepository.deleteByPosHeldSaleId(id);
        posHeldSaleRepository.delete(held);
    }

    private PosHeldSaleResponse toResponse(PosHeldSale held) {
        return toResponse(held, null);
    }

    private PosHeldSaleResponse toResponse(PosHeldSale held, List<PosHeldSaleLine> preloadedLines) {
        List<PosHeldSaleLine> lines = preloadedLines != null ? preloadedLines : posHeldSaleLineRepository.findByPosHeldSaleId(held.getId());

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(PosHeldSaleLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        Register register = registerRepository.findById(held.getRegisterId()).orElse(null);
        Customer customer = held.getCustomerId() == null ? null : customerRepository.findById(held.getCustomerId()).orElse(null);

        List<PosHeldSaleLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return PosHeldSaleLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .imageUrl(product == null ? null : product.getImageUrl())
                            .quantity(line.getQuantity())
                            .discountPercent(line.getDiscountPercent())
                            .build();
                })
                .toList();

        int itemCount = lines.stream().mapToInt(l -> l.getQuantity().setScale(0, RoundingMode.CEILING).intValue()).sum();
        BigDecimal estimatedTotal = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    if (product == null) return BigDecimal.ZERO;
                    BigDecimal gross = line.getQuantity().multiply(product.getSellingPrice());
                    BigDecimal net = gross.subtract(gross.multiply(line.getDiscountPercent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                    BigDecimal tax = net.multiply(product.getTaxRate()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                    return net.add(tax);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PosHeldSaleResponse.builder()
                .id(held.getId())
                .companyId(held.getCompanyId())
                .warehouseId(held.getWarehouseId())
                .registerId(held.getRegisterId())
                .registerName(register == null ? null : register.getName())
                .posSessionId(held.getPosSessionId())
                .customerId(held.getCustomerId())
                .customerName(customer == null ? null : customer.getName())
                .heldNumber(held.getHeldNumber())
                .heldAt(held.getHeldAt())
                .heldBy(held.getHeldBy())
                .note(held.getNote())
                .itemCount(itemCount)
                .estimatedTotal(estimatedTotal)
                .lines(lineResponses)
                .build();
    }

    private PosHeldSale find(Long id) {
        return posHeldSaleRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Held sale not found with id: " + id));
    }
}
