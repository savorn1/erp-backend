package com.example.erp.service;

import com.example.erp.dto.AuthResponse;
import com.example.erp.dto.LoginRequest;
import com.example.erp.dto.LogoutRequest;
import com.example.erp.dto.RefreshRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);
}
