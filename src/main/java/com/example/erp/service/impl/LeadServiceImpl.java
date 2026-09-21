package com.example.erp.service.impl;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.AddLeadNoteRequest;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.CreateCustomerRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
import com.example.erp.dto.LoseLeadRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateLeadRequest;
import com.example.erp.dto.UpdateLeadStatusRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Lead;
import com.example.erp.entity.LeadActivity;
import com.example.erp.entity.LeadActivityType;
import com.example.erp.entity.LeadStatus;
import com.example.erp.entity.PaymentTerms;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.LeadActivityRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CustomerService;
import com.example.erp.service.LeadService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadActivityRepository leadActivityRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> listLeads(LeadFilterRequest filter) {
        List<Specification<Lead>> conditions = new ArrayList<>();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String like = "%" + filter.getSearch().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("contactName")), like),
                    cb.like(cb.lower(root.get("organizationName")), like)));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getStatus() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getSource() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("source"), filter.getSource()));
        }
        if (filter.getAssignedToUserId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("assignedToUserId"), filter.getAssignedToUserId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        Specification<Lead> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Lead> page = leadRepository.findAll(spec, pageable);
        List<Lead> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Lead::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> usernames = userRepository.findAllById(
                content.stream().map(Lead::getAssignedToUserId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> customerNames = customerRepository.findAllById(
                content.stream().map(Lead::getCustomerId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Customer::getId, Customer::getName));

        return PageResponse.of(page.map(lead -> toResponse(lead,
                companyNames.get(lead.getCompanyId()),
                lead.getAssignedToUserId() == null ? null : usernames.get(lead.getAssignedToUserId()),
                lead.getCustomerId() == null ? null : customerNames.get(lead.getCustomerId()))));
    }

    @Override
    public LeadResponse getLead(Long id) {
        Lead lead = find(id);
        return toResponse(lead,
                companyNameOf(lead.getCompanyId()),
                lead.getAssignedToUserId() == null ? null : usernameOf(lead.getAssignedToUserId()),
                lead.getCustomerId() == null ? null : customerNameOf(lead.getCustomerId()));
    }

    @Override
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, String actingUsername) {
        // Checked before any lookup: it depends only on the request, and
        // Lead.isEditable() refuses WON/LOST, so a lead created in one of
        // those could never be edited or moved again.
        if (request.getStatus() == LeadStatus.WON || request.getStatus() == LeadStatus.LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A lead cannot be created directly as " + request.getStatus());
        }
        requireCompany(request.getCompanyId());
        if (request.getAssignedToUserId() != null) {
            requireUser(request.getAssignedToUserId(), request.getCompanyId());
        }
        if (request.getCustomerId() != null) {
            requireCustomer(request.getCustomerId(), request.getCompanyId());
        }

        Lead lead = Lead.builder()
                .companyId(request.getCompanyId())
                .contactName(request.getContactName())
                .organizationName(request.getOrganizationName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .source(request.getSource())
                .assignedToUserId(request.getAssignedToUserId())
                .estimatedValue(request.getEstimatedValue())
                .notes(request.getNotes())
                .dealName(request.getDealName())
                .amount(request.getAmount())
                .probability(request.getProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
                .customerId(request.getCustomerId())
                .createdBy(actingUsername)
                .build();
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
        }
        leadRepository.save(lead);

        // Naming the starting column in the activity log, rather than leaving
        // a bare "Lead created" that implies it began at NEW.
        recordActivity(lead.getId(), LeadActivityType.CREATED,
                lead.getStatus() == LeadStatus.NEW ? "Lead created" : "Lead created in " + lead.getStatus(), actingUsername);
        if (request.getAssignedToUserId() != null) {
            recordActivity(lead.getId(), LeadActivityType.ASSIGNED,
                    "Assigned to " + usernameOf(request.getAssignedToUserId()), actingUsername);
        }

        return getLead(lead.getId());
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Long id, UpdateLeadRequest request) {
        Lead lead = find(id);
        requireOpen(lead);
        requireCompany(request.getCompanyId());
        if (request.getCustomerId() != null) {
            requireCustomer(request.getCustomerId(), request.getCompanyId());
        }

        lead.setCompanyId(request.getCompanyId());
        lead.setContactName(request.getContactName());
        lead.setOrganizationName(request.getOrganizationName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setSource(request.getSource());
        lead.setEstimatedValue(request.getEstimatedValue());
        lead.setNotes(request.getNotes());
        lead.setDealName(request.getDealName());
        lead.setAmount(request.getAmount());
        lead.setProbability(request.getProbability());
        lead.setExpectedCloseDate(request.getExpectedCloseDate());
        lead.setCustomerId(request.getCustomerId());
        leadRepository.save(lead);
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse updateStatus(Long id, UpdateLeadStatusRequest request, String actingUsername) {
        Lead lead = find(id);
        requireOpen(lead);
        if (request.getStatus() == LeadStatus.WON || request.getStatus() == LeadStatus.LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Use the win/lose endpoints to close a lead");
        }
        LeadStatus previous = lead.getStatus();
        lead.setStatus(request.getStatus());
        leadRepository.save(lead);
        if (previous != request.getStatus()) {
            recordActivity(id, LeadActivityType.STATUS_CHANGE,
                    "Status changed from " + previous + " to " + request.getStatus(), actingUsername);
        }
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse assignSalesperson(Long id, AssignLeadRequest request, String actingUsername) {
        Lead lead = find(id);
        String description;
        if (request.getAssignedToUserId() == null) {
            description = "Unassigned";
        } else {
            requireUser(request.getAssignedToUserId(), lead.getCompanyId());
            description = "Assigned to " + usernameOf(request.getAssignedToUserId());
        }
        lead.setAssignedToUserId(request.getAssignedToUserId());
        leadRepository.save(lead);
        recordActivity(id, LeadActivityType.ASSIGNED, description, actingUsername);
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse winLead(Long id, String actingUsername) {
        Lead lead = find(id);
        requireOpen(lead);

        if (lead.getCustomerId() == null) {
            CreateCustomerRequest customerRequest = new CreateCustomerRequest();
            customerRequest.setCompanyId(lead.getCompanyId());
            customerRequest.setName(lead.getDealName() != null && !lead.getDealName().isBlank() ? lead.getDealName() : lead.getContactName());
            customerRequest.setCreditLimit(BigDecimal.ZERO);
            customerRequest.setPaymentTerms(PaymentTerms.NET_30);
            var customer = customerService.createCustomer(customerRequest, actingUsername);
            lead.setCustomerId(customer.getId());
        }

        lead.setStatus(LeadStatus.WON);
        lead.setClosedAt(LocalDateTime.now());
        leadRepository.save(lead);

        String customerName = customerNameOf(lead.getCustomerId());
        recordActivity(id, LeadActivityType.WON, "Won — customer " + customerName, actingUsername);
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse loseLead(Long id, LoseLeadRequest request, String actingUsername) {
        Lead lead = find(id);
        requireOpen(lead);

        lead.setStatus(LeadStatus.LOST);
        lead.setClosedAt(LocalDateTime.now());
        leadRepository.save(lead);

        String description = "Lost" + (request.getReason() != null && !request.getReason().isBlank() ? " — " + request.getReason() : "");
        recordActivity(id, LeadActivityType.LOST, description, actingUsername);
        return getLead(id);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        Lead lead = find(id);
        requireOpen(lead);
        leadRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadActivityResponse> listActivities(Long leadId, LeadActivityFilterRequest filter) {
        find(leadId);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());
        Page<LeadActivity> page = leadActivityRepository.findByLeadId(leadId, pageable);
        return PageResponse.of(page.map(this::toActivityResponse));
    }

    @Override
    @Transactional
    public LeadActivityResponse addNote(Long leadId, AddLeadNoteRequest request, String actingUsername) {
        find(leadId);
        LeadActivity activity = recordActivity(leadId, LeadActivityType.NOTE, request.getDescription(), actingUsername);
        return toActivityResponse(activity);
    }

    @Override
    @Transactional
    public LeadActivityResponse addFollowUp(Long leadId, AddLeadFollowUpRequest request, String actingUsername) {
        Lead lead = find(leadId);
        lead.setNextFollowUpDate(request.getNextFollowUpDate());
        leadRepository.save(lead);
        LeadActivity activity = recordActivity(leadId, LeadActivityType.FOLLOW_UP, request.getDescription(), actingUsername);
        return toActivityResponse(activity);
    }

    private void requireOpen(Lead lead) {
        if (lead.getStatus() == LeadStatus.WON || lead.getStatus() == LeadStatus.LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This lead is already closed");
        }
    }

    private LeadActivity recordActivity(Long leadId, LeadActivityType type, String description, String actingUsername) {
        LeadActivity activity = LeadActivity.builder()
                .leadId(leadId)
                .type(type)
                .description(description)
                .createdBy(actingUsername)
                .build();
        return leadActivityRepository.save(activity);
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireCustomer(Long customerId, Long companyId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Customer not found with id: " + customerId));
        if (!customer.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Customer does not belong to the selected company");
        }
    }

    // A null User.companyId means a global/unscoped user (e.g. an admin not
    // tied to one company) — always allowed. A non-null mismatch is rejected.
    private User requireUser(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));
        if (user.getCompanyId() != null && !user.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Assigned user does not belong to the selected company");
        }
        return user;
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String usernameOf(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private String customerNameOf(Long customerId) {
        return customerRepository.findById(customerId).map(Customer::getName).orElse(null);
    }

    private Lead find(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id));
    }

    private LeadResponse toResponse(Lead lead, String companyName, String assignedToUsername, String customerName) {
        return LeadResponse.builder()
                .id(lead.getId())
                .companyId(lead.getCompanyId())
                .companyName(companyName)
                .contactName(lead.getContactName())
                .organizationName(lead.getOrganizationName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .source(lead.getSource().name())
                .status(lead.getStatus().name())
                .assignedToUserId(lead.getAssignedToUserId())
                .assignedToUsername(assignedToUsername)
                .estimatedValue(lead.getEstimatedValue())
                .notes(lead.getNotes())
                .createdBy(lead.getCreatedBy())
                .nextFollowUpDate(lead.getNextFollowUpDate())
                .followUpDue(lead.isFollowUpDue())
                .dealName(lead.getDealName())
                .amount(lead.getAmount())
                .probability(lead.getProbability())
                .expectedCloseDate(lead.getExpectedCloseDate())
                .customerId(lead.getCustomerId())
                .customerName(customerName)
                .closedAt(lead.getClosedAt())
                .build();
    }

    private LeadActivityResponse toActivityResponse(LeadActivity activity) {
        return LeadActivityResponse.builder()
                .id(activity.getId())
                .leadId(activity.getLeadId())
                .type(activity.getType().name())
                .description(activity.getDescription())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
