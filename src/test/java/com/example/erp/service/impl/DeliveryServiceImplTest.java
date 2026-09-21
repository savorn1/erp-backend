package com.example.erp.service.impl;

import com.example.erp.entity.Delivery;
import com.example.erp.entity.DeliveryLine;
import com.example.erp.entity.DeliveryStatus;
import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductTrackingType;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.StockMovement;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.repository.ProductBatchRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.StockMovementRepository;
import com.example.erp.repository.WarehouseBinRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.repository.WarehouseZoneRepository;
import com.example.erp.service.InventorySettingsService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the unit conversion in {@code shipDelivery} — the one place a sales UoM
 * turns into an actual stock movement.
 *
 * <p>A delivery line's {@code quantityDelivered} is in the sales order line's unit
 * (e.g. BOX), while stock, reservations and stock movements are all in the
 * product's inventory unit (e.g. PCS). Getting that boundary wrong doesn't throw —
 * it just silently issues the wrong amount of stock, which is why it's worth
 * pinning down.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryServiceImplTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private DeliveryLineRepository deliveryLineRepository;
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private SalesOrderLineRepository salesOrderLineRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private WarehouseZoneRepository warehouseZoneRepository;
    @Mock private WarehouseBinRepository warehouseBinRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductBatchRepository productBatchRepository;
    @Mock private SerialNumberRepository serialNumberRepository;
    @Mock private InventorySettingsService inventorySettingsService;

    @InjectMocks
    private DeliveryServiceImpl service;

    private static final long DELIVERY_ID = 1L;
    private static final long SO_ID = 10L;
    private static final long SO_LINE_ID = 100L;
    private static final long PRODUCT_ID = 50L;
    private static final long WAREHOUSE_ID = 7L;
    private static final long COMPANY_ID = 3L;

    private Delivery delivery;
    private SalesOrder salesOrder;
    private StockLevel stockLevel;

    @BeforeEach
    void setUp() {
        delivery = Delivery.builder()
                .salesOrderId(SO_ID)
                .warehouseId(WAREHOUSE_ID)
                .status(DeliveryStatus.PACKED)
                .build();
        delivery.setId(DELIVERY_ID);

        salesOrder = SalesOrder.builder()
                .companyId(COMPANY_ID)
                .warehouseId(WAREHOUSE_ID)
                .soNumber("SO-000010")
                .status(SalesOrderStatus.CONFIRMED)
                .build();
        salesOrder.setId(SO_ID);

        Product product = Product.builder()
                .name("Widget")
                .trackingType(ProductTrackingType.NONE)
                .build();
        product.setId(PRODUCT_ID);

        when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
        when(salesOrderRepository.findById(SO_ID)).thenReturn(Optional.of(salesOrder));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(Warehouse.builder().name("Main").build()));
        // reserveStock off keeps this focused on the issue path; allowNegativeStock off
        // so the availability check is actually enforced.
        when(inventorySettingsService.resolveForCompany(COMPANY_ID)).thenReturn(
                InventorySettings.builder().allowNegativeStock(false).reserveStock(false).build());
    }

    @Test
    void shippingABoxLineIssuesStockInBaseUnitsNotBoxes() {
        // 2 BOX of 12 = 24 PCS out of 100 PCS on hand.
        givenLine(new BigDecimal("2"), new BigDecimal("12"), new BigDecimal("100"));

        service.shipDelivery(DELIVERY_ID, "tester");

        assertThat(stockLevel.getQuantityOnHand()).isEqualByComparingTo("76");

        ArgumentCaptor<StockMovement> movement = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movement.capture());
        assertThat(movement.getValue().getQuantityDelta()).isEqualByComparingTo("-24");
    }

    @Test
    void theSalesOrderLineIsCreditedInItsOwnUnitNotBaseUnits() {
        // The delivered counter is compared against quantityOrdered, which is in BOX —
        // crediting it 24 would mark a 2-box order delivered twelve times over.
        SalesOrderLine soLine = givenLine(new BigDecimal("2"), new BigDecimal("12"), new BigDecimal("100"));

        service.shipDelivery(DELIVERY_ID, "tester");

        assertThat(soLine.getQuantityDelivered()).isEqualByComparingTo("2");
    }

    @Test
    void availabilityIsCheckedAgainstTheConvertedQuantity() {
        // 2 BOX = 24 PCS, but only 20 PCS on hand — this must be rejected even though
        // the raw line quantity (2) looks comfortably available.
        givenLine(new BigDecimal("2"), new BigDecimal("12"), new BigDecimal("20"));

        assertThatThrownBy(() -> service.shipDelivery(DELIVERY_ID, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Insufficient stock on hand");

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void aLineWithNoConversionFactorIsTreatedAsBaseUnits() {
        // Rows written before UoM support carry a null factor; they must keep behaving
        // exactly as they did — quantity moves one-for-one.
        givenLine(new BigDecimal("5"), null, new BigDecimal("100"));

        service.shipDelivery(DELIVERY_ID, "tester");

        assertThat(stockLevel.getQuantityOnHand()).isEqualByComparingTo("95");
    }

    @Test
    void fractionalFactorsConvertExactly() {
        // 3 lots of 0.5 KG = 1.5 KG — no rounding to whole units anywhere.
        givenLine(new BigDecimal("3"), new BigDecimal("0.5"), new BigDecimal("10"));

        service.shipDelivery(DELIVERY_ID, "tester");

        assertThat(stockLevel.getQuantityOnHand()).isEqualByComparingTo("8.5");
    }

    /** Wires up one delivery line against one SO line, with the given stock on hand. */
    private SalesOrderLine givenLine(BigDecimal quantityInLineUnit, BigDecimal conversionFactor, BigDecimal onHand) {
        SalesOrderLine soLine = SalesOrderLine.builder()
                .salesOrderId(SO_ID)
                .productId(PRODUCT_ID)
                .conversionFactor(conversionFactor)
                // Ordered generously so the order doesn't flip to fully-delivered, which
                // would drag SalesOrderStatus transitions into these assertions.
                .quantityOrdered(new BigDecimal("1000"))
                .quantityDelivered(BigDecimal.ZERO)
                .unitPrice(BigDecimal.ONE)
                .build();
        soLine.setId(SO_LINE_ID);

        DeliveryLine deliveryLine = DeliveryLine.builder()
                .deliveryId(DELIVERY_ID)
                .salesOrderLineId(SO_LINE_ID)
                .productId(PRODUCT_ID)
                .quantityDelivered(quantityInLineUnit)
                .conversionFactor(conversionFactor)
                .build();
        deliveryLine.setId(200L);

        stockLevel = StockLevel.builder()
                .companyId(COMPANY_ID)
                .productId(PRODUCT_ID)
                .warehouseId(WAREHOUSE_ID)
                .quantityOnHand(onHand)
                .build();

        when(deliveryLineRepository.findByDeliveryId(DELIVERY_ID)).thenReturn(List.of(deliveryLine));
        when(salesOrderLineRepository.findBySalesOrderId(SO_ID)).thenReturn(List.of(soLine));
        when(stockLevelRepository.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID)).thenReturn(List.of(stockLevel));
        return soLine;
    }
}
