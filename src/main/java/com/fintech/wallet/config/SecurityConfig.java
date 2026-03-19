package com.fintech.wallet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.api.key}")
    private String principalRequestValue;

    private static final String principalRequestHeader = "X-API-KEY";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    ApiKeyFilter filter = new ApiKeyFilter(principalRequestHeader, principalRequestValue);

    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // --- ส่วนที่ต้องแก้ไข/เพิ่ม ---
            .requestMatchers(
                "/v3/api-docs/**",    // ตัวดึง JSON โครงสร้าง API
                "/swagger-ui/**",     // ตัวหน้าเว็บ UI
                "/swagger-ui.html",   // ทางเข้าหลัก
                "/swagger-resources/**",
                "/webjars/**"
            ).permitAll()             // อนุญาตให้เข้าได้โดยไม่ต้องมี Key
            // ---------------------------
            .anyRequest().authenticated() // ที่เหลือ (เช่น /api/v1/...) ต้องมี Key เท่านั้น
        )
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}