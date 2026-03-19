package com.fintech.wallet.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // ช่วยให้เราจำลอง HTTP Request ได้โดยไม่ต้องรัน Server จริงบน Port
class WalletSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.api.key}")
    private String validApiKey;

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
    @DisplayName("3. เรียก API ด้วย Key ที่ผิด - ต้องติด 401 Unauthorized")
    void callApi_WithInvalidKey_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/12345")
                .header("X-API-KEY", "wrong-key-1234"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. เรียก API ด้วย Key ที่ถูกต้อง - ต้องผ่านเข้าสู่ระบบได้")
    void callApi_WithValidKey_ShouldReturnOkOrNotFound() throws Exception {
        // หมายเหตุ: ตรงนี้ .isOk() หรือ .isNotFound() ขึ้นอยู่กับว่ามีข้อมูลจริงไหม 
        // แต่ประเด็นคือต้องไม่ติด 401
        mockMvc.perform(get("/api/v1/wallets/non-exist-account")
                .header("X-API-KEY", validApiKey))
                .andExpect(status().isNotFound()); 
    }
}