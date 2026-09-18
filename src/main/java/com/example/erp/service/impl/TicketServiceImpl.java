package com.example.erp.service.impl;

import com.example.erp.dto.CreateTicketCommentRequest;
import com.example.erp.dto.CreateTicketRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TicketCommentResponse;
import com.example.erp.dto.TicketFilterRequest;
import com.example.erp.dto.TicketResponse;
import com.example.erp.dto.UpdateTicketRequest;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Product;
import com.example.erp.entity.Ticket;
import com.example.erp.entity.TicketComment;
import com.example.erp.entity.TicketPriority;
import com.example.erp.entity.TicketStatus;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.ProductRepository;
import com.example.erp.repository.TicketCommentRepository;
import com.example.erp.repository.TicketRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.TicketService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> listTickets(TicketFilterRequest filter) {
        List<Specification<Ticket>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getPriority() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("priority"), filter.getPriority()));
        }
        if (filter.getAssignedToUserId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("assignedToUserId"), filter.getAssignedToUserId()));
        }
        Specification<Ticket> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Ticket> page = ticketRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public TicketResponse getTicket(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        requireCustomer(request.getCustomerId(), request.getCompanyId());
        if (request.getProductId() != null) {
            requireProduct(request.getProductId(), request.getCompanyId());
        }

        Ticket ticket = Ticket.builder()
                .companyId(request.getCompanyId())
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() == null ? TicketPriority.MEDIUM : request.getPriority())
                .assignedToUserId(request.getAssignedToUserId())
                .createdBy(actingUsername)
                .build();
        ticketRepository.save(ticket);
        ticket.setTicketNumber("TKT-" + String.format("%06d", ticket.getId()));
        ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.OPEN && ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only open or in-progress tickets can be edited");
        }
        if (request.getProductId() != null) {
            requireProduct(request.getProductId(), ticket.getCompanyId());
        }
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setProductId(request.getProductId());
        ticket.setAssignedToUserId(request.getAssignedToUserId());
        ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse addComment(Long id, CreateTicketCommentRequest request, String actingUsername) {
        Ticket ticket = find(id);
        ticketCommentRepository.save(TicketComment.builder()
                .ticketId(ticket.getId())
                .authorUsername(actingUsername)
                .body(request.getBody())
                .internal(request.isInternal())
                .build());
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse startProgress(Long id) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only open tickets can be started");
        }
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse resolveTicket(Long id) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.OPEN && ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only open or in-progress tickets can be resolved");
        }
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse closeTicket(Long id) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only resolved tickets can be closed");
        }
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse reopenTicket(Long id) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only resolved or closed tickets can be reopened");
        }
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setResolvedAt(null);
        ticket.setClosedAt(null);
        ticketRepository.save(ticket);
        return toResponse(ticket);
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only open tickets can be deleted");
        }
        ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(id).forEach(c -> ticketCommentRepository.deleteById(c.getId()));
        ticketRepository.deleteById(id);
    }

    private Ticket find(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Ticket not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireCustomer(Long customerId, Long companyId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company");
        }
    }

    private void requireProduct(Long productId, Long companyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Product not found with id: " + productId));
        if (!product.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product does not belong to the selected company");
        }
    }

    private TicketResponse toResponse(Ticket ticket) {
        String customerName = customerRepository.findById(ticket.getCustomerId()).map(Customer::getName).orElse(null);
        String productName = ticket.getProductId() == null ? null
                : productRepository.findById(ticket.getProductId()).map(Product::getName).orElse(null);
        String assignedToUsername = ticket.getAssignedToUserId() == null ? null
                : userRepository.findById(ticket.getAssignedToUserId()).map(User::getUsername).orElse(null);

        LocalDate endDate = ticket.getClosedAt() != null ? ticket.getClosedAt().toLocalDate() : LocalDate.now();
        long daysOpen = ChronoUnit.DAYS.between(ticket.getCreatedAt().toLocalDate(), endDate);

        List<TicketCommentResponse> comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                .map(c -> TicketCommentResponse.builder()
                        .id(c.getId())
                        .authorUsername(c.getAuthorUsername())
                        .body(c.getBody())
                        .internal(c.isInternal())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        return TicketResponse.builder()
                .id(ticket.getId())
                .companyId(ticket.getCompanyId())
                .customerId(ticket.getCustomerId())
                .customerName(customerName)
                .productId(ticket.getProductId())
                .productName(productName)
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .assignedToUserId(ticket.getAssignedToUserId())
                .assignedToUsername(assignedToUsername)
                .createdBy(ticket.getCreatedBy())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .daysOpen(daysOpen)
                .overdue(ticket.isOverdue())
                .comments(comments)
                .build();
    }
}
