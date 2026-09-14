package com.example.erp.dto;

import com.example.erp.entity.PaymentTerms;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSupplierRequest {

    @NotNull
    private Long companyId;

    private Long supplierTypeId;

    @NotBlank
    private String name;

    private String contactName;
    private String phone;

    @Email
    private String email;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @DecimalMin(value = "0", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit = BigDecimal.ZERO;
}
