package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreateCreditNoteRequest;
import com.example.erp.dto.CreditNoteFilterRequest;
import com.example.erp.dto.CreditNoteResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CreditNoteRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceLineRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.service.CreditNoteService;
import com.example.erp.service.CustomerService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditNoteServiceImpl implements CreditNoteService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CreditNoteResponse> listCreditNotes(CreditNoteFilterRequest filter) {
        List<Specification<CreditNote>> conditions = new ArrayList<>();
        if (filter.getCreditNoteNumber() != null && !filter.getCreditNoteNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("creditNoteNumber")), "%" + filter.getCreditNoteNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getInvoiceId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("invoiceId"), filter.getInvoiceId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        Specification<CreditNote> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CreditNote> page = creditNoteRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public CreditNoteResponse getCreditNote(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public CreditNoteResponse createCreditNote(Long invoiceId, CreateCreditNoteRequest request, String actingUsername) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Invoice not found with id: " + invoiceId));
        if (invoice.getStatus() != InvoiceStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only approved invoices can receive a credit note");
        }

        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(invoiceId);
        BigDecimal totalAmount = computeTotal(lines);
        BigDecimal alreadyCredited = creditNoteRepository.findByInvoiceId(invoiceId).stream()
                .map(CreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingCreditable = totalAmount.subtract(alreadyCredited);
        if (request.getAmount().compareTo(remainingCreditable) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Credit amount exceeds what remains creditable on this invoice (" + remainingCreditable + ")");
        }

        CreditNote creditNote = CreditNote.builder()
                .companyId(invoice.getCompanyId())
                .invoiceId(invoice.getId())
                .customerId(invoice.getCustomerId())
                .creditNoteDate(request.getCreditNoteDate())
                .reason(request.getReason())
                .amount(request.getAmount())
                .createdBy(actingUsername)
                .build();
        creditNoteRepository.save(creditNote);
        creditNote.setCreditNoteNumber("CN-" + String.format("%06d", creditNote.getId()));
        creditNoteRepository.save(creditNote);

        BalanceAdjustmentRequest paymentRequest = new BalanceAdjustmentRequest();
        paymentRequest.setType(BalanceAdjustmentType.PAYMENT);
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setNote("Credit note " + creditNote.getCreditNoteNumber() + " for invoice " + invoice.getInvoiceNumber());
        customerService.adjustBalance(invoice.getCustomerId(), paymentRequest, actingUsername);

        return toResponse(creditNote);
    }

    private BigDecimal computeTotal(List<InvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice());
            BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
            BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            total = total.add(afterDiscount).add(taxAmount);
        }
        return total;
    }

    private CreditNote find(Long id) {
        return creditNoteRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Credit note not found with id: " + id));
    }

    private CreditNoteResponse toResponse(CreditNote creditNote) {
        String invoiceNumber = invoiceRepository.findById(creditNote.getInvoiceId()).map(Invoice::getInvoiceNumber).orElse(null);
        String customerName = customerRepository.findById(creditNote.getCustomerId()).map(Customer::getName).orElse(null);
        return CreditNoteResponse.builder()
                .id(creditNote.getId())
                .companyId(creditNote.getCompanyId())
                .invoiceId(creditNote.getInvoiceId())
                .invoiceNumber(invoiceNumber)
                .customerId(creditNote.getCustomerId())
                .customerName(customerName)
                .creditNoteNumber(creditNote.getCreditNoteNumber())
                .creditNoteDate(creditNote.getCreditNoteDate())
                .reason(creditNote.getReason())
                .amount(creditNote.getAmount())
                .createdBy(creditNote.getCreatedBy())
                .build();
    }
}
