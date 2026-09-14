package com.example.erp.service.impl;

import com.example.erp.dto.CreatePurchaseRequestRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PurchaseRequestFilterRequest;
import com.example.erp.dto.PurchaseRequestLineRequest;
import com.example.erp.dto.PurchaseRequestLineResponse;
import com.example.erp.dto.PurchaseRequestResponse;
import com.example.erp.dto.RejectPurchaseRequestRequest;
import com.example.erp.dto.UpdatePurchaseRequestRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Department;
import com.example.erp.entity.Product;
import com.example.erp.entity.PurchaseRequest;
import com.example.erp.entity.PurchaseRequestLine;
import com.example.erp.entity.PurchaseRequestStatus;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.DepartmentRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.PurchaseRequestLineRepository;
import com.example.erp.repository.PurchaseRequestRepository;
import com.example.erp.service.PurchaseRequestService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PurchaseRequestLineRepository lineRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseRequestResponse> listPurchaseRequests(PurchaseRequestFilterRequest filter) {
        List<Specification<PurchaseRequest>> conditions = new ArrayList<>();
        if (filter.getRequestNumber() != null && !filter.getRequestNumber().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("requestNumber")), "%" + filter.getRequestNumber().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getDepartmentId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("departmentId"), filter.getDepartmentId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        Specification<PurchaseRequest> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PurchaseRequest> page = purchaseRequestRepository.findAll(spec, pageable);
        List<PurchaseRequest> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(PurchaseRequest::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> departmentNames = departmentRepository.findAllById(
                content.stream().map(PurchaseRequest::getDepartmentId).distinct().toList()
        ).stream().collect(Collectors.toMap(Department::getId, Department::getName));

        return PageResponse.of(page.map(pr -> toResponse(pr,
                companyNames.get(pr.getCompanyId()),
                departmentNames.get(pr.getDepartmentId()),
                null)));
    }

    @Override
    public PurchaseRequestResponse getPurchaseRequest(Long id) {
        PurchaseRequest pr = find(id);
        List<PurchaseRequestLine> lines = lineRepository.findByPurchaseRequestId(id);
        return toFullResponse(pr, lines);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireDepartment(request.getDepartmentId());
        validateLineProducts(request.getLines(), request.getCompanyId());

        PurchaseRequest pr = PurchaseRequest.builder()
                .companyId(request.getCompanyId())
                .departmentId(request.getDepartmentId())
                .requestDate(request.getRequestDate())
                .requiredDate(request.getRequiredDate())
                .notes(request.getNotes())
                .requestedBy(actingUsername)
                .build();
        purchaseRequestRepository.save(pr);
        pr.setRequestNumber("PR-" + String.format("%06d", pr.getId()));
        purchaseRequestRepository.save(pr);

        List<PurchaseRequestLine> lines = saveLines(pr.getId(), request.getLines());
        return toFullResponse(pr, lines);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse updatePurchaseRequest(Long id, UpdatePurchaseRequestRequest request) {
        PurchaseRequest pr = find(id);
        if (pr.getStatus() != PurchaseRequestStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase requests can be edited");
        }
        requireDepartment(request.getDepartmentId());
        validateLineProducts(request.getLines(), pr.getCompanyId());

        pr.setDepartmentId(request.getDepartmentId());
        pr.setRequestDate(request.getRequestDate());
        pr.setRequiredDate(request.getRequiredDate());
        pr.setNotes(request.getNotes());
        purchaseRequestRepository.save(pr);

        lineRepository.deleteByPurchaseRequestId(id);
        List<PurchaseRequestLine> lines = saveLines(id, request.getLines());
        return toFullResponse(pr, lines);
    }

    @Override
    @Transactional
    public PurchaseRequestResponse submitPurchaseRequest(Long id) {
        PurchaseRequest pr = find(id);
        if (pr.getStatus() != PurchaseRequestStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase requests can be submitted");
        }
        pr.setStatus(PurchaseRequestStatus.SUBMITTED);
        purchaseRequestRepository.save(pr);
        return toFullResponse(pr, lineRepository.findByPurchaseRequestId(id));
    }

    @Override
    @Transactional
    public PurchaseRequestResponse approvePurchaseRequest(Long id) {
        PurchaseRequest pr = find(id);
        if (pr.getStatus() != PurchaseRequestStatus.SUBMITTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only submitted purchase requests can be approved");
        }
        pr.setStatus(PurchaseRequestStatus.APPROVED);
        pr.setRejectionReason(null);
        purchaseRequestRepository.save(pr);
        return toFullResponse(pr, lineRepository.findByPurchaseRequestId(id));
    }

    @Override
    @Transactional
    public PurchaseRequestResponse rejectPurchaseRequest(Long id, RejectPurchaseRequestRequest request) {
        PurchaseRequest pr = find(id);
        if (pr.getStatus() != PurchaseRequestStatus.SUBMITTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only submitted purchase requests can be rejected");
        }
        pr.setStatus(PurchaseRequestStatus.REJECTED);
        pr.setRejectionReason(request == null ? null : request.getReason());
        purchaseRequestRepository.save(pr);
        return toFullResponse(pr, lineRepository.findByPurchaseRequestId(id));
    }

    @Override
    @Transactional
    public void deletePurchaseRequest(Long id) {
        PurchaseRequest pr = find(id);
        if (pr.getStatus() != PurchaseRequestStatus.DRAFT) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only draft purchase requests can be deleted");
        }
        lineRepository.deleteByPurchaseRequestId(id);
        purchaseRequestRepository.deleteById(id);
    }

    private List<PurchaseRequestLine> saveLines(Long purchaseRequestId, List<PurchaseRequestLineRequest> requests) {
        List<PurchaseRequestLine> lines = requests.stream()
                .map(r -> PurchaseRequestLine.builder()
                        .purchaseRequestId(purchaseRequestId)
                        .productId(r.getProductId())
                        .quantity(r.getQuantity())
                        .notes(r.getNotes())
                        .build())
                .toList();
        return lineRepository.saveAll(lines);
    }

    private void validateLineProducts(List<PurchaseRequestLineRequest> lines, Long companyId) {
        List<Long> productIds = lines.stream().map(PurchaseRequestLineRequest::getProductId).distinct().toList();
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

    private void requireDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Department not found with id: " + departmentId);
        }
    }

    private PurchaseRequest find(Long id) {
        return purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Purchase request not found with id: " + id));
    }

    private PurchaseRequestResponse toFullResponse(PurchaseRequest pr, List<PurchaseRequestLine> lines) {
        String companyName = companyRepository.findById(pr.getCompanyId()).map(Company::getName).orElse(null);
        String departmentName = departmentRepository.findById(pr.getDepartmentId()).map(Department::getName).orElse(null);

        Map<Long, Product> products = lines.isEmpty() ? Map.of() : productRepository.findAllById(
                lines.stream().map(PurchaseRequestLine::getProductId).distinct().toList()
        ).stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<PurchaseRequestLineResponse> lineResponses = lines.stream()
                .map(line -> {
                    Product product = products.get(line.getProductId());
                    return PurchaseRequestLineResponse.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .productName(product == null ? null : product.getName())
                            .productSku(product == null ? null : product.getSku())
                            .quantity(line.getQuantity())
                            .notes(line.getNotes())
                            .build();
                })
                .toList();

        return toResponse(pr, companyName, departmentName, lineResponses);
    }

    private PurchaseRequestResponse toResponse(PurchaseRequest pr, String companyName, String departmentName,
                                                List<PurchaseRequestLineResponse> lines) {
        return PurchaseRequestResponse.builder()
                .id(pr.getId())
                .companyId(pr.getCompanyId())
                .companyName(companyName)
                .departmentId(pr.getDepartmentId())
                .departmentName(departmentName)
                .requestNumber(pr.getRequestNumber())
                .requestDate(pr.getRequestDate())
                .requiredDate(pr.getRequiredDate())
                .status(pr.getStatus().name())
                .notes(pr.getNotes())
                .rejectionReason(pr.getRejectionReason())
                .requestedBy(pr.getRequestedBy())
                .lines(lines)
                .build();
    }
}
