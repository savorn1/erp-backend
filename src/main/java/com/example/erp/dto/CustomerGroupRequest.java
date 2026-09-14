package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerGroupRequest {

    @NotBlank
    private String name;

    private Long priceGroupId;

    private boolean active = true;
}
