package com.example.erp.service;

import com.example.erp.dto.DashboardSummaryFilterRequest;
import com.example.erp.dto.DashboardSummaryResponse;
import com.example.erp.dto.DashboardTrendFilterRequest;
import com.example.erp.dto.DashboardTrendResponse;

public interface DashboardService {

    DashboardSummaryResponse summary(DashboardSummaryFilterRequest filter);

    DashboardTrendResponse trend(DashboardTrendFilterRequest filter);
}
