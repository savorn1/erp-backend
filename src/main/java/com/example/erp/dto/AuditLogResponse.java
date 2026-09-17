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
public class AuditLogResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String module;
    private String action;
    private String httpMethod;
    private String path;
    private Long sourceId;
    private String actingUsername;
    private Integer statusCode;
    private String description;
    private LocalDateTime createdAt;
}
