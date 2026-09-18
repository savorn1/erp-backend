package com.example.erp.dto;

import com.example.erp.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long customerId;

    private Long productId;

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    private TicketPriority priority;

    private Long assignedToUserId;
}
