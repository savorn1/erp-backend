package com.example.erp.service.impl;

import com.example.erp.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Shared per-line pricing math for every POS flow that prices a product
// against today's selling price/tax rate — POS checkout and the "new items"
// side of a POS exchange (PosSaleServiceImpl.checkout, PosExchangeServiceImpl).
@Component
public class PosPricingService {

    public record PricedLine(Long productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountPercent,
                              BigDecimal taxRate, BigDecimal net, BigDecimal tax, BigDecimal lineTotal, BigDecimal costPrice) {}

    public PricedLine price(Product product, BigDecimal quantity, BigDecimal discountPercentRaw) {
        BigDecimal discountPercent = discountPercentRaw == null ? BigDecimal.ZERO : discountPercentRaw;
        BigDecimal gross = quantity.multiply(product.getSellingPrice());
        BigDecimal discount = gross.multiply(discountPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(discount);
        BigDecimal tax = net.multiply(product.getTaxRate()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal lineTotal = net.add(tax);
        return new PricedLine(product.getId(), quantity, product.getSellingPrice(), discountPercent,
                product.getTaxRate(), net, tax, lineTotal, product.getCostPrice());
    }
}
