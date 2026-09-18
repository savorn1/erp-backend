package com.example.erp.service.impl;

import com.example.erp.dto.CommissionEntryFilterRequest;
import com.example.erp.dto.CommissionEntryResponse;
import com.example.erp.dto.CommissionReportFilterRequest;
import com.example.erp.dto.CommissionReportResponse;
import com.example.erp.dto.CommissionReportRowResponse;
import com.example.erp.dto.MarkCommissionPaidRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.CommissionEntry;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.Payment;
import com.example.erp.entity.User;
import com.example.erp.repository.CommissionEntryRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.repository.PaymentRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CommissionService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public CommissionReportResponse report(CommissionReportFilterRequest filter) {
        List<CommissionEntry> entries = entryRepository.findAll(buildSpec(
                filter.getCompanyId(), filter.getSalesRepUserId(), filter.getDateFrom(), filter.getDateTo(), null));

        Map<Long, List<CommissionEntry>> bySalesRep = entries.stream()
                .collect(Collectors.groupingBy(CommissionEntry::getSalesRepUserId));
        Map<Long, String> userNames = userRepository.findAllById(bySalesRep.keySet()).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<CommissionReportRowResponse> rows = new ArrayList<>();
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (Map.Entry<Long, List<CommissionEntry>> group : bySalesRep.entrySet()) {
            BigDecimal basis = BigDecimal.ZERO;
            BigDecimal commission = BigDecimal.ZERO;
            BigDecimal paidCommission = BigDecimal.ZERO;
            for (CommissionEntry entry : group.getValue()) {
                basis = basis.add(entry.getBasisAmount());
                commission = commission.add(entry.getCommissionAmount());
                if (entry.isPaidOut()) {
                    paidCommission = paidCommission.add(entry.getCommissionAmount());
                }
            }
            rows.add(CommissionReportRowResponse.builder()
                    .salesRepUserId(group.getKey())
                    .salesRepName(userNames.get(group.getKey()))
                    .entryCount(group.getValue().size())
                    .totalBasisAmount(basis)
                    .totalCommissionAmount(commission)
                    .paidCommissionAmount(paidCommission)
                    .unpaidCommissionAmount(commission.subtract(paidCommission))
                    .build());
            totalCommission = totalCommission.add(commission);
        }
        rows.sort(Comparator.comparing(CommissionReportRowResponse::getTotalCommissionAmount).reversed());

        return CommissionReportResponse.builder()
                .dateFrom(filter.getDateFrom())
                .dateTo(filter.getDateTo())
                .rows(rows)
                .totalCommissionAmount(totalCommission)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommissionEntryResponse> listEntries(CommissionEntryFilterRequest filter) {
        Specification<CommissionEntry> spec = buildSpec(
                filter.getCompanyId(), filter.getSalesRepUserId(), filter.getDateFrom(), filter.getDateTo(), filter.getPaidOut());
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CommissionEntry> page = entryRepository.findAll(spec, pageable);
        List<CommissionEntry> content = page.getContent();

        Map<Long, String> userNames = userRepository.findAllById(
                content.stream().map(CommissionEntry::getSalesRepUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> invoiceNumbers = invoiceRepository.findAllById(
                content.stream().map(CommissionEntry::getInvoiceId).distinct().toList()
        ).stream().collect(Collectors.toMap(Invoice::getId, Invoice::getInvoiceNumber));
        Map<Long, String> paymentNumbers = paymentRepository.findAllById(
                content.stream().map(CommissionEntry::getPaymentId).distinct().toList()
        ).stream().collect(Collectors.toMap(Payment::getId, Payment::getPaymentNumber));

        return PageResponse.of(page.map(entry -> CommissionEntryResponse.builder()
                .id(entry.getId())
                .salesRepUserId(entry.getSalesRepUserId())
                .salesRepName(userNames.get(entry.getSalesRepUserId()))
                .invoiceId(entry.getInvoiceId())
                .invoiceNumber(invoiceNumbers.get(entry.getInvoiceId()))
                .paymentId(entry.getPaymentId())
                .paymentNumber(paymentNumbers.get(entry.getPaymentId()))
                .basisAmount(entry.getBasisAmount())
                .ratePercent(entry.getRatePercent())
                .commissionAmount(entry.getCommissionAmount())
                .earnedDate(entry.getEarnedDate())
                .paidOut(entry.isPaidOut())
                .paidOutAt(entry.getPaidOutAt())
                .build()));
    }

    @Override
    @Transactional
    public void markPaid(MarkCommissionPaidRequest request) {
        List<CommissionEntry> entries = entryRepository.findBySalesRepUserIdAndPaidOutFalseAndEarnedDateLessThanEqual(
                request.getSalesRepUserId(), request.getThroughDate());
        LocalDateTime now = LocalDateTime.now();
        for (CommissionEntry entry : entries) {
            entry.setPaidOut(true);
            entry.setPaidOutAt(now);
        }
        entryRepository.saveAll(entries);
    }

    private Specification<CommissionEntry> buildSpec(Long companyId, Long salesRepUserId,
                                                       java.time.LocalDate dateFrom, java.time.LocalDate dateTo, Boolean paidOut) {
        List<Specification<CommissionEntry>> conditions = new ArrayList<>();
        if (companyId != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), companyId));
        }
        if (salesRepUserId != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("salesRepUserId"), salesRepUserId));
        }
        if (dateFrom != null) {
            conditions.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("earnedDate"), dateFrom));
        }
        if (dateTo != null) {
            conditions.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("earnedDate"), dateTo));
        }
        if (paidOut != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("paidOut"), paidOut));
        }
        return Specification.allOf(conditions);
    }
}
