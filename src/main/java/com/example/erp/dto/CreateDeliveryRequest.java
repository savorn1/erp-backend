package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateDeliveryRequest {

    @NotNull
    private Long salesOrderId;

    @NotNull
    private LocalDate deliveryDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<DeliveryLineRequest> lines;
}
