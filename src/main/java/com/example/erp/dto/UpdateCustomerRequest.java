package com.example.erp.dto;

import com.example.erp.entity.PaymentTerms;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Deliberately excludes `status` (own endpoint) and `currentBalance` (only
// ever changed via a balance adjustment) — see CustomerController/CustomerService.
@Data
public class UpdateCustomerRequest {

    @NotNull
    private Long companyId;

    private Long customerTypeId;
    private Long customerGroupId;

    @NotBlank
    private String name;

    private String contactName;
    private String phone;

    @Email
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

    @DecimalMin(value = "0", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;

    private PaymentTerms paymentTerms;
}
