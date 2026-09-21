package com.example.erp.service.impl;

import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.CustomerResponse;
import com.example.erp.dto.LoseLeadRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.Lead;
import com.example.erp.entity.LeadSource;
import com.example.erp.entity.LeadStatus;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.LeadActivityRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceImplTest {

    @Mock
    private LeadRepository leadRepository;
    @Mock
    private LeadActivityRepository leadActivityRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private LeadServiceImpl leadService;

    @Test
    void createLead_rejectsAssigneeFromDifferentCompany() {
        lenient().when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(userRepository.findById(99L)).thenReturn(Optional.of(User.builder().id(99L).companyId(2L).build()));

        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Jane");
        request.setSource(LeadSource.WEBSITE);
        request.setAssignedToUserId(99L);

        assertThatThrownBy(() -> leadService.createLead(request, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("does not belong to the selected company");
    }

    @Test
    void createLead_allowsAssigneeWithNoCompany() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(userRepository.findById(99L)).thenReturn(Optional.of(User.builder().id(99L).companyId(null).username("global.admin").build()));
        when(leadRepository.save(any())).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0, Lead.class);
            if (lead.getId() == null) lead.setId(1L);
            return lead;
        });
        lenient().when(leadRepository.findById(1L)).thenAnswer(invocation -> Optional.of(
                Lead.builder().id(1L).companyId(1L).assignedToUserId(99L).source(LeadSource.WEBSITE).build()));

        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Jane");
        request.setSource(LeadSource.WEBSITE);
        request.setAssignedToUserId(99L);

        // Should not throw despite the assignee having no companyId at all.
        assertThatCode(() -> leadService.createLead(request, "tester")).doesNotThrowAnyException();
    }

    @Test
    void winLead_createsCustomer_whenNoneLinkedYet() {
        Lead lead = Lead.builder().id(1L).companyId(1L).contactName("Jane").dealName("Acme deal")
                .status(LeadStatus.NEGOTIATION).source(LeadSource.WEBSITE).build();
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(customerService.createCustomer(any(), any())).thenReturn(CustomerResponse.builder().id(50L).name("Acme deal").build());
        lenient().when(customerRepository.findById(50L)).thenReturn(Optional.empty());

        leadService.winLead(1L, "tester");

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.WON);
        assertThat(lead.getCustomerId()).isEqualTo(50L);
        assertThat(lead.getClosedAt()).isNotNull();
    }

    @Test
    void winLead_rejectsAlreadyClosedLead() {
        Lead lead = Lead.builder().id(1L).companyId(1L).status(LeadStatus.WON).source(LeadSource.WEBSITE).build();
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));

        assertThatThrownBy(() -> leadService.winLead(1L, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already closed");
    }

    @Test
    void loseLead_setsStatusAndRecordsReason() {
        Lead lead = Lead.builder().id(1L).companyId(1L).status(LeadStatus.NEGOTIATION).source(LeadSource.WEBSITE).build();
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));

        LoseLeadRequest request = new LoseLeadRequest();
        request.setReason("Went with a competitor");
        leadService.loseLead(1L, request, "tester");

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.LOST);
        assertThat(lead.getClosedAt()).isNotNull();
    }

    private Lead savedLead;

    @Test
    void aLeadCreatedForABoardColumnStartsInThatColumn() {
        // Dragging a brand-new lead from New to Quotation is the thing this
        // avoids; it also keeps the activity log honest about where it began.
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead saved = invocation.getArgument(0);
            if (saved.getId() == null) saved.setId(9L);
            savedLead = saved;
            return saved;
        });
        // recordActivity reads the lead back.
        when(leadRepository.findById(9L)).thenAnswer(invocation -> Optional.ofNullable(savedLead));

        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Dana");
        request.setSource(LeadSource.WEBSITE);
        request.setStatus(LeadStatus.QUOTATION);

        var response = leadService.createLead(request, "tester");

        assertThat(response.getStatus()).isEqualTo(LeadStatus.QUOTATION.name());
    }

    @Test
    void aLeadCreatedWithNoStatusStillStartsAsNew() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead saved = invocation.getArgument(0);
            if (saved.getId() == null) saved.setId(9L);
            savedLead = saved;
            return saved;
        });
        // recordActivity reads the lead back.
        when(leadRepository.findById(9L)).thenAnswer(invocation -> Optional.ofNullable(savedLead));

        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Dana");
        request.setSource(LeadSource.WEBSITE);

        var response = leadService.createLead(request, "tester");

        assertThat(response.getStatus()).isEqualTo(LeadStatus.NEW.name());
    }

    @Test
    void aLeadCannotBeCreatedAlreadyClosed() {
        // Lead.isEditable() refuses WON/LOST, so a lead created there could
        // never be edited or moved again — a dead row by construction.
        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Dana");
        request.setSource(LeadSource.WEBSITE);
        request.setStatus(LeadStatus.WON);

        assertThatThrownBy(() -> leadService.createLead(request, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("cannot be created directly as WON");
    }
}
