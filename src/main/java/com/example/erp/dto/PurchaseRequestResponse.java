package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long departmentId;
    private String departmentName;
    private String requestNumber;
    private LocalDate requestDate;
    private LocalDate requiredDate;
    private String status;
    private String notes;
    private String rejectionReason;
    private String requestedBy;
    private List<PurchaseRequestLineResponse> lines;
}
