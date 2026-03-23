package com.fintech.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.wallet.dto.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // ช่วยให้เราจำลอง HTTP Request ได้โดยไม่ต้องรัน Server จริงบน Port
class WalletSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("1. เข้าหน้า Swagger - ต้องเข้าได้โดยไม่ต้องมี Key (PermitAll)")
    void accessSwagger_ShouldBeAllowed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. เรียก API ทั่วไปโดยไม่มี Header - ต้องติด 401 Unauthorized")
    void callApi_WithoutHeader_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/12345"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3. เรียก API ด้วย Token ที่ผิด - ต้องติด 401 Unauthorized")
    void callApi_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/12345")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. เรียก API ด้วย Token ที่ถูกต้อง - ต้องผ่านเข้าสู่ระบบได้")
    void callApi_WithValidToken_ShouldReturnOkOrNotFound() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/v1/wallets/non-exist-account")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5. Login ด้วย username/password ถูกต้อง - ต้องได้ token")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("6. Login ด้วย username/password ผิด - ต้องติด 401")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String obtainToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(content, LoginResponse.class);
        return loginResponse.getToken();
    }
}