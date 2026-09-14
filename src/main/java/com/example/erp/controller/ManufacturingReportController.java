package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.BomComparisonResponse;
import com.example.erp.dto.BomCostResponse;
import com.example.erp.dto.CostVarianceResponse;
import com.example.erp.dto.MachineCostResponse;
import com.example.erp.dto.MachineUtilizationResponse;
import com.example.erp.dto.ManufacturingCostResponse;
import com.example.erp.dto.ManufacturingProfitabilityResponse;
import com.example.erp.dto.ManufacturingReportFilterRequest;
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
import com.example.erp.service.ManufacturingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/manufacturing-reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManufacturingReportController {

    private final ManufacturingReportService manufacturingReportService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<MoSummaryResponse>> summary(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.summary(filter)));
    }

    @GetMapping("/material-consumption")
    public ResponseEntity<ApiResponse<MaterialConsumptionResponse>> materialConsumption(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.materialConsumption(filter)));
    }

    @GetMapping("/production-output")
    public ResponseEntity<ApiResponse<ProductionOutputResponse>> productionOutput(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.productionOutput(filter)));
    }

    @GetMapping("/scrap-wastage")
    public ResponseEntity<ApiResponse<ScrapWastageResponse>> scrapWastage(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.scrapWastage(filter)));
    }

    @GetMapping("/cost")
    public ResponseEntity<ApiResponse<ManufacturingCostResponse>> cost(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.cost(filter)));
    }

    @GetMapping("/rejections")
    public ResponseEntity<ApiResponse<RejectionsResponse>> rejections(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.rejections(filter)));
    }

    @GetMapping("/material-requirements")
    public ResponseEntity<ApiResponse<MaterialRequirementsResponse>> materialRequirements(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.materialRequirements(filter)));
    }

    @GetMapping("/bom-cost")
    public ResponseEntity<ApiResponse<BomCostResponse>> bomCost(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.bomCost(filter)));
    }

    @GetMapping("/cost-variance")
    public ResponseEntity<ApiResponse<CostVarianceResponse>> costVariance(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.costVariance(filter)));
    }

    @GetMapping("/production-time")
    public ResponseEntity<ApiResponse<ProductionTimeResponse>> productionTime(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.productionTime(filter)));
    }

    @GetMapping("/quality-pass-fail")
    public ResponseEntity<ApiResponse<QualityPassFailResponse>> qualityPassFail(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.qualityPassFail(filter)));
    }

    @GetMapping("/plan-vs-actual")
    public ResponseEntity<ApiResponse<PlanVsActualResponse>> planVsActual(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.planVsActual(filter)));
    }

    @GetMapping("/production-trend")
    public ResponseEntity<ApiResponse<ProductionTrendResponse>> productionTrend(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.productionTrend(filter)));
    }

    @GetMapping("/profitability")
    public ResponseEntity<ApiResponse<ManufacturingProfitabilityResponse>> profitability(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.profitability(filter)));
    }

    @GetMapping("/work-center-utilization")
    public ResponseEntity<ApiResponse<WorkCenterUtilizationResponse>> workCenterUtilization(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.workCenterUtilization(filter)));
    }

    @GetMapping("/machine-utilization")
    public ResponseEntity<ApiResponse<MachineUtilizationResponse>> machineUtilization(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.machineUtilization(filter)));
    }

    @GetMapping("/operation-performance")
    public ResponseEntity<ApiResponse<OperationPerformanceResponse>> operationPerformance(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.operationPerformance(filter)));
    }

    @GetMapping("/machine-cost")
    public ResponseEntity<ApiResponse<MachineCostResponse>> machineCost(@ModelAttribute ManufacturingReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.machineCost(filter)));
    }

    @GetMapping("/bom-comparison")
    public ResponseEntity<ApiResponse<BomComparisonResponse>> bomComparison(@RequestParam Long bomId, @RequestParam Long compareToBomId) {
        return ResponseEntity.ok(ApiResponse.success(manufacturingReportService.bomComparison(bomId, compareToBomId)));
    }
}
