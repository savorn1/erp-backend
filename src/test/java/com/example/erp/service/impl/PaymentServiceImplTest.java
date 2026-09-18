package com.example.erp.service.impl;

import com.example.erp.dto.CreatePaymentRequest;
import com.example.erp.dto.PaymentAllocationRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.CommissionRule;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.SalesOrder;
import com.example.erp.repository.CommissionEntryRepository;
import com.example.erp.repository.CommissionRuleRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentAllocationRepository;
import com.example.erp.repository.PaymentRepository;
import com.example.erp.repository.SalesOrderRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentAllocationRepository paymentAllocationRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceLineRepository invoiceLineRepository;
    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private AutoPostingService autoPostingService;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private CommissionRuleRepository commissionRuleRepository;
    @Mock
    private CommissionEntryRepository commissionEntryRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CreatePaymentRequest buildRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setCompanyId(1L);
        request.setCustomerId(5L);
        request.setPaymentDate(LocalDate.now());
        request.setMethod(PaymentMethod.BANK_TRANSFER);
        PaymentAllocationRequest allocation = new PaymentAllocationRequest();
        allocation.setInvoiceId(20L);
        allocation.setAmount(BigDecimal.valueOf(100));
        request.setAllocations(List.of(allocation));
        return request;
    }

    private void stubCommonFixtures(Long salesOrderId) {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(customerRepository.findById(5L)).thenReturn(Optional.of(Customer.builder().id(5L).companyId(1L).build()));
        Invoice invoice = Invoice.builder()
                .id(20L).companyId(1L).customerId(5L).salesOrderId(salesOrderId)
                .status(InvoiceStatus.APPROVED)
                .build();
        when(invoiceRepository.findById(20L)).thenReturn(Optional.of(invoice));
        InvoiceLine line = InvoiceLine.builder()
                .id(1L).invoiceId(20L).productId(1L)
                .quantity(BigDecimal.ONE).unitPrice(BigDecimal.valueOf(100))
                .build();
        when(invoiceLineRepository.findByInvoiceId(20L)).thenReturn(List.of(line));
        when(creditNoteRepository.findByInvoiceId(20L)).thenReturn(List.of());
        when(paymentAllocationRepository.findByInvoiceId(20L)).thenReturn(List.of());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) payment.setId(500L);
            return payment;
        });
        when(paymentAllocationRepository.save(any(PaymentAllocation.class))).thenAnswer(invocation -> {
            PaymentAllocation allocation = invocation.getArgument(0);
            if (allocation.getId() == null) allocation.setId(900L);
            return allocation;
        });
        lenient().when(paymentAllocationRepository.findByPaymentId(any())).thenReturn(List.of());
        lenient().when(paymentRepository.findByRelatedPaymentId(any())).thenReturn(List.of());
    }

    @Test
    void recordPayment_createsCommissionEntry_whenSalesRepAndRuleResolve() {
        stubCommonFixtures(30L);
        when(salesOrderRepository.findById(30L)).thenReturn(Optional.of(
                SalesOrder.builder().id(30L).companyId(1L).salesRepUserId(42L).build()));
        when(commissionRuleRepository.findByCompanyIdAndUserIdAndActiveTrue(1L, 42L)).thenReturn(
                Optional.of(CommissionRule.builder().id(1L).companyId(1L).userId(42L).ratePercent(BigDecimal.TEN).active(true).build()));

        paymentService.recordPayment(buildRequest(), "tester");

        ArgumentCaptor<com.example.erp.entity.CommissionEntry> captor = ArgumentCaptor.forClass(com.example.erp.entity.CommissionEntry.class);
        verify(commissionEntryRepository).save(captor.capture());
        com.example.erp.entity.CommissionEntry entry = captor.getValue();
        assertThat(entry.getSalesRepUserId()).isEqualTo(42L);
        assertThat(entry.getBasisAmount()).isEqualByComparingTo("100");
        assertThat(entry.getRatePercent()).isEqualByComparingTo("10");
        assertThat(entry.getCommissionAmount()).isEqualByComparingTo("10.0000");
    }

    @Test
    void recordPayment_skipsCommission_whenSalesOrderHasNoSalesRep() {
        stubCommonFixtures(30L);
        when(salesOrderRepository.findById(30L)).thenReturn(Optional.of(
                SalesOrder.builder().id(30L).companyId(1L).salesRepUserId(null).build()));

        paymentService.recordPayment(buildRequest(), "tester");

        verify(commissionEntryRepository, never()).save(any());
    }

    @Test
    void recordPayment_skipsCommission_whenNoCommissionRuleResolves() {
        stubCommonFixtures(30L);
        when(salesOrderRepository.findById(30L)).thenReturn(Optional.of(
                SalesOrder.builder().id(30L).companyId(1L).salesRepUserId(42L).build()));
        when(commissionRuleRepository.findByCompanyIdAndUserIdAndActiveTrue(1L, 42L)).thenReturn(Optional.empty());
        when(commissionRuleRepository.findByCompanyIdAndUserIdIsNullAndActiveTrue(1L)).thenReturn(Optional.empty());

        paymentService.recordPayment(buildRequest(), "tester");

        verify(commissionEntryRepository, never()).save(any());
    }

    @Test
    void recordPayment_skipsCommission_whenInvoiceHasNoSalesOrder() {
        stubCommonFixtures(null);

        paymentService.recordPayment(buildRequest(), "tester");

        verify(commissionEntryRepository, never()).save(any());
    }

    @Test
    void recordPayment_usesCompanyDefaultRule_whenRepHasNoOwnRule() {
        stubCommonFixtures(30L);
        when(salesOrderRepository.findById(30L)).thenReturn(Optional.of(
                SalesOrder.builder().id(30L).companyId(1L).salesRepUserId(42L).build()));
        when(commissionRuleRepository.findByCompanyIdAndUserIdAndActiveTrue(1L, 42L)).thenReturn(Optional.empty());
        when(commissionRuleRepository.findByCompanyIdAndUserIdIsNullAndActiveTrue(1L)).thenReturn(
                Optional.of(CommissionRule.builder().id(2L).companyId(1L).userId(null).ratePercent(BigDecimal.valueOf(5)).active(true).build()));

        paymentService.recordPayment(buildRequest(), "tester");

        ArgumentCaptor<com.example.erp.entity.CommissionEntry> captor = ArgumentCaptor.forClass(com.example.erp.entity.CommissionEntry.class);
        verify(commissionEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getRatePercent()).isEqualByComparingTo("5");
        assertThat(captor.getValue().getCommissionAmount()).isEqualByComparingTo("5.0000");
    }
}
