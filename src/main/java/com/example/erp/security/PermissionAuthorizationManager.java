package com.example.erp.security;

import com.example.erp.entity.PermissionAction;
import com.example.erp.entity.RolePermission;
import com.example.erp.entity.User;
import com.example.erp.repository.RolePermissionRepository;
import com.example.erp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

// Gates every /api/admin/** request (wired in SecurityConfig, replacing the
// old flat hasRole("ADMIN") matcher). ADMIN always passes — this never
// changes what an existing ADMIN account can do. A USER-role account passes
// only if their assigned CustomRole (User.customRoleId) grants the
// (module, action) this request resolves to:
//   module = the URL segment right after /api/admin/, e.g.
//            /api/admin/sales-orders/5/approve -> "sales-orders"
//   action = READ for GET; otherwise WRITE, unless the last path segment is
//            one of APPROVAL_KEYWORDS (or starts with one of
//            APPROVAL_PREFIXES), in which case APPROVE.
// This is a heuristic, not a hand-annotated mapping — see the plan doc for
// why (retrofitting 76 controllers' ~380 endpoints by hand was judged not
// worth the precision gain). A handful of oddly-named endpoints may need
// APPROVAL_KEYWORDS/APPROVAL_PREFIXES extended over time.
@Component
@RequiredArgsConstructor
public class PermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Set<String> APPROVAL_KEYWORDS = Set.of(
            "approve", "reject", "post", "dispose", "cancel", "reverse",
            "close", "reopen", "ship", "complete", "receive", "void", "checkout"
    );
    private static final Set<String> APPROVAL_PREFIXES = Set.of("run-", "seed");

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        if (authorities.contains("ROLE_ADMIN")) {
            return new AuthorizationDecision(true);
        }
        if (!authorities.contains("ROLE_USER")) {
            return new AuthorizationDecision(false);
        }

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || user.getCustomRoleId() == null) {
            return new AuthorizationDecision(false);
        }

        HttpServletRequest request = context.getRequest();
        String module = moduleOf(request.getRequestURI(), request.getContextPath());
        if (module == null) {
            return new AuthorizationDecision(false);
        }
        PermissionAction action = actionOf(request);

        List<RolePermission> grants = rolePermissionRepository.findByCustomRoleId(user.getCustomRoleId());
        boolean granted = grants.stream().anyMatch(g -> g.getModule().equals(module) && g.getAction() == action);
        return new AuthorizationDecision(granted);
    }

    // "/api/admin/sales-orders/5/approve" -> "sales-orders". Null if the path
    // doesn't have a segment after /api/admin/ (shouldn't happen given the
    // matcher this is registered against, but fail closed just in case).
    private String moduleOf(String requestUri, String contextPath) {
        String path = contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)
                ? requestUri.substring(contextPath.length())
                : requestUri;
        String prefix = "/api/admin/";
        if (!path.startsWith(prefix)) return null;
        String rest = path.substring(prefix.length());
        int slash = rest.indexOf('/');
        String module = slash < 0 ? rest : rest.substring(0, slash);
        return module.isBlank() ? null : module;
    }

    private PermissionAction actionOf(HttpServletRequest request) {
        if (HttpMethod.GET.matches(request.getMethod())) {
            return PermissionAction.READ;
        }
        String path = request.getRequestURI();
        int lastSlash = path.lastIndexOf('/');
        String lastSegment = (lastSlash < 0 ? path : path.substring(lastSlash + 1)).toLowerCase();
        if (APPROVAL_KEYWORDS.contains(lastSegment) || APPROVAL_PREFIXES.stream().anyMatch(lastSegment::startsWith)) {
            return PermissionAction.APPROVE;
        }
        return PermissionAction.WRITE;
    }
}
