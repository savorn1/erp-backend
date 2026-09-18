package com.example.erp.service.impl;

import com.example.erp.dto.CreateCreditNoteRequest;
import com.example.erp.dto.CreateRmaRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ResolveRmaRequest;
import com.example.erp.dto.RmaFilterRequest;
import com.example.erp.dto.RmaLineRequest;
import com.example.erp.dto.RmaLineResponse;
import com.example.erp.dto.RmaResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.entity.InvoiceLine;
import com.example.erp.entity.InvoiceStatus;
import com.example.erp.entity.Product;
import com.example.erp.entity.RmaLine;
import com.example.erp.entity.RmaRequest;
import com.example.erp.entity.RmaResolutionType;
import com.example.erp.entity.RmaStatus;
import com.example.erp.entity.SerialNumber;
import com.example.erp.entity.StockLevel;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
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
import com.example.erp.service.RmaService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RmaServiceImpl implements RmaService {

    private final RmaRequestRepository rmaRequestRepository;
    private final RmaLineRepository rmaLineRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineRepository invoiceLineRepository;
    private final SerialNumberRepository serialNumberRepository;
    private final StockLevelRepository stockLevelRepository;
    private final CreditNoteService creditNoteService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RmaResponse> listRmas(RmaFilterRequest filter) {
        List<Specification<RmaRequest>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<RmaRequest> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<RmaRequest> page = rmaRequestRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public RmaResponse getRma(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public RmaResponse createRma(CreateRmaRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        Customer customer = requireCustomer(request.getCustomerId(), request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());

        Invoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Invoice not found with id: " + request.getInvoiceId()));
            if (!invoice.getCompanyId().equals(request.getCompanyId()) || !invoice.getCustomerId().equals(customer.getId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Invoice does not belong to the selected company/customer");
            }
            if (invoice.getStatus() != InvoiceStatus.APPROVED) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Only approved invoices can be returned against");
            }
        }

        RmaRequest rma = RmaRequest.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .invoiceId(request.getInvoiceId())
                .warehouseId(request.getWarehouseId())
                .requestDate(request.getRequestDate())
                .reason(request.getReason())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        rmaRequestRepository.save(rma);
        rma.setRmaNumber("RMA-" + String.format("%06d", rma.getId()));
        rmaRequestRepository.save(rma);

        List<RmaLine> lines = saveLines(rma.getId(), request.getCompanyId(), invoice, request.getLines());
        return toResponse(rma, lines);
    }

    @Override
    @Transactional
    public RmaResponse approveRma(Long id) {
        RmaRequest rma = find(id);
        if (rma.getStatus() != RmaStatus.REQUESTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested RMAs can be approved");
        }
        rma.setStatus(RmaStatus.APPROVED);
        rmaRequestRepository.save(rma);
        return toResponse(rma);
    }

    @Override
    @Transactional
    public RmaResponse rejectRma(Long id) {
        RmaRequest rma = find(id);
        if (rma.getStatus() != RmaStatus.REQUESTED && rma.getStatus() != RmaStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested or approved RMAs can be rejected");
        }
        rma.setStatus(RmaStatus.REJECTED);
        rmaRequestRepository.save(rma);
        return toResponse(rma);
    }

    @Override
    @Transactional
    public RmaResponse resolveRma(Long id, ResolveRmaRequest request, String actingUsername) {
        RmaRequest rma = find(id);
        if (rma.getStatus() != RmaStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only approved RMAs can be resolved");
        }
        List<RmaLine> lines = rmaLineRepository.findByRmaId(id);

        if (request.getResolutionType() == RmaResolutionType.REFUND) {
            if (rma.getInvoiceId() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "A refund requires an invoice on the RMA");
            }
            BigDecimal total = lines.stream()
                    .map(l -> l.getQuantity().multiply(l.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            CreateCreditNoteRequest creditNoteRequest = new CreateCreditNoteRequest();
            creditNoteRequest.setCreditNoteDate(LocalDate.now());
            creditNoteRequest.setReason("RMA " + rma.getRmaNumber());
            creditNoteRequest.setAmount(total);
            creditNoteService.createCreditNote(rma.getInvoiceId(), creditNoteRequest, actingUsername);
            for (RmaLine line : lines) {
                increaseStock(rma.getCompanyId(), line.getProductId(), rma.getWarehouseId(), line.getQuantity());
            }
        } else if (request.getResolutionType() == RmaResolutionType.REPLACEMENT) {
            for (RmaLine line : lines) {
                increaseStock(rma.getCompanyId(), line.getProductId(), rma.getWarehouseId(), line.getQuantity());
                decreaseStock(rma.getCompanyId(), line.getProductId(), rma.getWarehouseId(), line.getQuantity());
            }
        }
        // REPAIR: no stock or financial effect.

        rma.setStatus(RmaStatus.RESOLVED);
        rma.setResolutionType(request.getResolutionType());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            rma.setNotes(rma.getNotes() == null ? request.getNotes() : rma.getNotes() + "\n" + request.getNotes());
        }
        rmaRequestRepository.save(rma);
        return toResponse(rma, lines);
    }

    @Override
    @Transactional
    public RmaResponse cancelRma(Long id) {
        RmaRequest rma = find(id);
        if (rma.getStatus() != RmaStatus.REQUESTED && rma.getStatus() != RmaStatus.APPROVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested or approved RMAs can be cancelled");
        }
        rma.setStatus(RmaStatus.CANCELLED);
        rmaRequestRepository.save(rma);
        return toResponse(rma);
    }

    @Override
    @Transactional
    public void deleteRma(Long id) {
        RmaRequest rma = find(id);
        if (rma.getStatus() != RmaStatus.REQUESTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only requested RMAs can be deleted");
        }
        rmaLineRepository.deleteByRmaId(id);
        rmaRequestRepository.deleteById(id);
    }

    // Same unbinned-"pool"-row convention as SalesOrderServiceImpl's own
    // reservation adjustment / StockAdjustmentServiceImpl's increase — RMA
    // doesn't track bins, so everything lands on the unbinned row.
    private void increaseStock(Long companyId, Long productId, Long warehouseId, BigDecimal quantity) {
        StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder().companyId(companyId).productId(productId).warehouseId(warehouseId).build());
        stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().add(quantity));
        stockLevelRepository.save(stockLevel);
    }

    private void decreaseStock(Long companyId, Long productId, Long warehouseId, BigDecimal quantity) {
        StockLevel stockLevel = stockLevelRepository.findByProductIdAndWarehouseIdAndBinIdIsNull(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder().companyId(companyId).productId(productId).warehouseId(warehouseId).build());
        stockLevel.setQuantityOnHand(stockLevel.getQuantityOnHand().subtract(quantity));
        stockLevelRepository.save(stockLevel);
    }

    private List<RmaLine> saveLines(Long rmaId, Long companyId, Invoice invoice, List<RmaLineRequest> requests) {
        Map<Long, InvoiceLine> invoiceLinesById = invoice == null ? Map.of()
                : invoiceLineRepository.findByInvoiceId(invoice.getId()).stream()
                        .collect(Collectors.toMap(InvoiceLine::getId, l -> l));

        List<RmaLine> lines = new ArrayList<>();
        for (RmaLineRequest request : requests) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + request.getProductId()));
            if (!product.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product " + product.getSku() + " does not belong to the selected company");
            }

            BigDecimal unitPrice = request.getUnitPrice();
            if (request.getInvoiceLineId() != null) {
                InvoiceLine invoiceLine = invoiceLinesById.get(request.getInvoiceLineId());
                if (invoiceLine == null) {
                    throw new AppException(HttpStatus.BAD_REQUEST, "Invoice line not found on the selected invoice: " + request.getInvoiceLineId());
                }
                if (unitPrice == null) {
                    unitPrice = invoiceLine.getUnitPrice();
                }
            }
            if (unitPrice == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Unit price is required when a line has no invoice line to default from");
            }

            lines.add(rmaLineRepository.save(RmaLine.builder()
                    .rmaId(rmaId)
                    .productId(request.getProductId())
                    .invoiceLineId(request.getInvoiceLineId())
                    .serialNumberId(request.getSerialNumberId())
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .reasonNote(request.getReasonNote())
                    .build()));
        }
        return lines;
    }

    private RmaRequest find(Long id) {
        return rmaRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RMA not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
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

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }

    private RmaResponse toResponse(RmaRequest rma) {
        return toResponse(rma, rmaLineRepository.findByRmaId(rma.getId()));
    }

    private RmaResponse toResponse(RmaRequest rma, List<RmaLine> lines) {
        String companyName = companyRepository.findById(rma.getCompanyId()).map(Company::getName).orElse(null);
        String customerName = customerRepository.findById(rma.getCustomerId()).map(Customer::getName).orElse(null);
        String warehouseName = warehouseRepository.findById(rma.getWarehouseId()).map(Warehouse::getName).orElse(null);
        Invoice invoice = rma.getInvoiceId() == null ? null : invoiceRepository.findById(rma.getInvoiceId()).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(RmaLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, SerialNumber> serials = lines.stream().map(RmaLine::getSerialNumberId).filter(java.util.Objects::nonNull).distinct().toList().isEmpty()
                ? Map.of()
                : serialNumberRepository.findAllById(lines.stream().map(RmaLine::getSerialNumberId).filter(java.util.Objects::nonNull).distinct().toList())
                        .stream().collect(Collectors.toMap(SerialNumber::getId, s -> s));

        BigDecimal totalRefundable = BigDecimal.ZERO;
        List<RmaLineResponse> lineResponses = new ArrayList<>();
        for (RmaLine line : lines) {
            Product product = products.get(line.getProductId());
            SerialNumber serial = line.getSerialNumberId() == null ? null : serials.get(line.getSerialNumberId());
            BigDecimal lineTotal = line.getQuantity().multiply(line.getUnitPrice());
            totalRefundable = totalRefundable.add(lineTotal);

            Boolean withinWarranty = null;
            if (invoice != null && product != null && product.getWarrantyMonths() != null) {
                LocalDate expiry = invoice.getInvoiceDate().plusMonths(product.getWarrantyMonths());
                withinWarranty = !LocalDate.now().isAfter(expiry);
            }

            lineResponses.add(RmaLineResponse.builder()
                    .id(line.getId())
                    .productId(line.getProductId())
                    .productName(product == null ? null : product.getName())
                    .productSku(product == null ? null : product.getSku())
                    .invoiceLineId(line.getInvoiceLineId())
                    .serialNumberId(line.getSerialNumberId())
                    .serialNumberValue(serial == null ? null : serial.getSerialNumber())
                    .quantity(line.getQuantity())
                    .unitPrice(line.getUnitPrice())
                    .lineTotal(lineTotal)
                    .reasonNote(line.getReasonNote())
                    .withinWarranty(withinWarranty)
                    .build());
        }

        return RmaResponse.builder()
                .id(rma.getId())
                .companyId(rma.getCompanyId())
                .companyName(companyName)
                .customerId(rma.getCustomerId())
                .customerName(customerName)
                .invoiceId(rma.getInvoiceId())
                .invoiceNumber(invoice == null ? null : invoice.getInvoiceNumber())
                .warehouseId(rma.getWarehouseId())
                .warehouseName(warehouseName)
                .rmaNumber(rma.getRmaNumber())
                .requestDate(rma.getRequestDate())
                .status(rma.getStatus())
                .resolutionType(rma.getResolutionType())
                .reason(rma.getReason())
                .notes(rma.getNotes())
                .createdBy(rma.getCreatedBy())
                .totalRefundable(totalRefundable)
                .lines(lineResponses)
                .build();
    }
}
