package com.example.erp.dto;

import com.example.erp.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerStatusRequest {

    @NotNull
    private CustomerStatus status;
}
