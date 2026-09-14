package com.example.erp.dto;

import com.example.erp.entity.PaymentTerms;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Deliberately excludes `status` (own endpoint) and `currentBalance` (only
// ever changed via a balance adjustment) — see SupplierController/SupplierService.
@Data
public class UpdateSupplierRequest {

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

    private PaymentTerms paymentTerms;

    @DecimalMin(value = "0", message = "Credit limit cannot be negative")
    private BigDecimal creditLimit;
}
