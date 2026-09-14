package com.example.erp.service;

import com.example.erp.dto.BomComparisonResponse;
import com.example.erp.dto.BomCostResponse;
import com.example.erp.dto.CostVarianceResponse;
import com.example.erp.dto.ManufacturingCostResponse;
import com.example.erp.dto.ManufacturingProfitabilityResponse;
import com.example.erp.dto.ManufacturingReportFilterRequest;
import com.example.erp.dto.MachineCostResponse;
import com.example.erp.dto.MachineUtilizationResponse;
import com.example.erp.dto.MaterialConsumptionResponse;
import com.example.erp.dto.MaterialRequirementsResponse;
import com.example.erp.dto.MoSummaryResponse;
import com.example.erp.dto.OperationPerformanceResponse;
import com.example.erp.dto.PlanVsActualResponse;
import com.example.erp.dto.ProductionOutputResponse;
import com.example.erp.dto.ProductionTimeResponse;
import com.example.erp.dto.ProductionTrendResponse;
import com.example.erp.dto.QualityPassFailResponse;
import com.example.erp.dto.RejectionsResponse;
import com.example.erp.dto.ScrapWastageResponse;
import com.example.erp.dto.WorkCenterUtilizationResponse;

public interface ManufacturingReportService {

    MoSummaryResponse summary(ManufacturingReportFilterRequest filter);

    MaterialConsumptionResponse materialConsumption(ManufacturingReportFilterRequest filter);

    ProductionOutputResponse productionOutput(ManufacturingReportFilterRequest filter);

    ScrapWastageResponse scrapWastage(ManufacturingReportFilterRequest filter);

    ManufacturingCostResponse cost(ManufacturingReportFilterRequest filter);

    RejectionsResponse rejections(ManufacturingReportFilterRequest filter);

    MaterialRequirementsResponse materialRequirements(ManufacturingReportFilterRequest filter);

    BomCostResponse bomCost(ManufacturingReportFilterRequest filter);

    CostVarianceResponse costVariance(ManufacturingReportFilterRequest filter);

    ProductionTimeResponse productionTime(ManufacturingReportFilterRequest filter);

    QualityPassFailResponse qualityPassFail(ManufacturingReportFilterRequest filter);

    PlanVsActualResponse planVsActual(ManufacturingReportFilterRequest filter);

    ProductionTrendResponse productionTrend(ManufacturingReportFilterRequest filter);

    ManufacturingProfitabilityResponse profitability(ManufacturingReportFilterRequest filter);

    WorkCenterUtilizationResponse workCenterUtilization(ManufacturingReportFilterRequest filter);

    MachineUtilizationResponse machineUtilization(ManufacturingReportFilterRequest filter);

    OperationPerformanceResponse operationPerformance(ManufacturingReportFilterRequest filter);

    MachineCostResponse machineCost(ManufacturingReportFilterRequest filter);

    BomComparisonResponse bomComparison(Long bomId, Long compareToBomId);
}
