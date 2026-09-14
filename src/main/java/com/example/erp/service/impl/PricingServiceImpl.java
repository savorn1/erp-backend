package com.example.erp.service.impl;

import com.example.erp.dto.PriceLookupResponse;
import com.example.erp.entity.Customer;
import com.example.erp.entity.CustomerGroup;
import com.example.erp.entity.PriceGroup;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductPrice;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductPriceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Explains, for a (customer, product) pair, the same pricing cascade
// SalesOrderServiceImpl.resolveUnitPrice applies when adding a sales order
// line — a read-only support/admin tool, not part of the order flow itself.
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final PriceGroupRepository priceGroupRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PriceLookupResponse lookup(Long companyId, Long customerId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId));
        if (!product.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company: " + customer.getName());
        }

        CustomerGroup customerGroup = customer.getCustomerGroupId() == null
                ? null : customerGroupRepository.findById(customer.getCustomerGroupId()).orElse(null);
        PriceGroup priceGroup = customerGroup == null || customerGroup.getPriceGroupId() == null
                ? null : priceGroupRepository.findById(customerGroup.getPriceGroupId()).orElse(null);

        PriceLookupResponse.PriceLookupResponseBuilder response = PriceLookupResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .sellingPrice(product.getSellingPrice())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerGroupId(customerGroup == null ? null : customerGroup.getId())
                .customerGroupName(customerGroup == null ? null : customerGroup.getName())
                .priceGroupId(priceGroup == null ? null : priceGroup.getId())
                .priceGroupName(priceGroup == null ? null : priceGroup.getName())
                .discountPercent(priceGroup == null ? null : priceGroup.getDiscountPercent());

        if (priceGroup != null) {
            ProductPrice override = productPriceRepository
                    .findByProductIdAndPriceGroupId(productId, priceGroup.getId())
                    .orElse(null);
            if (override != null) {
                return response
                        .productPriceId(override.getId())
                        .resolvedUnitPrice(override.getPrice())
                        .source("PRODUCT_PRICE_OVERRIDE")
                        .build();
            }
            if (priceGroup.getDiscountPercent() != null) {
                BigDecimal factor = BigDecimal.ONE.subtract(priceGroup.getDiscountPercent().divide(BigDecimal.valueOf(100)));
                BigDecimal discounted = product.getSellingPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
                return response
                        .resolvedUnitPrice(discounted)
                        .source("PRICE_GROUP_DISCOUNT")
                        .build();
            }
        }

        return response
                .resolvedUnitPrice(product.getSellingPrice())
                .source("SELLING_PRICE")
                .build();
    }
}
