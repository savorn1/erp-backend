package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTicketCommentRequest {

    @NotBlank
    private String body;

    private boolean internal = false;
}
