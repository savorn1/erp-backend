package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class AuditLogFilterRequest {

    private Long companyId;
    private String module;
    private String action;
    private String actingUsername;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 20;
}
