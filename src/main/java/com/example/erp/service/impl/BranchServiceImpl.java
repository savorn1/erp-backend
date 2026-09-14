package com.example.erp.service.impl;

import com.example.erp.dto.BranchFilterRequest;
import com.example.erp.dto.BranchResponse;
import com.example.erp.dto.CreateBranchRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateBranchRequest;
import com.example.erp.dto.UpdateBranchStatusRequest;
import com.example.erp.entity.Branch;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.BranchRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.BranchService;
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
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BranchResponse> listBranches(BranchFilterRequest filter) {
        List<Specification<Branch>> conditions = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            conditions.add((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        }
        if (filter.getActive() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("active"), filter.getActive()));
        }
        Specification<Branch> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<Branch> branches = branchRepository.findAll(spec, pageable);

        List<Long> managerIds = branches.getContent().stream()
                .map(Branch::getManagerId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, String> managerUsernames = userRepository.findAllById(managerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return PageResponse.of(branches.map(branch -> toResponse(branch,
                branch.getManagerId() == null ? null : managerUsernames.get(branch.getManagerId()))));
    }

    @Override
    public BranchResponse getBranch(Long id) {
        Branch branch = findBranch(id);
        return toResponse(branch, usernameOf(branch.getManagerId()));
    }

    @Override
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (branchRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Branch name already taken: " + request.getName());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .managerId(request.getManagerId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .timezone(request.getTimezone())
                .build();
        branchRepository.save(branch);
        return toResponse(branch, manager == null ? null : manager.getUsername());
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = findBranch(id);
        User manager = request.getManagerId() == null ? null : requireManager(request.getManagerId());
        if (branchRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(HttpStatus.CONFLICT, "Branch name already taken: " + request.getName());
        }

        branch.setName(request.getName());
        branch.setAddressLine1(request.getAddressLine1());
        branch.setAddressLine2(request.getAddressLine2());
        branch.setCity(request.getCity());
        branch.setState(request.getState());
        branch.setPostalCode(request.getPostalCode());
        branch.setCountry(request.getCountry());
        branch.setManagerId(request.getManagerId());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());
        branch.setTimezone(request.getTimezone());
        branchRepository.save(branch);
        return toResponse(branch, manager == null ? null : manager.getUsername());
    }

    @Override
    @Transactional
    public BranchResponse updateStatus(Long id, UpdateBranchStatusRequest request) {
        Branch branch = findBranch(id);
        branch.setActive(request.getActive());
        branchRepository.save(branch);
        return toResponse(branch, usernameOf(branch.getManagerId()));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        branchRepository.delete(findBranch(id));
    }

    private User requireManager(Long managerId) {
        return userRepository.findById(managerId)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Manager not found with id: " + managerId));
    }

    private String usernameOf(Long userId) {
        return userId == null ? null : userRepository.findById(userId).map(User::getUsername).orElse(null);
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Branch not found with id: " + id));
    }

    private BranchResponse toResponse(Branch branch, String managerUsername) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .addressLine1(branch.getAddressLine1())
                .addressLine2(branch.getAddressLine2())
                .city(branch.getCity())
                .state(branch.getState())
                .postalCode(branch.getPostalCode())
                .country(branch.getCountry())
                .managerId(branch.getManagerId())
                .managerUsername(managerUsername)
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .timezone(branch.getTimezone())
                .active(branch.isActive())
                .build();
    }
}
