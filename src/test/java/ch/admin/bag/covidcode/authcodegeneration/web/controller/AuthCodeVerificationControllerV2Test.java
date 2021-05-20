package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeOnsetResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDtoWrapper;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthCodeVerificationControllerV2Test {

    private static final String URL = "/v2/onset";
    private static final String URL_ONSET = "/v2/onset/date";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String TEST_ONSET_DATE = "1970-01-01";
    private static final String DUMMY_FOO = "foo";
    private static final String DUMMY_BAR = "bar";
    private static final String FAKE_NOT_FAKE = "0";

    @Mock
    private AuthCodeVerificationService authCodeVerificationService;

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = standaloneSetup(new AuthCodeVerificationControllerV2(authCodeVerificationService, 0)).build();
    }

    @Test
    void test_verify() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        AuthorizationCodeVerifyResponseDto dp3tResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_FOO);
        AuthorizationCodeVerifyResponseDto checkInResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_BAR);
        final var expectedWrapper = new AuthorizationCodeVerifyResponseDtoWrapper(dp3tResponseDto, checkInResponseDto);
        when(authCodeVerificationService.verify(anyString(), anyString(), anyBoolean())).thenReturn(expectedWrapper);

        //when
        MvcResult result = mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final var actualWrapper = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeVerifyResponseDtoWrapper.class);
        final var dp3tAccessToken = actualWrapper.getDP3TAccessToken();
        final var checkInAccessToken = actualWrapper.getCheckInAccessToken();
        assertNotNull(dp3tAccessToken, "Should return exactly two tokens for swissCovid and notifyMe backend");
        assertNotNull(checkInAccessToken, "Should return exactly two tokens for swissCovid and notifyMe backend");
        assertEquals(DUMMY_FOO, dp3tAccessToken.getAccessToken());
        assertEquals(DUMMY_BAR, checkInAccessToken.getAccessToken());
    }

    @Test
    void test_verify_not_found_exception() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);

        lenient().when(authCodeVerificationService.verify(anyString(), anyString(), anyBoolean())).thenReturn(new AuthorizationCodeVerifyResponseDtoWrapper());

        //when
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().is(404));
    }

    @Test
    void test_getOnset() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        final var expectedResponse = new AuthorizationCodeOnsetResponseDto(TEST_ONSET_DATE);
        when(authCodeVerificationService.getOnsetForAuthCode(anyString(), anyString())).thenReturn(expectedResponse);

        //when
        MvcResult result = mockMvc.perform(post(URL_ONSET)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final var actualResponse = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeOnsetResponseDto.class);
        final var onset = actualResponse.getOnset();
        assertNotNull(onset, "Should return a non-null onset date");
        assertEquals(TEST_ONSET_DATE, onset);
    }

    @Test
    void test_getOnset_not_found_exception() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);

        when(authCodeVerificationService.getOnsetForAuthCode(anyString(), anyString())).thenReturn(new AuthorizationCodeOnsetResponseDto(null));

        //when
        mockMvc.perform(post(URL_ONSET)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().is(404));
    }
}
