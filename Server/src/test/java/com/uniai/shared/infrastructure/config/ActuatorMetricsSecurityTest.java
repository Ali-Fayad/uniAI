package com.uniai.shared.infrastructure.config;

import com.uniai.shared.infrastructure.jwt.JwtTokenPayload;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.uniai.shared.infrastructure.jwt.JwtFilter;
import org.springframework.security.web.FilterChainProxy;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ActuatorLoggerSecurityTest.TestApp.class)
@TestPropertySource(properties = {
        "management.endpoints.web.exposure.include=health,loggers,metrics",
        "management.health.mail.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "jwt.secret=Zm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFyZm9vYmFy",
        "spring.flyway.enabled=false"
})
class ActuatorMetricsSecurityTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void anonymousUsersShouldBeDeniedFromMetricsManagement() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/metrics/jvm.memory.used"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedNonAdminUsersShouldBeForbiddenFromMetricsManagement() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", bearerToken("user@example.com", "USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/metrics/jvm.memory.used")
                        .header("Authorization", bearerToken("user@example.com", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUsersShouldAccessMetricsManagement() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .header("Authorization", bearerToken("admin@example.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("\"names\"")));

        mockMvc.perform(get("/actuator/metrics/jvm.memory.used")
                        .header("Authorization", bearerToken("admin@example.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("\"name\"")));
    }

    private String bearerToken(String email, String role) {
        JwtTokenPayload payload = JwtTokenPayload.builder()
                .username(email.split("@")[0])
                .email(email)
                .role(role)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
        return "Bearer " + jwtUtil.generateToken(payload);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            DataJpaRepositoriesAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @Import(SecurityConfig.class)
    static class TestApp {

        @Bean
        JwtUtil jwtUtil() throws Exception {
            JwtUtil jwtUtil = new JwtUtil();

            Field secretField = JwtUtil.class.getDeclaredField("secretKeyBase64");
            secretField.setAccessible(true);
            secretField.set(jwtUtil, Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes()));

            Field expirationField = JwtUtil.class.getDeclaredField("expirationMs");
            expirationField.setAccessible(true);
            expirationField.setLong(jwtUtil, 86400000L);

            jwtUtil.init();
            return jwtUtil;
        }

        @Bean
        JwtFilter jwtFilter(JwtUtil jwtUtil) {
            return new JwtFilter(jwtUtil);
        }
    }
}
