package com.example.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWarehouseRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String name;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private Long managerId;

    private String phone;

    @Email
    private String email;

    private String timezone;
}
