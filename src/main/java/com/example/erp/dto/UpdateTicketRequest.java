package com.example.erp.dto;

import com.example.erp.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTicketRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    @NotNull
    private TicketPriority priority;

    private Long productId;

    private Long assignedToUserId;
}
