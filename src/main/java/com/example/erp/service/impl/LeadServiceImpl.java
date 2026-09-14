package com.example.erp.service.impl;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.CreateOpportunityRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateLeadRequest;
import com.example.erp.dto.UpdateLeadStatusRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Lead;
import com.example.erp.entity.LeadActivity;
import com.example.erp.entity.LeadActivityType;
import com.example.erp.entity.LeadStatus;
import com.example.erp.entity.Opportunity;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.LeadActivityRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.LeadService;
import com.example.erp.service.OpportunityService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OpportunityRepository opportunityRepository;
    private final OpportunityService opportunityService;

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
        Map<Long, String> opportunityNames = opportunityRepository.findAllById(
                content.stream().map(Lead::getConvertedOpportunityId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Opportunity::getId, Opportunity::getName));

        return PageResponse.of(page.map(lead -> toResponse(lead,
                companyNames.get(lead.getCompanyId()),
                lead.getAssignedToUserId() == null ? null : usernames.get(lead.getAssignedToUserId()),
                lead.getConvertedOpportunityId() == null ? null : opportunityNames.get(lead.getConvertedOpportunityId()))));
    }

    @Override
    public LeadResponse getLead(Long id) {
        Lead lead = find(id);
        return toResponse(lead,
                companyNameOf(lead.getCompanyId()),
                lead.getAssignedToUserId() == null ? null : usernameOf(lead.getAssignedToUserId()),
                lead.getConvertedOpportunityId() == null ? null : opportunityNameOf(lead.getConvertedOpportunityId()));
    }

    @Override
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, String actingUsername) {
        requireCompany(request.getCompanyId());
        if (request.getAssignedToUserId() != null) {
            requireUser(request.getAssignedToUserId());
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
                .createdBy(actingUsername)
                .build();
        leadRepository.save(lead);

        recordActivity(lead.getId(), LeadActivityType.CREATED, "Lead created", actingUsername);
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
        requireCompany(request.getCompanyId());

        lead.setCompanyId(request.getCompanyId());
        lead.setContactName(request.getContactName());
        lead.setOrganizationName(request.getOrganizationName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setSource(request.getSource());
        lead.setEstimatedValue(request.getEstimatedValue());
        lead.setNotes(request.getNotes());
        leadRepository.save(lead);
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse updateStatus(Long id, UpdateLeadStatusRequest request, String actingUsername) {
        Lead lead = find(id);
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A converted lead's status cannot be changed further");
        }
        if (request.getStatus() == LeadStatus.CONVERTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Use the convert endpoint to convert a lead");
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
            requireUser(request.getAssignedToUserId());
            description = "Assigned to " + usernameOf(request.getAssignedToUserId());
        }
        lead.setAssignedToUserId(request.getAssignedToUserId());
        leadRepository.save(lead);
        recordActivity(id, LeadActivityType.ASSIGNED, description, actingUsername);
        return getLead(id);
    }

    @Override
    @Transactional
    public LeadResponse convertLead(Long id, String actingUsername) {
        Lead lead = find(id);
        if (lead.getStatus() == LeadStatus.CONVERTED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lead has already been converted");
        }
        if (lead.getStatus() != LeadStatus.QUALIFIED && lead.getStatus() != LeadStatus.PROPOSAL
                && lead.getStatus() != LeadStatus.NEGOTIATION) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Lead must be qualified before it can be converted to an opportunity");
        }

        CreateOpportunityRequest opportunityRequest = new CreateOpportunityRequest();
        opportunityRequest.setCompanyId(lead.getCompanyId());
        opportunityRequest.setLeadId(lead.getId());
        opportunityRequest.setName(lead.getOrganizationName() != null && !lead.getOrganizationName().isBlank()
                ? lead.getOrganizationName() : lead.getContactName());
        opportunityRequest.setAmount(lead.getEstimatedValue());
        opportunityRequest.setAssignedToUserId(lead.getAssignedToUserId());
        var opportunity = opportunityService.createOpportunity(opportunityRequest, actingUsername);

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedOpportunityId(opportunity.getId());
        lead.setConvertedAt(LocalDateTime.now());
        leadRepository.save(lead);

        recordActivity(id, LeadActivityType.CONVERTED, "Converted to opportunity " + opportunity.getName(), actingUsername);
        return getLead(id);
    }

    @Override
    @Transactional
    public void deleteLead(Long id) {
        find(id);
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
    public LeadActivityResponse addFollowUp(Long leadId, AddLeadFollowUpRequest request, String actingUsername) {
        find(leadId);
        LeadActivity activity = recordActivity(leadId, LeadActivityType.FOLLOW_UP, request.getDescription(), actingUsername);
        return toActivityResponse(activity);
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

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));
    }

    private String companyNameOf(Long companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse(null);
    }

    private String usernameOf(Long userId) {
        return userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private String opportunityNameOf(Long opportunityId) {
        return opportunityRepository.findById(opportunityId).map(Opportunity::getName).orElse(null);
    }

    private Lead find(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Lead not found with id: " + id));
    }

    private LeadResponse toResponse(Lead lead, String companyName, String assignedToUsername, String convertedOpportunityName) {
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
                .convertedOpportunityId(lead.getConvertedOpportunityId())
                .convertedOpportunityName(convertedOpportunityName)
                .convertedAt(lead.getConvertedAt())
                .createdBy(lead.getCreatedBy())
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
