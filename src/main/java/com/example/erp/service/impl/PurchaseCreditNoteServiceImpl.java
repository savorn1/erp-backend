package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreatePurchaseCreditNoteRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseCreditNoteFilterRequest;
import com.example.erp.dto.PurchaseCreditNoteResponse;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.PurchaseInvoiceLine;
import com.example.erp.entity.PurchaseInvoiceStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PurchaseCreditNoteRepository;
import com.example.erp.repository.PurchaseInvoiceLineRepository;
import com.example.erp.repository.PurchaseInvoiceRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PurchaseCreditNoteService;
import com.example.erp.service.SupplierService;
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
public class PurchaseCreditNoteServiceImpl implements PurchaseCreditNoteService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PurchaseCreditNoteRepository purchaseCreditNoteRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final AutoPostingService autoPostingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseCreditNoteResponse> listPurchaseCreditNotes(PurchaseCreditNoteFilterRequest filter) {
        List<Specification<PurchaseCreditNote>> conditions = new ArrayList<>();
        if (filter.getCreditNoteNumber() != null && !filter.getCreditNoteNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("creditNoteNumber")), "%" + filter.getCreditNoteNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getPurchaseInvoiceId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("purchaseInvoiceId"), filter.getPurchaseInvoiceId()));
        }
        if (filter.getSupplierId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("supplierId"), filter.getSupplierId()));
        }
        Specification<PurchaseCreditNote> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PurchaseCreditNote> page = purchaseCreditNoteRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public PurchaseCreditNoteResponse getPurchaseCreditNote(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PurchaseCreditNoteResponse createPurchaseCreditNote(Long purchaseInvoiceId, CreatePurchaseCreditNoteRequest request, String actingUsername) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(purchaseInvoiceId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Purchase invoice not found with id: " + purchaseInvoiceId));
        if (invoice.getStatus() != PurchaseInvoiceStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only approved purchase invoices can receive a credit note");
        }

        List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(purchaseInvoiceId);
        BigDecimal totalAmount = computeTotal(lines);
        BigDecimal alreadyCredited = purchaseCreditNoteRepository.findByPurchaseInvoiceId(purchaseInvoiceId).stream()
                .map(PurchaseCreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingCreditable = totalAmount.subtract(alreadyCredited);
        if (request.getAmount().compareTo(remainingCreditable) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Credit amount exceeds what remains creditable on this invoice (" + remainingCreditable + ")");
        }

        PurchaseCreditNote creditNote = PurchaseCreditNote.builder()
                .companyId(invoice.getCompanyId())
                .purchaseInvoiceId(invoice.getId())
                .supplierId(invoice.getSupplierId())
                .creditNoteDate(request.getCreditNoteDate())
                .reason(request.getReason())
                .amount(request.getAmount())
                .createdBy(actingUsername)
                .build();
        purchaseCreditNoteRepository.save(creditNote);
        creditNote.setCreditNoteNumber("PCN-" + String.format("%06d", creditNote.getId()));
        purchaseCreditNoteRepository.save(creditNote);

        BalanceAdjustmentRequest paymentRequest = new BalanceAdjustmentRequest();
        paymentRequest.setType(BalanceAdjustmentType.PAYMENT);
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setNote("Purchase credit note " + creditNote.getCreditNoteNumber() + " for invoice " + invoice.getInvoiceNumber());
        supplierService.adjustBalance(invoice.getSupplierId(), paymentRequest, actingUsername);
        autoPostingService.postPurchaseCreditNote(creditNote, actingUsername);

        return toResponse(creditNote);
    }

    private BigDecimal computeTotal(List<PurchaseInvoiceLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitCost());
            BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
            BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            total = total.add(afterDiscount).add(taxAmount);
        }
        return total;
    }

    private PurchaseCreditNote find(Long id) {
        return purchaseCreditNoteRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase credit note not found with id: " + id));
    }

    private PurchaseCreditNoteResponse toResponse(PurchaseCreditNote creditNote) {
        String invoiceNumber = purchaseInvoiceRepository.findById(creditNote.getPurchaseInvoiceId()).map(PurchaseInvoice::getInvoiceNumber).orElse(null);
        String supplierName = supplierRepository.findById(creditNote.getSupplierId()).map(Supplier::getName).orElse(null);
        return PurchaseCreditNoteResponse.builder()
                .id(creditNote.getId())
                .companyId(creditNote.getCompanyId())
                .purchaseInvoiceId(creditNote.getPurchaseInvoiceId())
                .invoiceNumber(invoiceNumber)
                .supplierId(creditNote.getSupplierId())
                .supplierName(supplierName)
                .creditNoteNumber(creditNote.getCreditNoteNumber())
                .creditNoteDate(creditNote.getCreditNoteDate())
                .reason(creditNote.getReason())
                .amount(creditNote.getAmount())
                .createdBy(creditNote.getCreatedBy())
                .build();
    }
}
