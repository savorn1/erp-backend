package com.example.erp.service.impl;

import com.example.erp.dto.AddOpportunityFollowUpRequest;
import com.example.erp.dto.AddOpportunityNoteRequest;
import com.example.erp.dto.CreateCustomerRequest;
import com.example.erp.dto.CreateOpportunityRequest;
import com.example.erp.dto.LoseOpportunityRequest;
import com.example.erp.dto.OpportunityActivityFilterRequest;
import com.example.erp.dto.OpportunityActivityResponse;
import com.example.erp.dto.OpportunityFilterRequest;
import com.example.erp.dto.OpportunityResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateOpportunityRequest;
import com.example.erp.dto.UpdateOpportunityStageRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Customer;
import com.example.erp.entity.Lead;
import com.example.erp.entity.Opportunity;
import com.example.erp.entity.OpportunityActivity;
import com.example.erp.entity.OpportunityActivityType;
import com.example.erp.entity.OpportunityStage;
import com.example.erp.entity.PaymentTerms;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.OpportunityActivityRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CustomerService;
import com.example.erp.service.OpportunityService;
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
public class OpportunityServiceImpl implements OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final OpportunityActivityRepository opportunityActivityRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpportunityResponse> listOpportunities(OpportunityFilterRequest filter) {
        List<Specification<Opportunity>> conditions = new ArrayList<>();
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getSearch().toLowerCase() + "%"));
        }
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getStage() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("stage"), filter.getStage()));
        }
        if (filter.getLeadId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("leadId"), filter.getLeadId()));
        }
        if (filter.getCustomerId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("customerId"), filter.getCustomerId()));
        }
        if (filter.getAssignedToUserId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("assignedToUserId"), filter.getAssignedToUserId()));
        }
        Specification<Opportunity> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Opportunity> page = opportunityRepository.findAll(spec, pageable);
        List<Opportunity> content = page.getContent();

        Map<Long, String> companyNames = companyRepository.findAllById(
                content.stream().map(Opportunity::getCompanyId).distinct().toList()
        ).stream().collect(Collectors.toMap(Company::getId, Company::getName));
        Map<Long, String> leadNames = leadRepository.findAllById(
                content.stream().map(Opportunity::getLeadId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Lead::getId, Lead::getContactName));
        Map<Long, String> customerNames = customerRepository.findAllById(
                content.stream().map(Opportunity::getCustomerId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Customer::getId, Customer::getName));
        Map<Long, String> usernames = userRepository.findAllById(
                content.stream().map(Opportunity::getAssignedToUserId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(User::getId, User::getUsername));

        return PageResponse.of(page.map(o -> toResponse(o,
                companyNames.get(o.getCompanyId()),
                o.getLeadId() == null ? null : leadNames.get(o.getLeadId()),
                o.getCustomerId() == null ? null : customerNames.get(o.getCustomerId()),
                o.getAssignedToUserId() == null ? null : usernames.get(o.getAssignedToUserId()))));
    }

    @Override
    public OpportunityResponse getOpportunity(Long id) {
        Opportunity opportunity = find(id);
        return toResponse(opportunity,
                companyNameOf(opportunity.getCompanyId()),
                opportunity.getLeadId() == null ? null : leadRepository.findById(opportunity.getLeadId()).map(Lead::getContactName).orElse(null),
                opportunity.getCustomerId() == null ? null : customerNameOf(opportunity.getCustomerId()),
                opportunity.getAssignedToUserId() == null ? null : usernameOf(opportunity.getAssignedToUserId()));
    }

    @Override
    @Transactional
    public OpportunityResponse createOpportunity(CreateOpportunityRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        if (request.getLeadId() != null) {
            requireLead(request.getLeadId(), request.getCompanyId());
        }
        if (request.getCustomerId() != null) {
            requireCustomer(request.getCustomerId(), request.getCompanyId());
        }
        if (request.getAssignedToUserId() != null) {
            requireUser(request.getAssignedToUserId(), request.getCompanyId());
        }

        Opportunity opportunity = Opportunity.builder()
                .companyId(request.getCompanyId())
                .leadId(request.getLeadId())
                .customerId(request.getCustomerId())
                .name(request.getName())
                .amount(request.getAmount())
                .probability(request.getProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
                .assignedToUserId(request.getAssignedToUserId())
                .notes(request.getNotes())
                .createdBy(actingUsername)
                .build();
        opportunityRepository.save(opportunity);

        recordActivity(opportunity.getId(), OpportunityActivityType.CREATED, "Opportunity created", actingUsername);
        return getOpportunity(opportunity.getId());
    }

    @Override
    @Transactional
    public OpportunityResponse updateOpportunity(Long id, UpdateOpportunityRequest request) {
        Opportunity opportunity = find(id);
        requireOpen(opportunity);
        requireCompany(request.getCompanyId());
        if (request.getAssignedToUserId() != null) {
            requireUser(request.getAssignedToUserId(), request.getCompanyId());
        }

        opportunity.setCompanyId(request.getCompanyId());
        opportunity.setName(request.getName());
        opportunity.setAmount(request.getAmount());
        opportunity.setProbability(request.getProbability());
        opportunity.setExpectedCloseDate(request.getExpectedCloseDate());
        opportunity.setAssignedToUserId(request.getAssignedToUserId());
        opportunity.setNotes(request.getNotes());
        opportunityRepository.save(opportunity);
        return getOpportunity(id);
    }

    @Override
    @Transactional
    public OpportunityResponse updateStage(Long id, UpdateOpportunityStageRequest request, String actingUsername) {
        Opportunity opportunity = find(id);
        requireOpen(opportunity);
        if (request.getStage() == OpportunityStage.CLOSED_WON || request.getStage() == OpportunityStage.CLOSED_LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Use the win/lose endpoints to close an opportunity");
        }
        OpportunityStage previous = opportunity.getStage();
        opportunity.setStage(request.getStage());
        opportunityRepository.save(opportunity);
        if (previous != request.getStage()) {
            recordActivity(id, OpportunityActivityType.STAGE_CHANGE,
                    "Stage changed from " + previous + " to " + request.getStage(), actingUsername);
        }
        return getOpportunity(id);
    }

    @Override
    @Transactional
    public OpportunityResponse winOpportunity(Long id, String actingUsername) {
        Opportunity opportunity = find(id);
        requireOpen(opportunity);

        if (opportunity.getCustomerId() == null) {
            CreateCustomerRequest customerRequest = new CreateCustomerRequest();
            customerRequest.setCompanyId(opportunity.getCompanyId());
            customerRequest.setName(opportunity.getName());
            customerRequest.setCreditLimit(BigDecimal.ZERO);
            customerRequest.setPaymentTerms(PaymentTerms.NET_30);
            var customer = customerService.createCustomer(customerRequest, actingUsername);
            opportunity.setCustomerId(customer.getId());
        }

        opportunity.setStage(OpportunityStage.CLOSED_WON);
        opportunity.setClosedAt(LocalDateTime.now());
        opportunityRepository.save(opportunity);

        String customerName = customerNameOf(opportunity.getCustomerId());
        recordActivity(id, OpportunityActivityType.WON, "Won — customer " + customerName, actingUsername);
        return getOpportunity(id);
    }

    @Override
    @Transactional
    public OpportunityResponse loseOpportunity(Long id, LoseOpportunityRequest request, String actingUsername) {
        Opportunity opportunity = find(id);
        requireOpen(opportunity);

        opportunity.setStage(OpportunityStage.CLOSED_LOST);
        opportunity.setClosedAt(LocalDateTime.now());
        opportunityRepository.save(opportunity);

        String description = "Lost" + (request.getReason() != null && !request.getReason().isBlank() ? " — " + request.getReason() : "");
        recordActivity(id, OpportunityActivityType.LOST, description, actingUsername);
        return getOpportunity(id);
    }

    @Override
    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opportunity = find(id);
        requireOpen(opportunity);
        opportunityRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpportunityActivityResponse> listActivities(Long opportunityId, OpportunityActivityFilterRequest filter) {
        find(opportunityId);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());
        Page<OpportunityActivity> page = opportunityActivityRepository.findByOpportunityId(opportunityId, pageable);
        return PageResponse.of(page.map(this::toActivityResponse));
    }

    @Override
    @Transactional
    public OpportunityActivityResponse addNote(Long opportunityId, AddOpportunityNoteRequest request, String actingUsername) {
        find(opportunityId);
        OpportunityActivity activity = recordActivity(opportunityId, OpportunityActivityType.NOTE, request.getDescription(), actingUsername);
        return toActivityResponse(activity);
    }

    @Override
    @Transactional
    public OpportunityActivityResponse addFollowUp(Long opportunityId, AddOpportunityFollowUpRequest request, String actingUsername) {
        find(opportunityId);
        OpportunityActivity activity = recordActivity(opportunityId, OpportunityActivityType.FOLLOW_UP, request.getDescription(), actingUsername);
        return toActivityResponse(activity);
    }

    private void requireOpen(Opportunity opportunity) {
        if (opportunity.getStage() == OpportunityStage.CLOSED_WON || opportunity.getStage() == OpportunityStage.CLOSED_LOST) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This opportunity is already closed");
        }
    }

    private OpportunityActivity recordActivity(Long opportunityId, OpportunityActivityType type, String description, String actingUsername) {
        OpportunityActivity activity = OpportunityActivity.builder()
                .opportunityId(opportunityId)
                .type(type)
                .description(description)
                .createdBy(actingUsername)
                .build();
        return opportunityActivityRepository.save(activity);
    }

    private Company requireCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId));
    }

    private void requireLead(Long leadId, Long companyId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Lead not found with id: " + leadId));
        if (!lead.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lead does not belong to the selected company");
        }
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

    private Opportunity find(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Opportunity not found with id: " + id));
    }

    private OpportunityResponse toResponse(Opportunity opportunity, String companyName, String leadContactName,
                                            String customerName, String assignedToUsername) {
        return OpportunityResponse.builder()
                .id(opportunity.getId())
                .companyId(opportunity.getCompanyId())
                .companyName(companyName)
                .leadId(opportunity.getLeadId())
                .leadContactName(leadContactName)
                .customerId(opportunity.getCustomerId())
                .customerName(customerName)
                .name(opportunity.getName())
                .amount(opportunity.getAmount())
                .stage(opportunity.getStage().name())
                .probability(opportunity.getProbability())
                .expectedCloseDate(opportunity.getExpectedCloseDate())
                .assignedToUserId(opportunity.getAssignedToUserId())
                .assignedToUsername(assignedToUsername)
                .notes(opportunity.getNotes())
                .closedAt(opportunity.getClosedAt())
                .createdBy(opportunity.getCreatedBy())
                .build();
    }

    private OpportunityActivityResponse toActivityResponse(OpportunityActivity activity) {
        return OpportunityActivityResponse.builder()
                .id(activity.getId())
                .opportunityId(activity.getOpportunityId())
                .type(activity.getType().name())
                .description(activity.getDescription())
                .createdBy(activity.getCreatedBy())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
