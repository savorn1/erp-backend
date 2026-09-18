package com.example.erp.service.impl;

import com.example.erp.dto.CreateOpportunityRequest;
import com.example.erp.entity.Company;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.CustomerRepository;
import com.example.erp.repository.LeadRepository;
import com.example.erp.repository.OpportunityActivityRepository;
import com.example.erp.repository.OpportunityRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceImplTest {

    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private OpportunityActivityRepository opportunityActivityRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LeadRepository leadRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private OpportunityServiceImpl opportunityService;

    @Test
    void createOpportunity_rejectsAssigneeFromDifferentCompany() {
        lenient().when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(userRepository.findById(99L)).thenReturn(Optional.of(User.builder().id(99L).companyId(2L).build()));

        CreateOpportunityRequest request = new CreateOpportunityRequest();
        request.setCompanyId(1L);
        request.setName("Big deal");
        request.setAssignedToUserId(99L);

        assertThatThrownBy(() -> opportunityService.createOpportunity(request, "tester"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("does not belong to the selected company");
    }

    @Test
    void createOpportunity_allowsAssigneeWithNoCompany() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));
        when(userRepository.findById(99L)).thenReturn(Optional.of(User.builder().id(99L).companyId(null).username("global.admin").build()));
        when(opportunityRepository.save(any())).thenAnswer(invocation -> {
            var opportunity = invocation.getArgument(0, com.example.erp.entity.Opportunity.class);
            if (opportunity.getId() == null) opportunity.setId(1L);
            return opportunity;
        });
        lenient().when(opportunityRepository.findById(1L)).thenAnswer(invocation -> Optional.of(
                com.example.erp.entity.Opportunity.builder().id(1L).companyId(1L).assignedToUserId(99L).build()));

        CreateOpportunityRequest request = new CreateOpportunityRequest();
        request.setCompanyId(1L);
        request.setName("Big deal");
        request.setAssignedToUserId(99L);

        assertThatCode(() -> opportunityService.createOpportunity(request, "tester")).doesNotThrowAnyException();
    }
}
