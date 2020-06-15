package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcode.authcodegeneration.config.security.OAuth2SecuredWebConfiguration;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeGenerationService;
import ch.admin.bag.covidcode.authcodegeneration.testutil.JwtTestUtil;
import ch.admin.bag.covidcode.authcodegeneration.testutil.KeyPairTestUtil;
import ch.admin.bag.covidcode.authcodegeneration.testutil.LocalDateSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {AuthCodeGenerationController.class, OAuth2SecuredWebConfiguration.class},
            properties="jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri=http://localhost:8182/.well-known/jwks.json")  // Avoid port 8180, see below
@ActiveProfiles("local")
class AuthCodeGenerationControllerSecurityTest {

    private static final String URL = "/v1/authcode";
    private static final String VALID_USER_ROLE = "bag-pts-allow";
    private static final String INVALID_USER_ROLE = "invalid-role";
    // Avoid port 8180, which is likely used by the local KeyCloak:
    private static final int MOCK_SERVER_PORT = 8182;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthCodeGenerationService service;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final KeyPairTestUtil KEY_PAIR_TEST_UTIL = new KeyPairTestUtil();
    private static final String PRIVATE_KEY = KEY_PAIR_TEST_UTIL.getPrivateKey();
    private static final LocalDateTime EXPIRED_IN_FUTURE = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime EXPIRED_IN_PAST = LocalDateTime.now().minusDays(1);

    private static WireMockServer wireMockServer =
        // new WireMockServer(options().dynamicPort());
        new WireMockServer(options().port(MOCK_SERVER_PORT));

    @BeforeAll
    static void setup() throws Exception {
        wireMockServer.start();
        wireMockServer.stubFor(get(urlPathEqualTo("/.well-known/jwks.json")).willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", "application/json")
                .withBody(KEY_PAIR_TEST_UTIL.getJwks())));
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        MAPPER.registerModule(module);
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void test_create_authorization_with_valid_token() throws Exception {
        test_call_create_with_token(EXPIRED_IN_FUTURE, VALID_USER_ROLE, HttpStatus.OK);
        verify(service, times(1)).create(any(AuthorizationCodeCreateDto.class));
    }

    @Test
    void test_create_authorization_with_valid_token_but_wrong_userrole() throws Exception {
        test_call_create_with_token(EXPIRED_IN_FUTURE, INVALID_USER_ROLE, HttpStatus.FORBIDDEN);
        verify(service, times(0)).create(any(AuthorizationCodeCreateDto.class));
    }

    @Test
    void test_create_authorization_with_expired_token() throws Exception {
        test_call_create_with_token(EXPIRED_IN_PAST, VALID_USER_ROLE, HttpStatus.UNAUTHORIZED);
        verify(service, times(0)).create(any(AuthorizationCodeCreateDto.class));
    }


    private void test_call_create_with_token(LocalDateTime tokenExpiration, String userRole, HttpStatus status) throws Exception {
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now());
        String token = JwtTestUtil.getJwtTestToken(PRIVATE_KEY, tokenExpiration, userRole);
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + token)
                .content(MAPPER.writeValueAsString(createDto)))
                .andExpect(getResultMatcher(status));
    }

    private ResultMatcher getResultMatcher(HttpStatus status) {
        switch(status) {
            case OK:
                return status().isOk();
            case FORBIDDEN:
                return status().isForbidden();
            case UNAUTHORIZED:
                return status().isUnauthorized();
            default:
                throw new IllegalArgumentException("HttpStatus not found!");
        }
    }
}
