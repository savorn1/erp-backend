package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long bomId;
    private String bomNumber;
    private String routingNumber;
    private String name;
    private String status;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<RoutingOperationResponse> operations;
}
