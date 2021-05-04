package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDtoWrapper;
import ch.admin.bag.covidcode.authcodegeneration.config.security.OAuth2SecuredWebConfiguration;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import ch.admin.bag.covidcode.authcodegeneration.testutil.LocalDateSerializer;
import ch.admin.bag.covidcode.authcodegeneration.web.security.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = {AuthCodeVerificationControllerV2.class, OAuth2SecuredWebConfiguration.class, WebSecurityConfig.class})
@ActiveProfiles("local")
class AuthCodeVerificationControllerV2SecurityTest {

    private static final String URL = "/v2/onset";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String FAKE = "0";
    private static final String DUMMY_FOO = "foo";
    private static final String DUMMY_BAR = "bar";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthCodeVerificationService service;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void setup() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        MAPPER.registerModule(module);
    }

    @Test
    void test_verify_authorization_without_token_is_permitted() throws Exception {
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE);
        AuthorizationCodeVerifyResponseDto dp3tResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_FOO);
        AuthorizationCodeVerifyResponseDto checkInResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_BAR);
        final var expectedWrapper = new AuthorizationCodeVerifyResponseDtoWrapper(dp3tResponseDto, checkInResponseDto);
        when(service.verify(anyString(), anyString(), anyBoolean())).thenReturn(expectedWrapper);

        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(MAPPER.writeValueAsString(verificationDto)))
                .andExpect(status().isOk());

        verify(service, times(1)).verify(anyString(), anyString(), anyBoolean());
    }

    @Test
    void test_verify_authorization_without_token_is_permitted_return_404() throws Exception {
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE);
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(MAPPER.writeValueAsString(verificationDto)))
                .andExpect(status().is(404));

        verify(service, times(1)).verify(anyString(), anyString(), anyBoolean());
    }
}
