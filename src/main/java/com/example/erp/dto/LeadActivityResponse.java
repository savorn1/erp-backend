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
public class LeadActivityResponse {

    private Long id;
    private Long leadId;
    private String type;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}
