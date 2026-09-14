package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApDetailResponse {

    private LocalDate asOfDate;
    // Every line of every currently-outstanding purchase invoice — sorted
    // by days overdue, descending.
    private List<ApDetailRowResponse> rows;
    private BigDecimal totalOutstanding;
}
