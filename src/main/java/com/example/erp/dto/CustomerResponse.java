package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerTypeId;
    private String customerTypeName;
    private Long customerGroupId;
    private String customerGroupName;
    private String name;

    private String contactName;
    private String phone;
    private String email;

    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    private BigDecimal creditLimit;
    private String paymentTerms;
    private String status;
    private BigDecimal currentBalance;
}
