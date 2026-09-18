package com.example.erp.service.impl;

import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.LeadSource;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.LeadActivityRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.OpportunityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private OpportunityRepository opportunityRepository;
    @Mock
    private OpportunityService opportunityService;

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
        when(leadRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            var lead = invocation.getArgument(0, com.example.erp.entity.Lead.class);
            if (lead.getId() == null) lead.setId(1L);
            return lead;
        });
        lenient().when(leadRepository.findById(1L)).thenAnswer(invocation -> Optional.of(
                com.example.erp.entity.Lead.builder().id(1L).companyId(1L).assignedToUserId(99L).source(LeadSource.WEBSITE).build()));

        CreateLeadRequest request = new CreateLeadRequest();
        request.setCompanyId(1L);
        request.setContactName("Jane");
        request.setSource(LeadSource.WEBSITE);
        request.setAssignedToUserId(99L);

        // Should not throw despite the assignee having no companyId at all.
        assertThatCode(() -> leadService.createLead(request, "tester")).doesNotThrowAnyException();
    }
}
