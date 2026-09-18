package com.example.erp.service.impl;

import com.example.erp.entity.Quotation;
import com.example.erp.entity.QuotationStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.OpportunityActivityRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.QuotationLineRepository;
import com.example.erp.repository.QuotationRepository;
import com.example.erp.service.EmailService;
import com.example.erp.service.PdfRenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceImplTest {

    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private QuotationLineRepository quotationLineRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private OpportunityActivityRepository opportunityActivityRepository;
    @Mock
    private PdfRenderService pdfRenderService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    @Test
    void acceptQuotation_rejectsExpiredQuotation() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .companyId(1L)
                .status(QuotationStatus.SENT)
                .validUntil(LocalDate.now().minusDays(1))
                .build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));

        assertThatThrownBy(() -> quotationService.acceptQuotation(1L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void acceptQuotation_acceptsWhenNotExpired() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .companyId(1L)
                .status(QuotationStatus.SENT)
                .validUntil(LocalDate.now().plusDays(1))
                .build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));
        when(quotationLineRepository.findByQuotationId(1L)).thenReturn(List.of());
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(customerRepository.findById(any())).thenReturn(Optional.empty());

        var response = quotationService.acceptQuotation(1L);

        assertThat(response.getStatus()).isEqualTo(QuotationStatus.ACCEPTED.name());
    }

    @Test
    void acceptQuotation_withNoValidUntil_isNeverExpired() {
        Quotation quotation = Quotation.builder()
                .id(1L)
                .companyId(1L)
                .status(QuotationStatus.SENT)
                .validUntil(null)
                .build();
        when(quotationRepository.findById(1L)).thenReturn(Optional.of(quotation));
        when(quotationLineRepository.findByQuotationId(1L)).thenReturn(List.of());
        lenient().when(companyRepository.findById(any())).thenReturn(Optional.empty());
        lenient().when(customerRepository.findById(any())).thenReturn(Optional.empty());

        var response = quotationService.acceptQuotation(1L);

        assertThat(response.getStatus()).isEqualTo(QuotationStatus.ACCEPTED.name());
    }
}
