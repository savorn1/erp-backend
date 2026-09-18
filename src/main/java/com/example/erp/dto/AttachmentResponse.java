package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private String ownerType;
    private Long ownerId;
    private String key;
    private String url;
    private String fileName;
    private String contentType;
    private long size;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
