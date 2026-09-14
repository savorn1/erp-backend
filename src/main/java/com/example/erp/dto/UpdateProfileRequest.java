package com.example.erp.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Email
    private String email;
}
