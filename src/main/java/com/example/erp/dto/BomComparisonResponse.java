package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomComparisonResponse {

    private BomComparisonSideResponse left;
    private BomComparisonSideResponse right;
}
