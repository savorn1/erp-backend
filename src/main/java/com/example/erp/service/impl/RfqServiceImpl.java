package com.example.erp.service.impl;

import com.example.erp.dto.CreateRfqRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RecordRfqQuotationRequest;
import com.example.erp.dto.RfqFilterRequest;
import com.example.erp.dto.RfqLineRequest;
import com.example.erp.dto.RfqLineResponse;
import com.example.erp.dto.RfqQuotationLineItem;
import com.example.erp.dto.RfqQuotationLineResponse;
import com.example.erp.dto.RfqResponse;
import com.example.erp.dto.RfqSupplierResponse;
import com.example.erp.dto.UpdateRfqRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Product;
import com.example.erp.entity.ProductUom;
import com.example.erp.entity.UnitOfMeasure;
import com.example.erp.entity.PurchaseOrder;
import com.example.erp.entity.PurchaseOrderLine;
import com.example.erp.entity.PurchaseOrderStatus;
import com.example.erp.entity.PurchaseRequest;
import com.example.erp.entity.Rfq;
import com.example.erp.entity.RfqLine;
import com.example.erp.entity.RfqQuotationLine;
import com.example.erp.entity.RfqStatus;
import com.example.erp.entity.RfqSupplier;
import com.example.erp.entity.RfqSupplierStatus;
import com.example.erp.entity.Supplier;
import com.example.erp.entity.Warehouse;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.ProductUomRepository;
import com.example.erp.repository.UnitOfMeasureRepository;
import com.example.erp.repository.PurchaseOrderLineRepository;
import com.example.erp.repository.PurchaseOrderRepository;
import com.example.erp.repository.PurchaseRequestRepository;
import com.example.erp.repository.RfqLineRepository;
import com.example.erp.repository.RfqQuotationLineRepository;
import com.example.erp.repository.RfqRepository;
import com.example.erp.repository.RfqSupplierRepository;
import com.example.erp.repository.SupplierRepository;
import com.example.erp.repository.WarehouseRepository;
import com.example.erp.service.RfqService;
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
public class RfqServiceImpl implements RfqService {

    private final RfqRepository rfqRepository;
    private final RfqLineRepository rfqLineRepository;
    private final RfqSupplierRepository rfqSupplierRepository;
    private final RfqQuotationLineRepository rfqQuotationLineRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductUomRepository productUomRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RfqResponse> listRfqs(RfqFilterRequest filter) {
        List<Specification<Rfq>> conditions = new ArrayList<>();
        if (filter.getRfqNumber() != null && !filter.getRfqNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("rfqNumber")), "%" + filter.getRfqNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getWarehouseId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("warehouseId"), filter.getWarehouseId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<Rfq> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Rfq> page = rfqRepository.findAll(spec, pageable);
        List<Rfq> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Rfq::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> warehouseNames = warehouseRepository.findAllById(
                content.stream().map(Rfq::getWarehouseId).distinct().toList()
        ).stream().collect(Collectors.toMap(Warehouse::getId, Warehouse::getName));

        return PageResponse.of(page.map(rfq -> RfqResponse.builder()
                .id(rfq.getId())
                .companyId(rfq.getCompanyId())
                .companyName(companyNames.get(rfq.getCompanyId()))
                .warehouseId(rfq.getWarehouseId())
                .warehouseName(warehouseNames.get(rfq.getWarehouseId()))
                .purchaseRequestId(rfq.getPurchaseRequestId())
                .rfqNumber(rfq.getRfqNumber())
                .issueDate(rfq.getIssueDate())
                .status(rfq.getStatus().name())
                .cancelledFromStatus(rfq.getCancelledFromStatus() == null ? null : rfq.getCancelledFromStatus().name())
                .notes(rfq.getNotes())
                .createdBy(rfq.getCreatedBy())
                .awardedSupplierId(rfq.getAwardedSupplierId())
                .awardedPurchaseOrderId(rfq.getAwardedPurchaseOrderId())
                .build()));
    }

    @Override
    public RfqResponse getRfq(Long id) {
        Rfq rfq = find(id);
        return toFullResponse(rfq, rfqLineRepository.findByRfqId(id), rfqSupplierRepository.findByRfqId(id));
    }

    @Override
    @Transactional
    public RfqResponse createRfq(CreateRfqRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireWarehouse(request.getWarehouseId(), request.getCompanyId());
        requireSuppliers(request.getSupplierIds(), request.getCompanyId());
        validateLineProducts(request.getLines(), request.getCompanyId());

        Rfq rfq = Rfq.builder()
                .companyId(request.getCompanyId())
                .warehouseId(request.getWarehouseId())
                .purchaseRequestId(request.getPurchaseRequestId())
                .issueDate(request.getIssueDate())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        rfqRepository.save(rfq);
        rfq.setRfqNumber("RFQ-" + String.format("%06d", rfq.getId()));
        rfqRepository.save(rfq);

        List<RfqLine> lines = saveLines(rfq.getId(), request.getLines());
        List<RfqSupplier> suppliers = saveSuppliers(rfq.getId(), request.getSupplierIds());
        return toFullResponse(rfq, lines, suppliers);
    }

    @Override
    @Transactional
    public RfqResponse updateRfq(Long id, UpdateRfqRequest request) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft RFQs can be edited");
        }
        requireWarehouse(request.getWarehouseId(), rfq.getCompanyId());
        requireSuppliers(request.getSupplierIds(), rfq.getCompanyId());
        validateLineProducts(request.getLines(), rfq.getCompanyId());

        rfq.setWarehouseId(request.getWarehouseId());
        rfq.setIssueDate(request.getIssueDate());
        rfq.setNotes(request.getNotes());
        rfqRepository.save(rfq);

        rfqLineRepository.deleteByRfqId(id);
        List<RfqLine> lines = saveLines(id, request.getLines());

        // Safe to fully replace — quotations can only be recorded once the
        // Rfq has been sent, and edits are DRAFT-only.
        rfqSupplierRepository.deleteByRfqId(id);
        List<RfqSupplier> suppliers = saveSuppliers(id, request.getSupplierIds());
        return toFullResponse(rfq, lines, suppliers);
    }

    @Override
    @Transactional
    public RfqResponse sendRfq(Long id) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft RFQs can be sent");
        }
        rfq.setStatus(RfqStatus.SENT);
        rfqRepository.save(rfq);
        return toFullResponse(rfq, rfqLineRepository.findByRfqId(id), rfqSupplierRepository.findByRfqId(id));
    }

    @Override
    @Transactional
    public RfqResponse recordQuotation(Long id, Long supplierId, RecordRfqQuotationRequest request) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.SENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Quotations can only be recorded for a sent RFQ");
        }
        RfqSupplier rfqSupplier = rfqSupplierRepository.findByRfqIdAndSupplierId(id, supplierId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier was not invited to this RFQ"));

        List<RfqLine> lines = rfqLineRepository.findByRfqId(id);
        java.util.Set<Long> rfqProductIds = lines.stream().map(RfqLine::getProductId).collect(Collectors.toSet());
        for (RfqQuotationLineItem item : request.getLines()) {
            if (!rfqProductIds.contains(item.getProductId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product " + item.getProductId() + " is not part of this RFQ");
            }
        }

        rfqQuotationLineRepository.deleteByRfqIdAndSupplierId(id, supplierId);
        List<RfqQuotationLine> quotationLines = request.getLines().stream()
                .map(item -> RfqQuotationLine.builder()
                        .rfqId(id)
                        .supplierId(supplierId)
                        .productId(item.getProductId())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();
        rfqQuotationLineRepository.saveAll(quotationLines);

        rfqSupplier.setStatus(RfqSupplierStatus.QUOTED);
        rfqSupplierRepository.save(rfqSupplier);

        return toFullResponse(rfq, lines, rfqSupplierRepository.findByRfqId(id));
    }

    @Override
    @Transactional
    public RfqResponse selectSupplier(Long id, Long supplierId, String actingUsername) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.SENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only a sent RFQ can have a supplier selected");
        }
        RfqSupplier rfqSupplier = rfqSupplierRepository.findByRfqIdAndSupplierId(id, supplierId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Supplier was not invited to this RFQ"));
        if (rfqSupplier.getStatus() != RfqSupplierStatus.QUOTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Supplier has not submitted a quotation yet");
        }

        List<RfqLine> lines = rfqLineRepository.findByRfqId(id);
        Map<Long, BigDecimal> quotedPrices = rfqQuotationLineRepository.findByRfqIdAndSupplierId(id, supplierId).stream()
                .collect(Collectors.toMap(RfqQuotationLine::getProductId, RfqQuotationLine::getUnitPrice));
        for (RfqLine line : lines) {
            if (!quotedPrices.containsKey(line.getProductId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Supplier has not quoted a price for every requested item");
            }
        }

        PurchaseOrder po = PurchaseOrder.builder()
                .companyId(rfq.getCompanyId())
                .supplierId(supplierId)
                .warehouseId(rfq.getWarehouseId())
                .orderDate(LocalDate.now())
                .notes("Created from RFQ " + rfq.getRfqNumber())
                .status(PurchaseOrderStatus.DRAFT)
                .createdBy(actingUsername)
                .build();
        purchaseOrderRepository.save(po);
        po.setPoNumber("PO-" + String.format("%06d", po.getId()));
        purchaseOrderRepository.save(po);

        List<PurchaseOrderLine> poLines = lines.stream()
                .map(line -> PurchaseOrderLine.builder()
                        .purchaseOrderId(po.getId())
                        .productId(line.getProductId())
                        // Suppliers quoted a price per the RFQ's unit, so the awarded
                        // order has to inherit it — otherwise the price would be
                        // reinterpreted against the product's base unit.
                        .unitOfMeasureId(line.getUnitOfMeasureId())
                        .conversionFactor(conversionFactorOf(line))
                        .quantityOrdered(line.getQuantity())
                        .unitCost(quotedPrices.get(line.getProductId()))
                        .build())
                .toList();
        purchaseOrderLineRepository.saveAll(poLines);

        rfq.setStatus(RfqStatus.CLOSED);
        rfq.setAwardedSupplierId(supplierId);
        rfq.setAwardedPurchaseOrderId(po.getId());
        rfqRepository.save(rfq);

        return toFullResponse(rfq, lines, rfqSupplierRepository.findByRfqId(id));
    }

    @Override
    @Transactional
    public RfqResponse cancelRfq(Long id) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.DRAFT && rfq.getStatus() != RfqStatus.SENT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft or sent RFQs can be cancelled");
        }
        // Captured before the overwrite — this is the only record of how far the workflow got.
        rfq.setCancelledFromStatus(rfq.getStatus());
        rfq.setStatus(RfqStatus.CANCELLED);
        rfqRepository.save(rfq);
        return toFullResponse(rfq, rfqLineRepository.findByRfqId(id), rfqSupplierRepository.findByRfqId(id));
    }

    @Override
    @Transactional
    public void deleteRfq(Long id) {
        Rfq rfq = find(id);
        if (rfq.getStatus() != RfqStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft RFQs can be deleted");
        }
        rfqQuotationLineRepository.deleteByRfqId(id);
        rfqSupplierRepository.deleteByRfqId(id);
        rfqLineRepository.deleteByRfqId(id);
        rfqRepository.deleteById(id);
    }

    private List<RfqLine> saveLines(Long rfqId, List<RfqLineRequest> requests) {
        Map<Long, Product> products = requests.isEmpty() ? Map.of() : productRepository.findAllById(
                requests.stream().map(RfqLineRequest::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<RfqLine> lines = requests.stream()
                .map(r -> {
                    UnitSelection unit = resolveLineUnit(products.get(r.getProductId()), r.getUnitOfMeasureId());
                    return RfqLine.builder()
                            .rfqId(rfqId)
                            .productId(r.getProductId())
                            .unitOfMeasureId(unit.unitOfMeasureId())
                            .conversionFactor(unit.conversionFactor())
                            .quantity(r.getQuantity())
                            .build();
                })
                .toList();
        return rfqLineRepository.saveAll(lines);
    }

    private record UnitSelection(Long unitOfMeasureId, BigDecimal conversionFactor) {}

    // Same rule as PurchaseOrderServiceImpl — an awarded RFQ becomes a purchase
    // order, so the unit quoted against has to be legal there too.
    private UnitSelection resolveLineUnit(Product product, Long requestedUnitOfMeasureId) {
        // Deliberately tolerant: callers that require a real product validate it
        // separately, and a product with no base unit configured predates UoM setup.
        // Falling back to "no conversion" keeps this from changing which lines are
        // allowed to save — it only adds the unit when there is one to add.
        Long unitId = requestedUnitOfMeasureId != null ? requestedUnitOfMeasureId
                : (product == null ? null : product.getUnitOfMeasureId());
        if (product == null || unitId == null || unitId.equals(product.getUnitOfMeasureId())) {
            return new UnitSelection(unitId, BigDecimal.ONE);
        }
        ProductUom productUom = productUomRepository
                .findByProductIdAndVariantIdIsNullAndUnitOfMeasureId(product.getId(), unitId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                        "This unit is not configured for " + product.getName() + " — add it under the product's UOMs first"));
        if (!productUom.isAllowPurchase()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This unit is not allowed for purchasing " + product.getName());
        }
        return new UnitSelection(unitId, productUom.getConversionFactor());
    }

    private BigDecimal conversionFactorOf(RfqLine line) {
        return line.getConversionFactor() != null ? line.getConversionFactor() : BigDecimal.ONE;
    }

    private List<RfqSupplier> saveSuppliers(Long rfqId, List<Long> supplierIds) {
        List<RfqSupplier> suppliers = supplierIds.stream()
                .distinct()
                .map(supplierId -> RfqSupplier.builder().rfqId(rfqId).supplierId(supplierId).build())
                .toList();
        return rfqSupplierRepository.saveAll(suppliers);
    }

    private void validateLineProducts(List<RfqLineRequest> lines, Long companyId) {
        List<Long> productIds = lines.stream().map(RfqLineRequest::getProductId).distinct().toList();
        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        for (Long productId : productIds) {
            Product product = products.get(productId);
            if (product == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId);
            }
            if (!product.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company: " + product.getName());
            }
        }
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireWarehouse(Long warehouseId, Long companyId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Warehouse not found with id: " + warehouseId));
        if (!warehouse.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Warehouse does not belong to the selected company");
        }
    }

    private void requireSuppliers(List<Long> supplierIds, Long companyId) {
        Map<Long, Supplier> suppliers = supplierRepository.findAllById(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, s -> s));
        for (Long supplierId : supplierIds) {
            Supplier supplier = suppliers.get(supplierId);
            if (supplier == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Supplier not found with id: " + supplierId);
            }
            if (!supplier.getCompanyId().equals(companyId)) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Supplier does not belong to the selected company: " + supplier.getName());
            }
        }
    }

    private Rfq find(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RFQ not found with id: " + id));
    }

    private RfqResponse toFullResponse(Rfq rfq, List<RfqLine> lines, List<RfqSupplier> invitedSuppliers) {
        String companyName = companyRepository.findById(rfq.getCompanyId()).map(Company::getName).orElse(null);
        String warehouseName = warehouseRepository.findById(rfq.getWarehouseId()).map(Warehouse::getName).orElse(null);
        String purchaseRequestNumber = rfq.getPurchaseRequestId() == null ? null
                : purchaseRequestRepository.findById(rfq.getPurchaseRequestId()).map(PurchaseRequest::getRequestNumber).orElse(null);
        String awardedSupplierName = rfq.getAwardedSupplierId() == null ? null
                : supplierRepository.findById(rfq.getAwardedSupplierId()).map(Supplier::getName).orElse(null);
        String awardedPoNumber = rfq.getAwardedPurchaseOrderId() == null ? null
                : purchaseOrderRepository.findById(rfq.getAwardedPurchaseOrderId()).map(PurchaseOrder::getPoNumber).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(RfqLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, UnitOfMeasure> units = unitOfMeasureRepository.findAllById(
                lines.stream().map(RfqLine::getUnitOfMeasureId).filter(java.util.Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(UnitOfMeasure::getId, u -> u));

        List<RfqLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    UnitOfMeasure unit = units.get(line.getUnitOfMeasureId());
                    BigDecimal conversionFactor = conversionFactorOf(line);
                    return RfqLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .unitOfMeasureId(line.getUnitOfMeasureId())
                            .unitOfMeasureAbbreviation(unit == null ? null : unit.getAbbreviation())
                            .conversionFactor(conversionFactor)
                            .baseQuantity(line.getQuantity().multiply(conversionFactor))
                            .quantity(line.getQuantity())
                            .build();
                })
                .toList();

        Map<Long, String> supplierNames = invitedSuppliers.isEmpty() ? Map.of() : supplierRepository.findAllById(
                invitedSuppliers.stream().map(RfqSupplier::getSupplierId).toList()
        ).stream().collect(Collectors.toMap(Supplier::getId, Supplier::getName));

        List<RfqSupplierResponse> supplierResponses = invitedSuppliers.stream()
                .map(rfqSupplier -> {
                    Map<Long, BigDecimal> quotedPrices = rfqQuotationLineRepository
                            .findByRfqIdAndSupplierId(rfq.getId(), rfqSupplier.getSupplierId()).stream()
                            .collect(Collectors.toMap(RfqQuotationLine::getProductId, RfqQuotationLine::getUnitPrice));

                    List<RfqQuotationLineResponse> quotationLines = lines.stream()
                            .map(line -> {
                                Product product = products.get(line.getProductId());
                                BigDecimal unitPrice = quotedPrices.get(line.getProductId());
                                return RfqQuotationLineResponse.builder()
                                        .productId(line.getProductId())
                                        .productName(product == null ? null : product.getName())
                                        .productSku(product == null ? null : product.getSku())
                                        .quantity(line.getQuantity())
                                        .unitPrice(unitPrice)
                                        .lineTotal(unitPrice == null ? null : line.getQuantity().multiply(unitPrice))
                                        .build();
                            })
                            .toList();
                    boolean fullyQuoted = quotationLines.stream().allMatch(l -> l.getUnitPrice() != null);
                    BigDecimal quotedTotal = !fullyQuoted ? null : quotationLines.stream()
                            .map(RfqQuotationLineResponse::getLineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return RfqSupplierResponse.builder()
                            .supplierId(rfqSupplier.getSupplierId())
                            .supplierName(supplierNames.get(rfqSupplier.getSupplierId()))
                            .status(rfqSupplier.getStatus().name())
                            .quotedTotal(quotedTotal)
                            .lines(quotationLines)
                            .build();
                })
                .toList();

        return RfqResponse.builder()
                .id(rfq.getId())
                .companyId(rfq.getCompanyId())
                .companyName(companyName)
                .warehouseId(rfq.getWarehouseId())
                .warehouseName(warehouseName)
                .purchaseRequestId(rfq.getPurchaseRequestId())
                .purchaseRequestNumber(purchaseRequestNumber)
                .rfqNumber(rfq.getRfqNumber())
                .issueDate(rfq.getIssueDate())
                .status(rfq.getStatus().name())
                .cancelledFromStatus(rfq.getCancelledFromStatus() == null ? null : rfq.getCancelledFromStatus().name())
                .notes(rfq.getNotes())
                .createdBy(rfq.getCreatedBy())
                .awardedSupplierId(rfq.getAwardedSupplierId())
                .awardedSupplierName(awardedSupplierName)
                .awardedPurchaseOrderId(rfq.getAwardedPurchaseOrderId())
                .awardedPurchaseOrderNumber(awardedPoNumber)
                .lines(lineResponses)
                .suppliers(supplierResponses)
                .build();
    }
}
