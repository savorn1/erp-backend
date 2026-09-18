package com.example.erp.service.impl;

import com.example.erp.dto.BalanceAdjustmentRequest;
import com.example.erp.dto.CreatePaymentRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PaymentAllocationRequest;
import com.example.erp.dto.PaymentAllocationResponse;
import com.example.erp.dto.PaymentFilterRequest;
import com.example.erp.dto.PaymentResponse;
import com.example.erp.dto.RefundPaymentRequest;
import com.example.erp.entity.BalanceAdjustmentType;
import com.example.erp.entity.CommissionEntry;
import com.example.erp.entity.CommissionRule;
import com.example.erp.entity.Company;
import com.example.erp.entity.CreditNote;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PaymentAllocation;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.SalesOrder;
import com.example.erp.exception.AppException;
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
import com.example.erp.service.PaymentService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final AutoPostingService autoPostingService;
    private final SalesOrderRepository salesOrderRepository;
    private final CommissionRuleRepository commissionRuleRepository;
    private final CommissionEntryRepository commissionEntryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(PaymentFilterRequest filter) {
        List<Specification<Payment>> conditions = new ArrayList<>();
        if (filter.getPaymentNumber() != null && !filter.getPaymentNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("paymentNumber")), "%" + filter.getPaymentNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getMethod() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("method"), filter.getMethod()));
        }
        if (filter.getType() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filter.getType()));
        }
        Specification<Payment> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Payment> page = paymentRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public PaymentResponse getPayment(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(CreatePaymentRequest request, String actingUsername) {
        Company company = requireCompany(request.getCompanyId());
        Customer customer = requireCustomer(request.getCustomerId(), request.getCompanyId());

        Map<Long, Invoice> invoices = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentAllocationRequest allocationRequest : request.getAllocations()) {
            Invoice invoice = invoiceRepository.findById(allocationRequest.getInvoiceId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Invoice not found with id: " + allocationRequest.getInvoiceId()));
            if (!invoice.getCustomerId().equals(request.getCustomerId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Invoice " + invoice.getInvoiceNumber() + " does not belong to the selected customer");
            }
            if (invoice.getStatus() != InvoiceStatus.APPROVED) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Only approved invoices can receive payment (" + invoice.getInvoiceNumber() + ")");
            }
            BigDecimal outstanding = outstandingAmountOf(invoice);
            if (allocationRequest.getAmount().compareTo(outstanding) > 0) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Allocation exceeds outstanding balance (" + outstanding + ") on invoice " + invoice.getInvoiceNumber());
            }
            invoices.put(invoice.getId(), invoice);
            total = total.add(allocationRequest.getAmount());
        }

        Payment payment = Payment.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .paymentDate(request.getPaymentDate())
                .type(PaymentType.PAYMENT)
                .method(request.getMethod())
                .amount(total)
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        paymentRepository.save(payment);
        payment.setPaymentNumber("PAY-" + String.format("%06d", payment.getId()));
        paymentRepository.save(payment);

        List<PaymentAllocation> allocations = new ArrayList<>();
        for (PaymentAllocationRequest allocationRequest : request.getAllocations()) {
            PaymentAllocation allocation = paymentAllocationRepository.save(PaymentAllocation.builder()
                    .paymentId(payment.getId())
                    .invoiceId(allocationRequest.getInvoiceId())
                    .amount(allocationRequest.getAmount())
                    .build());
            allocations.add(allocation);
        }
        recordCommissionEntries(payment, allocations);

        BalanceAdjustmentRequest adjustment = new BalanceAdjustmentRequest();
        adjustment.setType(BalanceAdjustmentType.PAYMENT);
        adjustment.setAmount(total);
        adjustment.setNote("Payment " + payment.getPaymentNumber());
        customerService.adjustBalance(request.getCustomerId(), adjustment, actingUsername);
        autoPostingService.postCustomerPayment(payment, actingUsername);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long id, RefundPaymentRequest request, String actingUsername) {
        Payment original = find(id);
        if (original.getType() != PaymentType.PAYMENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a payment (not a refund) can be refunded");
        }
        BigDecimal alreadyRefunded = paymentRepository.findByRelatedPaymentId(id).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingRefundable = original.getAmount().subtract(alreadyRefunded);
        if (request.getAmount().compareTo(remainingRefundable) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Refund amount exceeds what remains refundable on this payment (" + remainingRefundable + ")");
        }

        Payment refund = Payment.builder()
                .companyId(original.getCompanyId())
                .customerId(original.getCustomerId())
                .paymentDate(request.getRefundDate())
                .type(PaymentType.REFUND)
                .method(original.getMethod())
                .amount(request.getAmount())
                .reference("Refund of " + original.getPaymentNumber())
                .notes(request.getReason())
                .relatedPaymentId(original.getId())
                .createdBy(actingUsername)
                .build();
        paymentRepository.save(refund);
        refund.setPaymentNumber("REF-" + String.format("%06d", refund.getId()));
        paymentRepository.save(refund);

        // Reverse the original allocations proportionally — a partial refund
        // gives each originally-credited invoice back a proportional share of
        // its outstanding reduction.
        List<PaymentAllocation> originalAllocations = paymentAllocationRepository.findByPaymentId(original.getId());
        BigDecimal refundRatio = request.getAmount().divide(original.getAmount(), 8, RoundingMode.HALF_UP);
        List<PaymentAllocation> reversingAllocations = new ArrayList<>();
        for (PaymentAllocation allocation : originalAllocations) {
            BigDecimal reversedAmount = allocation.getAmount().multiply(refundRatio).setScale(4, RoundingMode.HALF_UP);
            if (reversedAmount.compareTo(BigDecimal.ZERO) <= 0) continue;
            reversingAllocations.add(paymentAllocationRepository.save(PaymentAllocation.builder()
                    .paymentId(refund.getId())
                    .invoiceId(allocation.getInvoiceId())
                    .amount(reversedAmount.negate())
                    .build()));
        }
        recordCommissionEntries(refund, reversingAllocations);

        BalanceAdjustmentRequest adjustment = new BalanceAdjustmentRequest();
        adjustment.setType(BalanceAdjustmentType.CHARGE);
        adjustment.setAmount(request.getAmount());
        adjustment.setNote("Refund " + refund.getPaymentNumber() + " of payment " + original.getPaymentNumber());
        customerService.adjustBalance(original.getCustomerId(), adjustment, actingUsername);
        autoPostingService.postCustomerPayment(refund, actingUsername);

        return toResponse(refund);
    }

    // Commission accrues on the paid portion of an invoice, not the invoiced
    // amount — so this runs off PaymentAllocation rows, symmetrically for
    // both a payment's allocations and a refund's negative reversing ones
    // (a refund's negative basisAmount naturally yields a negative
    // commissionAmount, clawing back exactly what was earned on that
    // portion). Skips silently whenever the invoice isn't tied to a sales
    // order, that order has no salesRepUserId, or no commission rate
    // resolves for that rep/company — commission tracking is opt-in per
    // order, not universal.
    private void recordCommissionEntries(Payment payment, List<PaymentAllocation> allocations) {
        for (PaymentAllocation allocation : allocations) {
            Invoice invoice = invoiceRepository.findById(allocation.getInvoiceId()).orElse(null);
            if (invoice == null || invoice.getSalesOrderId() == null) continue;
            SalesOrder salesOrder = salesOrderRepository.findById(invoice.getSalesOrderId()).orElse(null);
            if (salesOrder == null || salesOrder.getSalesRepUserId() == null) continue;
            BigDecimal rate = resolveCommissionRate(invoice.getCompanyId(), salesOrder.getSalesRepUserId());
            if (rate == null) continue;

            BigDecimal commissionAmount = allocation.getAmount().multiply(rate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            commissionEntryRepository.save(CommissionEntry.builder()
                    .companyId(invoice.getCompanyId())
                    .salesRepUserId(salesOrder.getSalesRepUserId())
                    .invoiceId(invoice.getId())
                    .paymentId(payment.getId())
                    .paymentAllocationId(allocation.getId())
                    .basisAmount(allocation.getAmount())
                    .ratePercent(rate)
                    .commissionAmount(commissionAmount)
                    .earnedDate(payment.getPaymentDate())
                    .build());
        }
    }

    private BigDecimal resolveCommissionRate(Long companyId, Long salesRepUserId) {
        return commissionRuleRepository.findByCompanyIdAndUserIdAndActiveTrue(companyId, salesRepUserId)
                .map(CommissionRule::getRatePercent)
                .or(() -> commissionRuleRepository.findByCompanyIdAndUserIdIsNullAndActiveTrue(companyId).map(CommissionRule::getRatePercent))
                .orElse(null);
    }

    // Mirrors CreditNoteServiceImpl.computeTotal — the invoice's own
    // total minus everything already credited/paid against it.
    private BigDecimal outstandingAmountOf(Invoice invoice) {
        List<InvoiceLine> lines = invoiceLineRepository.findByInvoiceId(invoice.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLine line : lines) {
            BigDecimal lineSubtotal = line.getQuantity().multiply(line.getUnitPrice());
            BigDecimal discountAmount = lineSubtotal.multiply(line.getDiscountPercent()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal afterDiscount = lineSubtotal.subtract(discountAmount);
            BigDecimal taxAmount = afterDiscount.multiply(line.getTaxRate()).divide(HUNDRED, 4, RoundingMode.HALF_UP);
            total = total.add(afterDiscount).add(taxAmount);
        }
        BigDecimal credited = creditNoteRepository.findByInvoiceId(invoice.getId()).stream()
                .map(CreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = paymentAllocationRepository.findByInvoiceId(invoice.getId()).stream()
                .map(PaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.subtract(credited).subtract(paid);
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private Customer requireCustomer(Long customerId, Long companyId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company");
        }
        return customer;
    }

    private Payment find(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Payment not found with id: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        String companyName = companyRepository.findById(payment.getCompanyId()).map(Company::getName).orElse(null);
        String customerName = customerRepository.findById(payment.getCustomerId()).map(Customer::getName).orElse(null);
        String relatedPaymentNumber = payment.getRelatedPaymentId() == null ? null
                : paymentRepository.findById(payment.getRelatedPaymentId()).map(Payment::getPaymentNumber).orElse(null);

        List<PaymentAllocation> allocations = paymentAllocationRepository.findByPaymentId(payment.getId());
        Map<Long, String> invoiceNumbers = invoiceRepository.findAllById(
                allocations.stream().map(PaymentAllocation::getInvoiceId).distinct().toList()
        ).stream().collect(Collectors.toMap(Invoice::getId, Invoice::getInvoiceNumber));

        List<PaymentAllocationResponse> allocationResponses = allocations.stream()
                .map(a -> PaymentAllocationResponse.builder()
                        .id(a.getId())
                        .invoiceId(a.getInvoiceId())
                        .invoiceNumber(invoiceNumbers.get(a.getInvoiceId()))
                        .amount(a.getAmount())
                        .build())
                .toList();

        BigDecimal refundedAmount = payment.getType() == PaymentType.PAYMENT
                ? paymentRepository.findByRelatedPaymentId(payment.getId()).stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                : null;

        return PaymentResponse.builder()
                .id(payment.getId())
                .companyId(payment.getCompanyId())
                .companyName(companyName)
                .customerId(payment.getCustomerId())
                .customerName(customerName)
                .paymentNumber(payment.getPaymentNumber())
                .paymentDate(payment.getPaymentDate())
                .type(payment.getType().name())
                .method(payment.getMethod().name())
                .amount(payment.getAmount())
                .reference(payment.getReference())
                .notes(payment.getNotes())
                .createdBy(payment.getCreatedBy())
                .relatedPaymentId(payment.getRelatedPaymentId())
                .relatedPaymentNumber(relatedPaymentNumber)
                .refundedAmount(refundedAmount)
                .allocations(allocationResponses)
                .build();
    }
}
