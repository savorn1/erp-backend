package com.example.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAttachmentRequest {

    @NotBlank
    private String ownerType;

    @NotNull
    private Long ownerId;

    @NotBlank
    private String key;

    @NotBlank
    private String url;

    @NotBlank
    private String fileName;

    private String contentType;

    private long size;
}
