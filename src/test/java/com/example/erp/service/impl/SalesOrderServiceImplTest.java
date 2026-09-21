package com.example.erp.service.impl;

import com.example.erp.dto.CreateSalesOrderFromQuotationRequest;
import com.example.erp.entity.InventorySettings;
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
}
