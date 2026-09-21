package com.example.erp.service.impl;

import com.example.erp.dto.CreateStockAdjustmentRequest;
import com.example.erp.dto.StockAdjustmentResponse;
import com.example.erp.dto.StockCountLineCountRequest;
import com.example.erp.dto.StockCountResponse;
import com.example.erp.dto.SubmitStockCountRequest;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.StockAdjustmentReason;
import com.example.erp.entity.StockCount;
import com.example.erp.entity.StockCountLine;
import com.example.erp.entity.StockCountStatus;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.StockCountLineRepository;
import com.example.erp.repository.StockCountRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.StockAdjustmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the unit conversion on a stock count.
 *
 * <p>A count sheet is filled in whatever unit the warehouse handles — cases,
 * pallets — while {@code StockLevel} and the reconciliation adjustment are in
 * the product's base unit. Getting that boundary wrong doesn't throw: it posts
 * a variance that destroys or invents real stock, which is exactly what a
 * count is supposed to prevent.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockCountServiceImplTest {

    @Mock private StockCountRepository stockCountRepository;
    @Mock private StockCountLineRepository stockCountLineRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private WarehouseZoneRepository warehouseZoneRepository;
    @Mock private WarehouseBinRepository warehouseBinRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductUomRepository productUomRepository;
    @Mock private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private StockAdjustmentService stockAdjustmentService;

    @InjectMocks
    private StockCountServiceImpl service;

    private static final long COUNT_ID = 1L;
    private static final long LINE_ID = 100L;
    private static final long PRODUCT_ID = 50L;
    private static final long BASE_UNIT_ID = 5L;
    private static final long CASE_UNIT_ID = 6L;

    private StockCount count;
    private StockCountLine line;

    @BeforeEach
    void setUp() {
        count = StockCount.builder()
                .companyId(3L)
                .warehouseId(7L)
                .countNumber("SC-000001")
                .status(StockCountStatus.DRAFT)
                .build();
        count.setId(COUNT_ID);

        Product product = Product.builder()
                .name("Bottled water")
                .unitOfMeasureId(BASE_UNIT_ID)
                .trackingType(ProductTrackingType.NONE)
                .build();
        product.setId(PRODUCT_ID);

        when(stockCountRepository.findById(COUNT_ID)).thenReturn(Optional.of(count));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(warehouseRepository.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void countingInCasesStoresBaseUnits() {
        // 3 cases of 12 against 36 on hand — no variance at all.
        givenLine(new BigDecimal("12"), new BigDecimal("36"));

        service.submitCounts(COUNT_ID, submit(new BigDecimal("3")));

        assertThat(line.getCountedQuantity()).isEqualByComparingTo("36");
        assertThat(line.getVarianceQuantity()).isEqualByComparingTo("0");
    }

    @Test
    void aShortCaseIsAVarianceInBaseUnitsNotCases() {
        // 3 cases counted, 37 bottles on the system: one bottle unaccounted
        // for. Reporting -0.0833 cases here would reconcile to the wrong
        // number of bottles once multiplied back out.
        givenLine(new BigDecimal("12"), new BigDecimal("37"));

        service.submitCounts(COUNT_ID, submit(new BigDecimal("3")));

        assertThat(line.getVarianceQuantity()).isEqualByComparingTo("-1");
    }

    @Test
    void aLineWithNoConversionFactorIsTreatedAsBaseUnits() {
        // Counts written before units existed carry a null factor and must
        // keep behaving exactly as they did — one for one.
        givenLine(null, new BigDecimal("40"));

        service.submitCounts(COUNT_ID, submit(new BigDecimal("38")));

        assertThat(line.getCountedQuantity()).isEqualByComparingTo("38");
        assertThat(line.getVarianceQuantity()).isEqualByComparingTo("-2");
    }

    @Test
    void reconcileAdjustsInBaseUnits() {
        // The whole point of storing the variance in base units: it feeds the
        // adjustment untouched, so the stock movement is in bottles.
        givenLine(new BigDecimal("12"), new BigDecimal("37"));
        line.setCountedQuantity(new BigDecimal("36"));
        line.setVarianceQuantity(new BigDecimal("-1"));
        count.setStatus(StockCountStatus.COMPLETED);
        StockAdjustmentResponse created = StockAdjustmentResponse.builder().id(900L).adjustmentNumber("ADJ-000900").build();
        when(stockAdjustmentService.createStockAdjustment(any(), anyString())).thenReturn(created);
        // The response builder looks the adjustment back up for its number.
        when(stockAdjustmentService.getStockAdjustment(900L)).thenReturn(created);

        service.reconcileStockCount(COUNT_ID, "tester");

        ArgumentCaptor<CreateStockAdjustmentRequest> request = ArgumentCaptor.forClass(CreateStockAdjustmentRequest.class);
        verify(stockAdjustmentService).createStockAdjustment(request.capture(), anyString());
        assertThat(request.getValue().getLines()).singleElement().satisfies(adjustmentLine -> {
            assertThat(adjustmentLine.getQuantity()).isEqualByComparingTo("1");
            assertThat(adjustmentLine.getReason()).isEqualTo(StockAdjustmentReason.STOCK_DECREASE);
        });
    }

    @Test
    void aCountThatMatchesPostsNoAdjustment() {
        givenLine(new BigDecimal("12"), new BigDecimal("36"));
        line.setCountedQuantity(new BigDecimal("36"));
        line.setVarianceQuantity(BigDecimal.ZERO);
        count.setStatus(StockCountStatus.COMPLETED);

        service.reconcileStockCount(COUNT_ID, "tester");

        verify(stockAdjustmentService, never()).createStockAdjustment(any(), anyString());
        assertThat(count.getStatus()).isEqualTo(StockCountStatus.RECONCILED);
    }

    @Test
    void theResponseShowsBothBaseAndCaseQuantities() {
        // The warehouse works the sheet in cases; the adjustment is in
        // bottles. Both have to be legible on the same row.
        givenLine(new BigDecimal("12"), new BigDecimal("36"));

        StockCountResponse response = service.submitCounts(COUNT_ID, submit(new BigDecimal("3")));

        assertThat(response.getLines()).singleElement().satisfies(lineResponse -> {
            assertThat(lineResponse.getCountedQuantity()).isEqualByComparingTo("36");
            assertThat(lineResponse.getCountedQuantityInUnit()).isEqualByComparingTo("3");
            assertThat(lineResponse.getSystemQuantityInUnit()).isEqualByComparingTo("3");
            assertThat(lineResponse.getConversionFactor()).isEqualByComparingTo("12");
        });
    }

    private void givenLine(BigDecimal conversionFactor, BigDecimal systemQuantityInBaseUnits) {
        line = StockCountLine.builder()
                .stockCountId(COUNT_ID)
                .productId(PRODUCT_ID)
                .unitOfMeasureId(conversionFactor == null ? BASE_UNIT_ID : CASE_UNIT_ID)
                .conversionFactor(conversionFactor)
                .systemQuantity(systemQuantityInBaseUnits)
                .build();
        line.setId(LINE_ID);
        when(stockCountLineRepository.findByStockCountId(COUNT_ID)).thenReturn(List.of(line));
    }

    private SubmitStockCountRequest submit(BigDecimal countedInLineUnit) {
        StockCountLineCountRequest lineRequest = new StockCountLineCountRequest();
        lineRequest.setLineId(LINE_ID);
        lineRequest.setCountedQuantity(countedInLineUnit);
        SubmitStockCountRequest request = new SubmitStockCountRequest();
        request.setLines(List.of(lineRequest));
        return request;
    }
}
