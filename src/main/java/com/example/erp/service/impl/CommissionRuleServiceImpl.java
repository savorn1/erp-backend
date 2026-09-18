package com.example.erp.service.impl;

import com.example.erp.dto.CommissionRuleFilterRequest;
import com.example.erp.dto.CommissionRuleRequest;
import com.example.erp.dto.CommissionRuleResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.entity.Company;
import com.example.erp.entity.CommissionRule;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.CommissionRuleRepository;
import com.example.erp.repository.CompanyRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.CommissionRuleService;
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
public class CommissionRuleServiceImpl implements CommissionRuleService {

    private final CommissionRuleRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommissionRuleResponse> list(CommissionRuleFilterRequest filter) {
        List<Specification<CommissionRule>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        }
        if (filter.getUserId() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("userId"), filter.getUserId()));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<CommissionRule> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<CommissionRule> page = repository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    public CommissionRuleResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public CommissionRuleResponse create(CommissionRuleRequest request) {
        requireCompany(request.getCompanyId());
        if (request.getUserId() != null) {
            requireUser(request.getUserId(), request.getCompanyId());
            if (repository.existsByCompanyIdAndUserId(request.getCompanyId(), request.getUserId())) {
                throw new AppException(HttpStatus.CONFLICT, "A commission rule already exists for this sales rep");
            }
        } else if (repository.existsByCompanyIdAndUserIdIsNull(request.getCompanyId())) {
            throw new AppException(HttpStatus.CONFLICT, "A default commission rule already exists for this company");
        }

        CommissionRule rule = CommissionRule.builder()
                .companyId(request.getCompanyId())
                .userId(request.getUserId())
                .ratePercent(request.getRatePercent())
                .active(request.isActive())
                .build();
        repository.save(rule);
        return toResponse(rule);
    }

    @Override
    @Transactional
    public CommissionRuleResponse update(Long id, CommissionRuleRequest request) {
        CommissionRule rule = find(id);
        requireCompany(request.getCompanyId());
        if (request.getUserId() != null) {
            requireUser(request.getUserId(), request.getCompanyId());
            if (repository.existsByCompanyIdAndUserIdAndIdNot(request.getCompanyId(), request.getUserId(), id)) {
                throw new AppException(HttpStatus.CONFLICT, "A commission rule already exists for this sales rep");
            }
        } else if (repository.existsByCompanyIdAndUserIdIsNullAndIdNot(request.getCompanyId(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "A default commission rule already exists for this company");
        }

        rule.setCompanyId(request.getCompanyId());
        rule.setUserId(request.getUserId());
        rule.setRatePercent(request.getRatePercent());
        rule.setActive(request.isActive());
        repository.save(rule);
        return toResponse(rule);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private CommissionRule find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Commission rule not found with id: " + id));
    }

    private void requireCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Company not found with id: " + companyId);
        }
    }

    private void requireUser(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));
        if (user.getCompanyId() != null && !user.getCompanyId().equals(companyId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Sales rep does not belong to the selected company");
        }
    }

    private CommissionRuleResponse toResponse(CommissionRule rule) {
        String companyName = companyRepository.findById(rule.getCompanyId()).map(Company::getName).orElse(null);
        String userName = rule.getUserId() == null ? null
                : userRepository.findById(rule.getUserId()).map(User::getUsername).orElse(null);
        return CommissionRuleResponse.builder()
                .id(rule.getId())
                .companyId(rule.getCompanyId())
                .companyName(companyName)
                .userId(rule.getUserId())
                .userName(userName)
                .ratePercent(rule.getRatePercent())
                .active(rule.isActive())
                .build();
    }
}
