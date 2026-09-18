package com.example.erp.service.impl;

import com.example.erp.dto.InvoiceAgingFilterRequest;
import com.example.erp.dto.InvoiceAgingReportResponse;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.DeliveryLineRepository;
import com.example.erp.repository.DeliveryRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.SalesOrderLineRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.CustomerService;
import com.example.erp.service.EmailService;
import com.example.erp.service.PdfRenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Regression guard for the InvoiceServiceImpl.agingReport N+1 fix — asserts
// the batched (findByInvoiceIdIn) rewrite produces the same totals the old
// per-invoice-id loop would have, not just "fewer queries."
@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceLineRepository invoiceLineRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DeliveryLineRepository deliveryLineRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private PaymentAllocationRepository paymentAllocationRepository;
    @Mock
    private AutoPostingService autoPostingService;
    @Mock
    private PdfRenderService pdfRenderService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void agingReport_computesOutstandingTotalsAcrossBatchedLookups() {
        Invoice invoiceA = Invoice.builder().id(1L).companyId(1L).customerId(10L).status(InvoiceStatus.APPROVED).build();
        Invoice invoiceB = Invoice.builder().id(2L).companyId(1L).customerId(20L).status(InvoiceStatus.APPROVED).build();
        when(invoiceRepository.findAll(any(Specification.class))).thenReturn(List.of(invoiceA, invoiceB));

        InvoiceLine lineA = InvoiceLine.builder().id(100L).invoiceId(1L).productId(1L)
                .quantity(BigDecimal.TEN).unitPrice(BigDecimal.TEN).build();
        InvoiceLine lineB = InvoiceLine.builder().id(200L).invoiceId(2L).productId(1L)
                .quantity(BigDecimal.valueOf(5)).unitPrice(BigDecimal.TEN).build();
        when(invoiceLineRepository.findByInvoiceIdIn(List.of(1L, 2L))).thenReturn(List.of(lineA, lineB));

        CreditNote creditNote = CreditNote.builder().id(1000L).invoiceId(1L).amount(BigDecimal.valueOf(20)).build();
        when(creditNoteRepository.findByInvoiceIdIn(List.of(1L, 2L))).thenReturn(List.of(creditNote));

        PaymentAllocation allocation = PaymentAllocation.builder().id(2000L).invoiceId(2L).amount(BigDecimal.valueOf(10)).build();
        when(paymentAllocationRepository.findByInvoiceIdIn(List.of(1L, 2L))).thenReturn(List.of(allocation));

        when(customerRepository.findById(10L)).thenReturn(Optional.of(Customer.builder().id(10L).name("Customer A").build()));
        when(customerRepository.findById(20L)).thenReturn(Optional.of(Customer.builder().id(20L).name("Customer B").build()));

        InvoiceAgingReportResponse response = invoiceService.agingReport(new InvoiceAgingFilterRequest());

        // Invoice A: 10*10=100 total, 20 credited -> 80 outstanding.
        // Invoice B: 5*10=50 total, 10 paid -> 40 outstanding.
        assertThat(response.getTotals().getTotal()).isEqualByComparingTo("120");
        assertThat(response.getRows()).hasSize(2);
        assertThat(response.getRows().stream().filter(r -> r.getCustomerId().equals(10L)).findFirst().orElseThrow().getTotal())
                .isEqualByComparingTo("80");
        assertThat(response.getRows().stream().filter(r -> r.getCustomerId().equals(20L)).findFirst().orElseThrow().getTotal())
                .isEqualByComparingTo("40");
    }
}
