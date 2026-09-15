package com.example.erp.service.impl;

import com.example.erp.dto.ClosePosSessionRequest;
import com.example.erp.dto.OpenPosSessionRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosSessionFilterRequest;
import com.example.erp.dto.PosSessionResponse;
import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PosSale;
import com.example.erp.entity.PosSaleStatus;
import com.example.erp.entity.PosSession;
import com.example.erp.entity.PosSessionStatus;
import com.example.erp.entity.Register;
import com.example.erp.exception.AppException;
import com.example.erp.repository.PosPaymentLineRepository;
import com.example.erp.repository.PosSaleRepository;
import com.example.erp.repository.PosSessionRepository;
import com.example.erp.repository.RegisterRepository;
import com.example.erp.service.AutoPostingService;
import com.example.erp.service.PosSessionService;
import com.example.erp.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PosSessionServiceImpl implements PosSessionService {

    private final PosSessionRepository posSessionRepository;
    private final RegisterRepository registerRepository;
    private final PosSaleRepository posSaleRepository;
    private final PosPaymentLineRepository posPaymentLineRepository;
    private final AutoPostingService autoPostingService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PosSessionResponse> list(PosSessionFilterRequest filter) {
        List<Specification<PosSession>> conditions = new ArrayList<>();
        if (filter.getCompanyId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("companyId"), filter.getCompanyId()));
        if (filter.getRegisterId() != null) conditions.add((root, query, cb) -> cb.equal(root.get("registerId"), filter.getRegisterId()));
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            conditions.add((root, query, cb) -> cb.equal(root.get("status"), PosSessionStatus.valueOf(filter.getStatus())));
        }
        Specification<PosSession> spec = Specification.allOf(conditions);
        Pageable pageable = PageableUtils.of(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getSortOrder());

        Page<PosSession> page = posSessionRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PosSessionResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public PosSessionResponse open(OpenPosSessionRequest request, String actingUsername) {
        Register register = registerRepository.findById(request.getRegisterId())
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "Register not found with id: " + request.getRegisterId()));
        if (!register.isActive()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This register is inactive");
        }
        if (posSessionRepository.findByRegisterIdAndStatus(register.getId(), PosSessionStatus.OPEN).isPresent()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This register already has an open session");
        }
        PosSession session = PosSession.builder()
                .registerId(register.getId())
                .companyId(register.getCompanyId())
                .openedBy(actingUsername)
                .openedAt(LocalDateTime.now())
                .openingFloat(request.getOpeningFloat())
                .build();
        return toResponse(posSessionRepository.save(session));
    }

    @Override
    @Transactional
    public PosSessionResponse close(Long id, ClosePosSessionRequest request, String actingUsername) {
        PosSession session = find(id);
        if (session.getStatus() != PosSessionStatus.OPEN) {
            throw new AppException(HttpStatus.BAD_REQUEST, "This session is already closed");
        }

        BigDecimal expectedCash = session.getOpeningFloat().add(cashTenderedForSession(session.getId()));
        BigDecimal variance = request.getCountedCash().subtract(expectedCash);

        session.setCountedCash(request.getCountedCash());
        session.setExpectedCash(expectedCash);
        session.setCashVariance(variance);
        session.setStatus(PosSessionStatus.CLOSED);
        session.setClosedBy(actingUsername);
        session.setClosedAt(LocalDateTime.now());
        posSessionRepository.save(session);

        if (variance.signum() != 0) {
            autoPostingService.postCashVariance(session.getCompanyId(), LocalDate.now(), variance, session.getId(), actingUsername);
        }
        return toResponse(session);
    }

    // Cash actually tendered on this session's still-COMPLETED sales — a
    // voided sale's cash was handed back to the customer, so it shouldn't
    // still be expected in the drawer.
    private BigDecimal cashTenderedForSession(Long sessionId) {
        List<Long> completedSaleIds = posSaleRepository.findByPosSessionId(sessionId).stream()
                .filter(s -> s.getStatus() == PosSaleStatus.COMPLETED)
                .map(PosSale::getId)
                .toList();
        if (completedSaleIds.isEmpty()) return BigDecimal.ZERO;
        return posPaymentLineRepository.findByPosSaleIdInAndMethod(completedSaleIds, PaymentMethod.CASH).stream()
                .map(com.example.erp.entity.PosPaymentLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PosSessionResponse toResponse(PosSession session) {
        Register register = registerRepository.findById(session.getRegisterId()).orElse(null);
        BigDecimal salesTotalSoFar = posSaleRepository.findByPosSessionId(session.getId()).stream()
                .filter(s -> s.getStatus() == PosSaleStatus.COMPLETED)
                .map(PosSale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return PosSessionResponse.builder()
                .id(session.getId())
                .registerId(session.getRegisterId())
                .registerName(register == null ? null : register.getName())
                .companyId(session.getCompanyId())
                .openedBy(session.getOpenedBy())
                .openedAt(session.getOpenedAt())
                .openingFloat(session.getOpeningFloat())
                .status(session.getStatus().name())
                .closedBy(session.getClosedBy())
                .closedAt(session.getClosedAt())
                .countedCash(session.getCountedCash())
                .expectedCash(session.getExpectedCash())
                .cashVariance(session.getCashVariance())
                .salesTotalSoFar(salesTotalSoFar)
                .build();
    }

    private PosSession find(Long id) {
        return posSessionRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "POS session not found with id: " + id));
    }
}
