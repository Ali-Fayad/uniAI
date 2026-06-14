package com.uniai.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.admin.application.dto.command.UpdateAdminUserRoleCommand;
import com.uniai.shared.infrastructure.jwt.JwtTokenPayload;
import com.uniai.shared.infrastructure.jwt.JwtUtil;
import com.uniai.support.PostgresIntegrationTest;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.valueobject.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminSecurityWebTest extends PostgresIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE messages, chats, feedback, verification_code, personal_info, cvs, users
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void userTokenShouldBeRejectedFromAdminEndpoints() throws Exception {
        HttpResponse<String> response = send("GET", "/api/admin/health", null, "user@example.com", UserRole.USER);
        assertEquals(403, response.statusCode());
    }

    @Test
    void unauthenticatedRequestShouldBeRejectedFromAdminEndpoints() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/admin/health")))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode());
    }

    @Test
    void adminTokenShouldAccessAdminEndpoints() throws Exception {
        HttpResponse<String> response = send("GET", "/api/admin/overview", null, "admin@example.com", UserRole.ADMIN);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"totalUsers\":0"));
    }

    @Test
    void adminTokenShouldRejectMissingRoleInPatchBody() throws Exception {
        HttpResponse<String> response = send("PATCH", "/api/admin/users/1/role", "{}", "admin@example.com", UserRole.ADMIN);
        assertEquals(400, response.statusCode());
    }

    @Test
    void adminTokenShouldRejectInvalidRoleInPatchBody() throws Exception {
        HttpResponse<String> response = send("PATCH", "/api/admin/users/1/role", "{\"role\":\"SUPERADMIN\"}", "admin@example.com", UserRole.ADMIN);
        assertEquals(400, response.statusCode());
    }

    @Test
    void selfDemotionShouldBeBlockedThroughTheEndpoint() throws Exception {
        User admin = userRepository.save(User.builder()
                .email("admin@example.com")
                .username("admin")
                .password("encoded-password")
                .role(UserRole.ADMIN)
                .isVerified(true)
                .isTwoFacAuth(false)
                .build());

        HttpResponse<String> response = send("PATCH", "/api/admin/users/" + admin.getId() + "/role",
                objectMapper.writeValueAsString(new UpdateAdminUserRoleCommand(UserRole.USER)),
                "admin@example.com", UserRole.ADMIN);
        assertEquals(403, response.statusCode());
    }

    @Test
    void deleteEndpointShouldRequireAdminAuthorization() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/admin/users/1")))
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(403, response.statusCode());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> send(String method, String path, String body, String email, UserRole role) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Authorization", bearerToken(email, role))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        switch (method) {
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
            case "PATCH" -> builder.method("PATCH", body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        }

        if (!"PATCH".equals(method)) {
            if (body == null) {
                // already configured by GET/DELETE
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            }
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String bearerToken(String email, UserRole role) {
        JwtTokenPayload payload = JwtTokenPayload.builder()
                .username(email.split("@")[0])
                .email(email)
                .role(role.name())
                .isVerified(true)
                .isTwoFacAuth(false)
                .build();
        return "Bearer " + jwtUtil.generateToken(payload);
    }
}
