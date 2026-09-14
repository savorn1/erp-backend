package com.example.erp.dto;

import com.example.erp.entity.PaymentTerms;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerRequest {

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
    private BigDecimal creditLimit = BigDecimal.ZERO;

    private PaymentTerms paymentTerms = PaymentTerms.NET_30;
}
