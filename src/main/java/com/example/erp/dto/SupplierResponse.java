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
public class SupplierResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long supplierTypeId;
    private String supplierTypeName;
    private String name;

    private String contactName;
    private String phone;
    private String email;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String paymentTerms;
    private BigDecimal creditLimit;
    private String status;
    private BigDecimal currentBalance;
}
