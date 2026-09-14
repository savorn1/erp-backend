package com.example.erp.service.impl;

import com.example.erp.dto.CollectionActivityFilterRequest;
import com.example.erp.dto.CollectionActivityRequest;
import com.example.erp.dto.CollectionActivityResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.CollectionActivity;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Invoice;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CollectionActivityRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.InvoiceRepository;
import com.example.erp.service.CollectionActivityService;
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

@Service
@RequiredArgsConstructor
public class CollectionActivityServiceImpl implements CollectionActivityService {

    private final CollectionActivityRepository collectionActivityRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CollectionActivityResponse> list(CollectionActivityFilterRequest filter) {
        List<Specification<CollectionActivity>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getInvoiceId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("invoiceId"), filter.getInvoiceId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getResolved() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("resolved"), filter.getResolved()));
        }
        Specification<CollectionActivity> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CollectionActivity> page = collectionActivityRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public CollectionActivityResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public CollectionActivityResponse create(CollectionActivityRequest request, String actingUsername) {
        Invoice invoice = requireInvoice(request.getInvoiceId());
        CollectionActivity activity = CollectionActivity.builder()
                .companyId(invoice.getCompanyId())
                .invoiceId(invoice.getId())
                .customerId(invoice.getCustomerId())
                .activityDate(request.getActivityDate())
                .method(request.getMethod())
                .notes(request.getNotes())
                .followUpDate(request.getFollowUpDate())
                .resolved(request.isResolved())
                .createdBy(actingUsername)
                .build();
        collectionActivityRepository.save(activity);
        return toResponse(activity);
    }

    @Override
    @Transactional
    public CollectionActivityResponse update(Long id, CollectionActivityRequest request) {
        CollectionActivity activity = find(id);
        Invoice invoice = requireInvoice(request.getInvoiceId());
        activity.setInvoiceId(invoice.getId());
        activity.setCustomerId(invoice.getCustomerId());
        activity.setCompanyId(invoice.getCompanyId());
        activity.setActivityDate(request.getActivityDate());
        activity.setMethod(request.getMethod());
        activity.setNotes(request.getNotes());
        activity.setFollowUpDate(request.getFollowUpDate());
        activity.setResolved(request.isResolved());
        collectionActivityRepository.save(activity);
        return toResponse(activity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        collectionActivityRepository.delete(find(id));
    }

    private Invoice requireInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Invoice not found with id: " + invoiceId));
    }

    private CollectionActivity find(Long id) {
        return collectionActivityRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Collection activity not found with id: " + id));
    }

    private CollectionActivityResponse toResponse(CollectionActivity activity) {
        String invoiceNumber = invoiceRepository.findById(activity.getInvoiceId()).map(Invoice::getInvoiceNumber).orElse(null);
        String customerName = customerRepository.findById(activity.getCustomerId()).map(Customer::getName).orElse(null);
        return CollectionActivityResponse.builder()
                .id(activity.getId())
                .companyId(activity.getCompanyId())
                .invoiceId(activity.getInvoiceId())
                .invoiceNumber(invoiceNumber)
                .customerId(activity.getCustomerId())
                .customerName(customerName)
                .activityDate(activity.getActivityDate())
                .method(activity.getMethod().name())
                .notes(activity.getNotes())
                .followUpDate(activity.getFollowUpDate())
                .resolved(activity.isResolved())
                .createdBy(activity.getCreatedBy())
                .build();
    }
}
