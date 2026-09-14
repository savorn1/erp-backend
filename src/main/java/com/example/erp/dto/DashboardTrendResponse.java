package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendResponse {

    // Oldest first, ending with the current month — one entry per month even
    // when a month has no activity (sales/purchase both zero).
    private List<DashboardTrendPointResponse> months;
}
