package com.fintech.wallet.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final String headerName;
    private final String apiKeyValue;

    private static final String[] SWAGGER_PATHS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**"
    };

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.isEmpty()) {
            // Fallback to request URI when servlet path is empty (e.g., MockMvc)
            path = request.getRequestURI().substring(request.getContextPath().length());
        }
        log.debug("Checking shouldNotFilter for path: {}", path);
        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : SWAGGER_PATHS) {
            boolean matches = matcher.match(pattern, path);
            log.debug("  Pattern {} matches {}: {}", pattern, path, matches);
            if (matches) {
                log.debug("Path {} matches pattern {}, skipping filter", path, pattern);
                return true;
            }
        }
        log.debug("Path {} does not match any swagger pattern, applying filter", path);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestKey = request.getHeader(headerName);

        if (apiKeyValue.equals(requestKey)) {
            // ถ้า Key ถูกต้อง ให้ผ่านไปได้ (ในที่นี้เราใช้ Simple Auth)
            // สร้าง Authentication object เพื่อให้ Spring Security ยอมรับว่า authenticated
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "api-user",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_API"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            filterChain.doFilter(request, response);
        } else {
            // ถ้าผิด ดีดกลับ 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or Missing API Key");
        }
    }
}