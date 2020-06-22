package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthCodeVerificationControllerTest {

    private static final String URL = "/v1/onset";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String DUMMY_STR = "test";
    private static final String FAKE_NOT_FAKE = "0";

    @Mock
    private AuthCodeVerificationService authCodeVerificationService;

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = standaloneSetup(new AuthCodeVerificationController(authCodeVerificationService, 0)).build();
    }

    @Test
    void test_verify() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        AuthorizationCodeVerifyResponseDto responseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_STR);

        when(authCodeVerificationService.verify(anyString(), anyString())).thenReturn(responseDto);

        //when
        MvcResult result = mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", DUMMY_STR)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        AuthorizationCodeVerifyResponseDto expectedDto = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeVerifyResponseDto.class);
        assertEquals(DUMMY_STR, expectedDto.getAccessToken());
    }

    @Test
    void test_verify_not_found_exception() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);

        when(authCodeVerificationService.verify(anyString(), anyString())).thenThrow(new ResourceNotFoundException());

        //when
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", DUMMY_STR)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().is(404));
    }
}
