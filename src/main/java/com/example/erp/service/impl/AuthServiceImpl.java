package com.example.erp.service.impl;

import com.example.erp.dto.AuthResponse;
import com.example.erp.dto.LoginRequest;
import com.example.erp.dto.LogoutRequest;
import com.example.erp.dto.RefreshRequest;
import com.example.erp.entity.RefreshToken;
import com.example.erp.entity.User;
import com.example.erp.exception.AppException;
import com.example.erp.repository.RefreshTokenRepository;
import com.example.erp.repository.UserRepository;
import com.example.erp.service.AuditLogService;
import com.example.erp.service.AuthService;
import com.example.erp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        auditLogService.record(user.getCompanyId(), null, "LOGIN", "POST", "/api/auth/login",
                user.getId(), user.getUsername(), 200, "Login: " + user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!user.isEnabled()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        // Rotate: the presented refresh token is single-use.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(stored -> {
                    stored.setRevoked(true);
                    refreshTokenRepository.save(stored);
                    userRepository.findById(stored.getUserId()).ifPresent(user ->
                            auditLogService.record(user.getCompanyId(), null, "LOGOUT", "POST", "/api/auth/logout",
                                    user.getId(), user.getUsername(), 200, "Logout: " + user.getUsername()));
                });
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = issueRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    private String issueRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)))
                .build());
        return token;
    }
}
