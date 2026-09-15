package com.example.erp.config;

import com.example.erp.security.PermissionAuthorizationManager;
import com.example.erp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final PermissionAuthorizationManager permissionAuthorizationManager;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            // Without an explicit entry point, Spring Security defaults to Http403ForbiddenEntryPoint,
            // which also intercepts the /error dispatch a ResponseStatusException triggers — silently
            // rewriting an intended 401 (bad login) into a bare 403. Permit /error and set a real
            // 401 entry point so unauthenticated requests get the correct REST status.
            .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/error",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Standard unauthenticated health probe path (load balancers, k8s).
                // Other actuator endpoints (metrics) are admin-only below.
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // Uploaded assets (e.g. company logos) are plain <img src> requests from
                // the browser, which don't carry the Authorization bearer token — the
                // files themselves aren't sensitive, so this is public read access.
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/users/**", "/api/files/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // Real per-request enforcement lives in PermissionAuthorizationManager:
                // ADMIN always passes; USER passes only if their assigned custom
                // role grants the (module, action) the request resolves to. Every
                // controller's own @PreAuthorize is loosened to hasAnyRole('ADMIN','USER')
                // so both roles can reach this matcher at all.
                .requestMatchers("/api/admin/**").access(permissionAuthorizationManager)
                .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
