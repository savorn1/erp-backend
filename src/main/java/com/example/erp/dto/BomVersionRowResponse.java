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
public class BomVersionRowResponse {

    private Long id;
    private String bomNumber;
    private Integer version;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private boolean current;
}
