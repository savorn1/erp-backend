package com.example.erp.service.impl;

import com.example.erp.entity.CreditNote;
import com.example.erp.entity.FixedAsset;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.JournalEntry;
import com.example.erp.entity.JournalEntryLine;
import com.example.erp.entity.JournalEntryStatus;
import com.example.erp.entity.Payment;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PaymentType;
import com.example.erp.entity.PostingRule;
import com.example.erp.entity.PurchaseCreditNote;
import com.example.erp.entity.PurchaseInvoice;
import com.example.erp.entity.SupplierPayment;
import com.example.erp.repository.JournalEntryLineRepository;
import com.example.erp.repository.JournalEntryRepository;
import com.example.erp.repository.PostingRuleRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PeriodLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoPostingServiceImpl implements AutoPostingService {

    private final PostingRuleRepository postingRuleRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final PeriodLockService periodLockService;

    private record Line(Long accountId, BigDecimal debit, BigDecimal credit) {}

    @Override
    @Transactional
    public void postInvoiceApproval(Invoice invoice, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(invoice.getCompanyId()).orElse(null);
        if (rule == null) return;

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(rule.getAccountsReceivableAccountId(), totalAmount, BigDecimal.ZERO));
        lines.add(new Line(rule.getSalesRevenueAccountId(), BigDecimal.ZERO, netAmount));
        if (taxAmount.signum() > 0) {
            lines.add(new Line(rule.getTaxPayableAccountId(), BigDecimal.ZERO, taxAmount));
        }
        createAutoEntry(invoice.getCompanyId(), invoice.getInvoiceDate(), "Invoice " + invoice.getInvoiceNumber(),
                "INVOICE", invoice.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postPurchaseInvoiceApproval(PurchaseInvoice invoice, BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(invoice.getCompanyId()).orElse(null);
        if (rule == null) return;

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(rule.getPurchaseExpenseAccountId(), netAmount, BigDecimal.ZERO));
        if (taxAmount.signum() > 0) {
            lines.add(new Line(rule.getTaxReceivableAccountId(), taxAmount, BigDecimal.ZERO));
        }
        lines.add(new Line(rule.getAccountsPayableAccountId(), BigDecimal.ZERO, totalAmount));
        createAutoEntry(invoice.getCompanyId(), invoice.getInvoiceDate(), "Purchase invoice " + invoice.getInvoiceNumber(),
                "PURCHASE_INVOICE", invoice.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postCustomerPayment(Payment payment, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(payment.getCompanyId()).orElse(null);
        if (rule == null) return;

        Long cashOrBank = cashOrBankAccountId(rule, payment.getMethod());
        List<Line> lines = new ArrayList<>();
        if (payment.getType() == PaymentType.PAYMENT) {
            lines.add(new Line(cashOrBank, payment.getAmount(), BigDecimal.ZERO));
            lines.add(new Line(rule.getAccountsReceivableAccountId(), BigDecimal.ZERO, payment.getAmount()));
        } else {
            lines.add(new Line(rule.getAccountsReceivableAccountId(), payment.getAmount(), BigDecimal.ZERO));
            lines.add(new Line(cashOrBank, BigDecimal.ZERO, payment.getAmount()));
        }
        String label = payment.getType() == PaymentType.PAYMENT ? "Payment " : "Refund ";
        createAutoEntry(payment.getCompanyId(), payment.getPaymentDate(), label + payment.getPaymentNumber(),
                payment.getType().name(), payment.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postSupplierPayment(SupplierPayment payment, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(payment.getCompanyId()).orElse(null);
        if (rule == null) return;

        Long cashOrBank = cashOrBankAccountId(rule, payment.getMethod());
        List<Line> lines = new ArrayList<>();
        if (payment.getType() == PaymentType.PAYMENT) {
            lines.add(new Line(rule.getAccountsPayableAccountId(), payment.getAmount(), BigDecimal.ZERO));
            lines.add(new Line(cashOrBank, BigDecimal.ZERO, payment.getAmount()));
        } else {
            lines.add(new Line(cashOrBank, payment.getAmount(), BigDecimal.ZERO));
            lines.add(new Line(rule.getAccountsPayableAccountId(), BigDecimal.ZERO, payment.getAmount()));
        }
        String label = payment.getType() == PaymentType.PAYMENT ? "Supplier payment " : "Supplier refund ";
        createAutoEntry(payment.getCompanyId(), payment.getPaymentDate(), label + payment.getPaymentNumber(),
                "SUPPLIER_" + payment.getType().name(), payment.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postCreditNote(CreditNote creditNote, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(creditNote.getCompanyId()).orElse(null);
        if (rule == null) return;

        List<Line> lines = List.of(
                new Line(rule.getSalesReturnsAccountId(), creditNote.getAmount(), BigDecimal.ZERO),
                new Line(rule.getAccountsReceivableAccountId(), BigDecimal.ZERO, creditNote.getAmount())
        );
        createAutoEntry(creditNote.getCompanyId(), creditNote.getCreditNoteDate(), "Credit note " + creditNote.getCreditNoteNumber(),
                "CREDIT_NOTE", creditNote.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postPurchaseCreditNote(PurchaseCreditNote creditNote, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(creditNote.getCompanyId()).orElse(null);
        if (rule == null) return;

        List<Line> lines = List.of(
                new Line(rule.getAccountsPayableAccountId(), creditNote.getAmount(), BigDecimal.ZERO),
                new Line(rule.getPurchaseReturnsAccountId(), BigDecimal.ZERO, creditNote.getAmount())
        );
        createAutoEntry(creditNote.getCompanyId(), creditNote.getCreditNoteDate(), "Purchase credit note " + creditNote.getCreditNoteNumber(),
                "PURCHASE_CREDIT_NOTE", creditNote.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postFixedAssetAcquisition(FixedAsset asset, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(asset.getCompanyId()).orElse(null);
        if (rule == null) return;

        List<Line> lines = List.of(
                new Line(rule.getFixedAssetCostAccountId(), asset.getAcquisitionCost(), BigDecimal.ZERO),
                new Line(rule.getAccountsPayableAccountId(), BigDecimal.ZERO, asset.getAcquisitionCost())
        );
        createAutoEntry(asset.getCompanyId(), asset.getAcquisitionDate(), "Acquisition of asset " + asset.getAssetCode(),
                "FIXED_ASSET_ACQUISITION", asset.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postDepreciationRun(Long companyId, LocalDate date, BigDecimal totalAmount, Long sourceId, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(companyId).orElse(null);
        if (rule == null) return;

        List<Line> lines = List.of(
                new Line(rule.getDepreciationExpenseAccountId(), totalAmount, BigDecimal.ZERO),
                new Line(rule.getAccumulatedDepreciationAccountId(), BigDecimal.ZERO, totalAmount)
        );
        createAutoEntry(companyId, date, "Depreciation run", "DEPRECIATION_RUN", sourceId, actingUsername, lines);
    }

    @Override
    @Transactional
    public void postAssetDisposal(FixedAsset asset, BigDecimal proceeds, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(asset.getCompanyId()).orElse(null);
        if (rule == null) return;

        BigDecimal bookValue = asset.getAcquisitionCost().subtract(asset.getAccumulatedDepreciation());
        BigDecimal gainLoss = proceeds.subtract(bookValue);

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(rule.getDefaultBankAccountId(), proceeds, BigDecimal.ZERO));
        lines.add(new Line(rule.getAccumulatedDepreciationAccountId(), asset.getAccumulatedDepreciation(), BigDecimal.ZERO));
        lines.add(new Line(rule.getFixedAssetCostAccountId(), BigDecimal.ZERO, asset.getAcquisitionCost()));
        if (gainLoss.signum() > 0) {
            lines.add(new Line(rule.getAssetDisposalGainLossAccountId(), BigDecimal.ZERO, gainLoss));
        } else if (gainLoss.signum() < 0) {
            lines.add(new Line(rule.getAssetDisposalGainLossAccountId(), gainLoss.negate(), BigDecimal.ZERO));
        }
        createAutoEntry(asset.getCompanyId(), asset.getDisposalDate(), "Disposal of asset " + asset.getAssetCode(),
                "FIXED_ASSET_DISPOSAL", asset.getId(), actingUsername, lines);
    }

    @Override
    @Transactional
    public void postPosSale(Long companyId, LocalDate date, BigDecimal cashAmount, BigDecimal bankAmount,
                             BigDecimal netAmount, BigDecimal taxAmount, BigDecimal cogsAmount,
                             Long sourceId, String actingUsername) {
        PostingRule rule = postingRuleRepository.findByCompanyId(companyId).orElse(null);
        if (rule == null) return;

        List<Line> lines = new ArrayList<>();
        if (cashAmount.signum() > 0) {
            lines.add(new Line(rule.getDefaultCashAccountId(), cashAmount, BigDecimal.ZERO));
        }
        if (bankAmount.signum() > 0) {
            lines.add(new Line(rule.getDefaultBankAccountId(), bankAmount, BigDecimal.ZERO));
        }
        lines.add(new Line(rule.getSalesRevenueAccountId(), BigDecimal.ZERO, netAmount));
        if (taxAmount.signum() > 0) {
            lines.add(new Line(rule.getTaxPayableAccountId(), BigDecimal.ZERO, taxAmount));
        }
        if (cogsAmount.signum() > 0) {
            lines.add(new Line(rule.getPurchaseExpenseAccountId(), cogsAmount, BigDecimal.ZERO));
            lines.add(new Line(rule.getInventoryAssetAccountId(), BigDecimal.ZERO, cogsAmount));
        }
        createAutoEntry(companyId, date, "POS sale", "POS_SALE", sourceId, actingUsername, lines);
    }

    @Override
    @Transactional
    public void postCashVariance(Long companyId, LocalDate date, BigDecimal variance, Long sourceId, String actingUsername) {
        if (variance.signum() == 0) return;
        PostingRule rule = postingRuleRepository.findByCompanyId(companyId).orElse(null);
        if (rule == null) return;

        List<Line> lines;
        if (variance.signum() > 0) {
            lines = List.of(
                    new Line(rule.getDefaultCashAccountId(), variance, BigDecimal.ZERO),
                    new Line(rule.getCashVarianceAccountId(), BigDecimal.ZERO, variance)
            );
        } else {
            BigDecimal shortAmount = variance.negate();
            lines = List.of(
                    new Line(rule.getCashVarianceAccountId(), shortAmount, BigDecimal.ZERO),
                    new Line(rule.getDefaultCashAccountId(), BigDecimal.ZERO, shortAmount)
            );
        }
        createAutoEntry(companyId, date, "Cash variance", "POS_CASH_VARIANCE", sourceId, actingUsername, lines);
    }

    private Long cashOrBankAccountId(PostingRule rule, PaymentMethod method) {
        return method == PaymentMethod.CASH ? rule.getDefaultCashAccountId() : rule.getDefaultBankAccountId();
    }

    @Override
    @Transactional
    public void reverseAutoEntry(String sourceType, Long sourceId, String actingUsername) {
        JournalEntry original = journalEntryRepository.findBySourceTypeAndSourceId(sourceType, sourceId).orElse(null);
        if (original == null) return;

        if (original.getStatus() == JournalEntryStatus.DRAFT) {
            journalEntryLineRepository.deleteByJournalEntryId(original.getId());
            journalEntryRepository.delete(original);
            return;
        }
        if (original.getReversedByJournalEntryId() != null) return;

        List<JournalEntryLine> originalLines = journalEntryLineRepository.findByJournalEntryId(original.getId());
        JournalEntry reversal = JournalEntry.builder()
                .companyId(original.getCompanyId())
                .entryDate(LocalDate.now())
                .description("Reversal of " + original.getJournalNumber())
                .reversalOfJournalEntryId(original.getId())
                .status(JournalEntryStatus.POSTED)
                .createdBy(actingUsername)
                .postedBy(actingUsername)
                .postedAt(LocalDateTime.now())
                .build();
        journalEntryRepository.save(reversal);
        reversal.setJournalNumber("JE-" + String.format("%06d", reversal.getId()));
        journalEntryRepository.save(reversal);

        for (JournalEntryLine line : originalLines) {
            journalEntryLineRepository.save(JournalEntryLine.builder()
                    .journalEntryId(reversal.getId())
                    .accountId(line.getAccountId())
                    .debit(line.getCredit())
                    .credit(line.getDebit())
                    .costCenterId(line.getCostCenterId())
                    .description(line.getDescription())
                    .build());
        }

        original.setReversedByJournalEntryId(reversal.getId());
        journalEntryRepository.save(original);
    }

    // Silently skips (no entry created) when any line's account is
    // unmapped, or fewer than two non-zero lines survive — auto-posting
    // never blocks or fails the caller's own business transaction.
    private void createAutoEntry(Long companyId, LocalDate date, String description, String sourceType, Long sourceId,
                                  String actingUsername, List<Line> lines) {
        for (Line line : lines) {
            if (line.accountId() == null) return;
        }
        List<Line> nonZero = lines.stream().filter(l -> l.debit().signum() > 0 || l.credit().signum() > 0).toList();
        if (nonZero.size() < 2) return;

        JournalEntry entry = JournalEntry.builder()
                .companyId(companyId)
                .entryDate(date)
                .description(description)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .createdBy(actingUsername)
                .build();
        journalEntryRepository.save(entry);
        entry.setJournalNumber("JE-" + String.format("%06d", entry.getId()));

        if (!periodLockService.isLocked(companyId, date)) {
            entry.setStatus(JournalEntryStatus.POSTED);
            entry.setPostedBy(actingUsername);
            entry.setPostedAt(LocalDateTime.now());
        }
        journalEntryRepository.save(entry);

        for (Line line : nonZero) {
            journalEntryLineRepository.save(JournalEntryLine.builder()
                    .journalEntryId(entry.getId())
                    .accountId(line.accountId())
                    .debit(line.debit())
                    .credit(line.credit())
                    .build());
        }
    }
}
