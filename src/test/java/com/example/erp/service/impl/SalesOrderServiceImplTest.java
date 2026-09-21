package com.example.erp.service.impl;

import com.example.erp.dto.CreateSalesOrderFromQuotationRequest;
import com.example.erp.dto.CreateSalesOrderRequest;
import com.example.erp.dto.SalesOrderLineRequest;
import com.example.erp.entity.InventorySettings;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductPrice;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.Quotation;
import com.example.erp.entity.QuotationLine;
import com.example.erp.entity.QuotationStatus;
import com.example.erp.entity.SalesOrder;
import com.example.erp.entity.SalesOrderLine;
import com.example.erp.entity.SalesOrderStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerGroupRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.PriceGroupRepository;
import com.example.erp.repository.ProductPriceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.QuotationLineRepository;
import com.example.erp.repository.QuotationRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.EmailService;
import com.example.erp.service.InventorySettingsService;
import com.example.erp.service.PdfRenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderLineRepository lineRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductUomRepository productUomRepository;
    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;
    @Mock
    private StockLevelRepository stockLevelRepository;
    @Mock
    private CustomerGroupRepository customerGroupRepository;
    @Mock
    private ProductPriceRepository productPriceRepository;
    @Mock
    private PriceGroupRepository priceGroupRepository;
    @Mock
    private StockAvailabilityService stockAvailabilityService;
    @Mock
    private InventorySettingsService inventorySettingsService;
    @Mock
    private ApprovalWorkflowService approvalWorkflowService;
    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private QuotationLineRepository quotationLineRepository;
    @Mock
    private PdfRenderService pdfRenderService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private SalesOrderServiceImpl salesOrderService;

    @Test
    void createFromSalesQuotation_rejectsNonAcceptedQuotation() {
        Quotation quotation = Quotation.builder().id(1L).companyId(1L).status(QuotationStatus.SENT).build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        CreateSalesOrderFromQuotationRequest request = new CreateSalesOrderFromQuotationRequest();
        request.setWarehouseId(1L);
        request.setOrderDate(LocalDate.now());

        assertThatThrownBy(() -> salesOrderService.createFromSalesQuotation(1L, request, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("accepted");
    }

    @Test
    void createFromSalesQuotation_rejectsExpiredQuotation() {
        Quotation quotation = Quotation.builder()
                .id(1L).companyId(1L).customerId(5L)
                .status(QuotationStatus.ACCEPTED)
                .validUntil(LocalDate.now().minusDays(1))
                .build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        CreateSalesOrderFromQuotationRequest request = new CreateSalesOrderFromQuotationRequest();
        request.setWarehouseId(1L);
        request.setOrderDate(LocalDate.now());

        assertThatThrownBy(() -> salesOrderService.createFromSalesQuotation(1L, request, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void createFromSalesQuotation_createsSalesOrderWithCopiedLines() {
        Quotation quotation = Quotation.builder()
                .id(1L).companyId(1L).customerId(5L)
                .status(QuotationStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        Warehouse warehouse = Warehouse.builder().id(2L).companyId(1L).build();
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));

        QuotationLine quotationLine = QuotationLine.builder()
                .id(10L).quotationId(1L).productId(7L)
                .quantity(BigDecimal.valueOf(3)).unitPrice(BigDecimal.TEN)
                .build();
        when(quotationLineRepository.findByQuotationId(1L)).thenReturn(List.of(quotationLine));

        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder so = invocation.getArgument(0);
            if (so.getId() == null) so.setId(100L);
            return so;
        });
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());
        when(lineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());

        CreateSalesOrderFromQuotationRequest request = new CreateSalesOrderFromQuotationRequest();
        request.setWarehouseId(2L);
        request.setOrderDate(LocalDate.now());

        var response = salesOrderService.createFromSalesQuotation(1L, request, "tester");

        assertThat(response.getQuotationId()).isEqualTo(1L);
        assertThat(response.getCustomerId()).isEqualTo(5L);
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getQuantityOrdered()).isEqualByComparingTo("3");
    }

    @Test
    void cancelSalesOrderLine_rejectsWhenOrderNotConfirmed() {
        SalesOrder so = SalesOrder.builder().id(1L).companyId(1L).status(SalesOrderStatus.DRAFT).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));

        assertThatThrownBy(() -> salesOrderService.cancelSalesOrderLine(1L, 10L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("confirmed");
    }

    @Test
    void cancelSalesOrderLine_rejectsWhenNothingRemaining() {
        SalesOrder so = SalesOrder.builder().id(1L).companyId(1L).warehouseId(2L).status(SalesOrderStatus.CONFIRMED).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        SalesOrderLine line = SalesOrderLine.builder()
                .id(10L).salesOrderId(1L).productId(7L)
                .quantityOrdered(BigDecimal.valueOf(5)).quantityDelivered(BigDecimal.valueOf(5))
                .build();
        when(lineRepository.findById(10L)).thenReturn(Optional.of(line));

        assertThatThrownBy(() -> salesOrderService.cancelSalesOrderLine(1L, 10L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Nothing remaining");
    }

    @Test
    void cancelSalesOrderLine_reducesQuantityOrderedAndReleasesReservation() {
        SalesOrder so = SalesOrder.builder().id(1L).companyId(1L).warehouseId(2L).status(SalesOrderStatus.CONFIRMED).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        SalesOrderLine line = SalesOrderLine.builder()
                .id(10L).salesOrderId(1L).productId(7L)
                .quantityOrdered(BigDecimal.valueOf(5)).quantityDelivered(BigDecimal.valueOf(2))
                .unitPrice(BigDecimal.TEN)
                .build();
        when(lineRepository.findById(10L)).thenReturn(Optional.of(line));
        when(lineRepository.findBySalesOrderId(1L)).thenReturn(List.of(line));

        InventorySettings settings = InventorySettings.builder().reserveStock(true).build();
        when(inventorySettingsService.resolveForCompany(1L)).thenReturn(settings);
        StockLevel stockLevel = StockLevel.builder()
                .companyId(1L).productId(7L).warehouseId(2L)
                .quantityOnHand(BigDecimal.TEN).reservedQuantity(BigDecimal.valueOf(3))
                .build();
        when(stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(7L, 2L)).thenReturn(Optional.of(stockLevel));
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(customerRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(warehouseRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(productRepository.findAllById(any())).thenReturn(List.of());

        salesOrderService.cancelSalesOrderLine(1L, 10L);

        assertThat(line.getQuantityOrdered()).isEqualByComparingTo("2");
        assertThat(stockLevel.getReservedQuantity()).isEqualByComparingTo("0");
    }

    @Test
    void cancelSalesOrder_recordsHowFarTheWorkflowGot() {
        // Overwriting status destroys the only evidence of the order's
        // progress, and that is what tells someone reading the cancelled
        // order whether stock was reserved and has to be unwound. Capturing
        // it has to happen before the overwrite, so the ordering is the test.
        SalesOrder so = SalesOrder.builder().id(1L).companyId(1L).warehouseId(2L).status(SalesOrderStatus.CONFIRMED).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(inventorySettingsService.resolveForCompany(1L)).thenReturn(InventorySettings.builder().reserveStock(false).build());
        when(lineRepository.findBySalesOrderId(1L)).thenReturn(List.of());
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(customerRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(warehouseRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(productRepository.findAllById(any())).thenReturn(List.of());

        salesOrderService.cancelSalesOrder(1L);

        assertThat(so.getStatus()).isEqualTo(SalesOrderStatus.CANCELLED);
        assertThat(so.getCancelledFromStatus()).isEqualTo(SalesOrderStatus.CONFIRMED);
    }

    @Test
    void cancelSalesOrder_recordsDraftForAnOrderThatNeverProgressed() {
        // The other end of the same rule: a draft cancelled on day one has to
        // be distinguishable from one cancelled after delivery started.
        SalesOrder so = SalesOrder.builder().id(1L).companyId(1L).warehouseId(2L).status(SalesOrderStatus.DRAFT).build();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        lenient().when(inventorySettingsService.resolveForCompany(1L)).thenReturn(InventorySettings.builder().reserveStock(false).build());
        lenient().when(lineRepository.findBySalesOrderId(1L)).thenReturn(List.of());
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(customerRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(warehouseRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(productRepository.findAllById(any())).thenReturn(List.of());

        salesOrderService.cancelSalesOrder(1L);

        assertThat(so.getCancelledFromStatus()).isEqualTo(SalesOrderStatus.DRAFT);
    }

    @Test
    void anAutoPricedCaseCostsTheCaseNotTheBottle() {
        givenPlainCustomer();
        // The core of the bug: resolveUnitPrice returns the product's
        // base-unit price, but quantityOrdered is in the line's unit, so
        // quantity * unitPrice billed a twelve-bottle case as one bottle
        // while the delivery correctly shipped twelve.
        givenProductPricedAt("2.00");
        givenSalesUom(CASE_UNIT_ID, "12");

        var response = createOrderWith(CASE_UNIT_ID, "1", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("24.0000");
    }

    @Test
    void anAutoPricedBaseUnitLineIsUnchanged() {
        givenPlainCustomer();
        // Factor 1 must be a no-op — every existing order is this case.
        givenProductPricedAt("2.00");

        var response = createOrderWith(null, "5", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("2.00");
    }

    @Test
    void anExplicitPriceIsNotScaled() {
        givenPlainCustomer();
        // Whoever typed 20.00 was looking at "per case", so scaling it again
        // would bill 240 for a case. This is also the quotation-to-order
        // path, which carries the quoted unit and price across verbatim.
        givenProductPricedAt("2.00");
        givenSalesUom(CASE_UNIT_ID, "12");

        var response = createOrderWith(CASE_UNIT_ID, "1", new BigDecimal("20.00"));

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void aFractionalFactorKeepsSubCentPrecision() {
        givenPlainCustomer();
        // Half-litre bottles from a per-litre price: rounding to 2dp would be
        // fine here, but scale 4 is what stops a per-gram price vanishing.
        givenProductPricedAt("3.33");
        givenSalesUom(CASE_UNIT_ID, "0.5");

        var response = createOrderWith(CASE_UNIT_ID, "2", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("1.6650");
    }

    private static final long PRICED_PRODUCT_ID = 77L;
    private static final long BASE_UNIT_ID = 5L;
    private static final long CASE_UNIT_ID = 6L;

    private void givenProductPricedAt(String sellingPrice) {
        Product product = Product.builder()
                .id(PRICED_PRODUCT_ID)
                .companyId(1L)
                .name("Bottled water")
                .unitOfMeasureId(BASE_UNIT_ID)
                .sellingPrice(new BigDecimal(sellingPrice))
                .taxRate(BigDecimal.ZERO)
                .build();
        when(productRepository.findAllById(any())).thenReturn(List.of(product));
    }

    private void givenSalesUom(long unitId, String conversionFactor) {
        ProductUom uom = ProductUom.builder()
                .productId(PRICED_PRODUCT_ID)
                .unitOfMeasureId(unitId)
                .conversionFactor(new BigDecimal(conversionFactor))
                .allowSales(true)
                .build();
        when(productUomRepository.findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(PRICED_PRODUCT_ID, unitId))
                .thenReturn(Optional.of(uom));
    }

    private com.example.erp.dto.SalesOrderResponse createOrderWith(Long unitOfMeasureId, String quantity, BigDecimal explicitPrice) {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(com.example.erp.entity.Company.builder().id(1L).build()));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(Warehouse.builder().id(2L).companyId(1L).build()));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder so = invocation.getArgument(0);
            if (so.getId() == null) so.setId(100L);
            return so;
        });
        when(lineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SalesOrderLineRequest line = new SalesOrderLineRequest();
        line.setProductId(PRICED_PRODUCT_ID);
        line.setUnitOfMeasureId(unitOfMeasureId);
        line.setQuantityOrdered(new BigDecimal(quantity));
        line.setUnitPrice(explicitPrice);

        CreateSalesOrderRequest request = new CreateSalesOrderRequest();
        request.setCompanyId(1L);
        request.setCustomerId(5L);
        request.setWarehouseId(2L);
        request.setOrderDate(LocalDate.now());
        request.setLines(List.of(line));

        return salesOrderService.createSalesOrder(request, "tester");
    }

    @Test
    void aPerUnitOverrideIsUsedAsIsNotScaled() {
        // The whole point of per-unit pricing: a case priced at 20 is cheaper
        // than twelve bottles at 2. Multiplying it by the factor would bill
        // 240 and undo the bulk discount someone deliberately entered.
        givenProductPricedAt("2.00");
        givenSalesUom(CASE_UNIT_ID, "12");
        givenCustomerInPriceGroup();
        when(productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureId(PRICED_PRODUCT_ID, PRICE_GROUP_ID, CASE_UNIT_ID))
                .thenReturn(Optional.of(ProductPrice.builder()
                        .productId(PRICED_PRODUCT_ID).priceGroupId(PRICE_GROUP_ID)
                        .unitOfMeasureId(CASE_UNIT_ID).price(new BigDecimal("20.00")).build()));

        var response = createOrderWith(CASE_UNIT_ID, "1", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void withoutAPerUnitOverrideTheBasePriceIsStillScaled() {
        // No row for the case, so the cascade falls back to the base price
        // and multiplies — a case costs twelve bottles' worth.
        givenProductPricedAt("2.00");
        givenSalesUom(CASE_UNIT_ID, "12");
        givenCustomerInPriceGroup();
        when(productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureId(PRICED_PRODUCT_ID, PRICE_GROUP_ID, CASE_UNIT_ID))
                .thenReturn(Optional.empty());

        var response = createOrderWith(CASE_UNIT_ID, "1", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("24.0000");
    }

    @Test
    void aBaseUnitOverrideIsScaledForALargerUnit() {
        // A base-unit override is per bottle, so it behaves like any other
        // base figure: the case is twelve of them.
        givenProductPricedAt("2.00");
        givenSalesUom(CASE_UNIT_ID, "12");
        givenCustomerInPriceGroup();
        when(productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureId(PRICED_PRODUCT_ID, PRICE_GROUP_ID, CASE_UNIT_ID))
                .thenReturn(Optional.empty());
        when(productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(PRICED_PRODUCT_ID, PRICE_GROUP_ID))
                .thenReturn(Optional.of(ProductPrice.builder()
                        .productId(PRICED_PRODUCT_ID).priceGroupId(PRICE_GROUP_ID)
                        .price(new BigDecimal("1.50")).build()));

        var response = createOrderWith(CASE_UNIT_ID, "1", null);

        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("18.0000");
    }

    @Test
    void aBaseUnitLineNeverConsultsThePerUnitTable() {
        // Guards the normalisation rule: the base price lives in the
        // unit-is-null row, so a base-unit line must not look for a row keyed
        // on the base unit id — there is never one.
        givenProductPricedAt("2.00");
        givenCustomerInPriceGroup();
        when(productPriceRepository.findByProductIdAndPriceGroupIdAndUnitOfMeasureIdIsNull(PRICED_PRODUCT_ID, PRICE_GROUP_ID))
                .thenReturn(Optional.empty());

        createOrderWith(null, "3", null);

        verify(productPriceRepository, never())
                .findByProductIdAndPriceGroupIdAndUnitOfMeasureId(eq(PRICED_PRODUCT_ID), eq(PRICE_GROUP_ID), any());
    }

    private static final long PRICE_GROUP_ID = 41L;

    /** A customer with no group, so the cascade falls to the product's own price. */
    private void givenPlainCustomer() {
        when(customerRepository.findById(5L)).thenReturn(Optional.of(
                com.example.erp.entity.Customer.builder().id(5L).companyId(1L).build()));
    }

    private void givenCustomerInPriceGroup() {
        when(customerRepository.findById(5L)).thenReturn(Optional.of(
                com.example.erp.entity.Customer.builder().id(5L).companyId(1L).customerGroupId(31L).build()));
        when(customerGroupRepository.findById(31L)).thenReturn(Optional.of(
                com.example.erp.entity.CustomerGroup.builder().id(31L).priceGroupId(PRICE_GROUP_ID).build()));
        lenient().when(priceGroupRepository.findById(PRICE_GROUP_ID)).thenReturn(Optional.empty());
    }
}
