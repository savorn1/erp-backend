package com.example.erp.service.impl;

import com.example.erp.dto.ResolveRmaRequest;
import com.example.erp.entity.RmaLine;
import com.example.erp.entity.RmaRequest;
import com.example.erp.entity.RmaResolutionType;
import com.example.erp.entity.RmaStatus;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.SerialNumberStatus;
import com.example.erp.entity.StockLevel;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.RmaLineRepository;
import com.example.erp.repository.RmaRequestRepository;
import com.example.erp.repository.SerialNumberRepository;
import com.example.erp.repository.StockLevelRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.CreditNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers what resolving an RMA does to a serial-tracked unit.
 *
 * <p>The interesting invariant is that serial status has to agree with the
 * stock movement: a resolution that calls {@code increaseStock} has physically
 * put the unit back on the shelf, so the serial must leave {@code ISSUED} too.
 * When it didn't, {@code StockLevel} claimed the unit was on hand while every
 * outbound path (delivery, transfer and adjustment all require
 * {@code IN_STOCK}) refused to move it — the unit became unsellable forever.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RmaServiceImplTest {

    @Mock private RmaRequestRepository rmaRequestRepository;
    @Mock private RmaLineRepository rmaLineRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceLineRepository invoiceLineRepository;
    @Mock private SerialNumberRepository serialNumberRepository;
    @Mock private StockLevelRepository stockLevelRepository;
    @Mock private CreditNoteService creditNoteService;

    @InjectMocks
    private RmaServiceImpl service;

    private static final long RMA_ID = 1L;
    private static final long INVOICE_ID = 20L;
    private static final long PRODUCT_ID = 50L;
    private static final long WAREHOUSE_ID = 7L;
    private static final long COMPANY_ID = 3L;
    private static final long SERIAL_ID = 900L;

    private SerialNumber serial;

    @BeforeEach
    void setUp() {
        RmaRequest rma = RmaRequest.builder()
                .companyId(COMPANY_ID)
                .warehouseId(WAREHOUSE_ID)
                .invoiceId(INVOICE_ID)
                .rmaNumber("RMA-000001")
                .status(RmaStatus.APPROVED)
                .build();
        rma.setId(RMA_ID);

        RmaLine line = RmaLine.builder()
                .rmaId(RMA_ID)
                .productId(PRODUCT_ID)
                .serialNumberId(SERIAL_ID)
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("100"))
                .build();
        line.setId(300L);

        serial = SerialNumber.builder()
                .companyId(COMPANY_ID)
                .productId(PRODUCT_ID)
                .warehouseId(WAREHOUSE_ID)
                .serialNumber("SN-ABC-123")
                .status(SerialNumberStatus.ISSUED)
                .build();
        serial.setId(SERIAL_ID);

        when(rmaRequestRepository.findById(RMA_ID)).thenReturn(Optional.of(rma));
        when(rmaLineRepository.findByRmaId(RMA_ID)).thenReturn(List.of(line));
        when(serialNumberRepository.findById(SERIAL_ID)).thenReturn(Optional.of(serial));
        when(stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(StockLevel.builder()
                        .companyId(COMPANY_ID)
                        .productId(PRODUCT_ID)
                        .warehouseId(WAREHOUSE_ID)
                        .quantityOnHand(BigDecimal.ZERO)
                        .build()));
    }

    @Test
    void refundingReturnsTheUnitToStockSoItCanBeSoldAgain() {
        service.resolveRma(RMA_ID, resolution(RmaResolutionType.REFUND), "tester");

        assertThat(serial.getStatus()).isEqualTo(SerialNumberStatus.IN_STOCK);
        verify(serialNumberRepository).save(serial);
    }

    @Test
    void replacementAlsoReturnsTheFaultyUnitToStock() {
        // Net stock change is zero (one in, one out), but the returned unit is
        // physically back — its serial has to reflect that.
        service.resolveRma(RMA_ID, resolution(RmaResolutionType.REPLACEMENT), "tester");

        assertThat(serial.getStatus()).isEqualTo(SerialNumberStatus.IN_STOCK);
    }

    @Test
    void repairLeavesTheUnitIssued() {
        // A repair moves no stock — the same unit goes back to the same
        // customer, so it is still out in the field.
        service.resolveRma(RMA_ID, resolution(RmaResolutionType.REPAIR), "tester");

        assertThat(serial.getStatus()).isEqualTo(SerialNumberStatus.ISSUED);
        verify(serialNumberRepository, never()).save(serial);
    }

    @Test
    void aUnitAlreadyWrittenOffIsNotRevived() {
        // If the unit was adjusted out while the RMA sat open, resolving the
        // RMA must not quietly put a written-off unit back on the shelf.
        serial.setStatus(SerialNumberStatus.ADJUSTED_OUT);

        service.resolveRma(RMA_ID, resolution(RmaResolutionType.REFUND), "tester");

        assertThat(serial.getStatus()).isEqualTo(SerialNumberStatus.ADJUSTED_OUT);
        verify(serialNumberRepository, never()).save(serial);
    }

    @Test
    void aLineWithNoSerialIsIgnored() {
        // Non-serial-tracked products make up most RMA lines; they must not
        // trip the lookup.
        RmaLine plainLine = RmaLine.builder()
                .rmaId(RMA_ID)
                .productId(PRODUCT_ID)
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("100"))
                .build();
        plainLine.setId(301L);
        when(rmaLineRepository.findByRmaId(RMA_ID)).thenReturn(List.of(plainLine));

        service.resolveRma(RMA_ID, resolution(RmaResolutionType.REFUND), "tester");

        verify(serialNumberRepository, never()).findById(SERIAL_ID);
    }

    private ResolveRmaRequest resolution(RmaResolutionType type) {
        ResolveRmaRequest request = new ResolveRmaRequest();
        request.setResolutionType(type);
        return request;
    }
}
