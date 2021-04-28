package ch.admin.bag.covidcode.authcodegeneration.web.controller;

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
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthCodeVerificationControllerV2Test {

    private static final String URL = "/v2/onset";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
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
        AuthorizationCodeVerifyResponseDto fooResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_FOO);
        AuthorizationCodeVerifyResponseDto barResponseDto = new AuthorizationCodeVerifyResponseDto(DUMMY_BAR);
        final var responseDtos = Arrays.asList(fooResponseDto, barResponseDto);
        when(authCodeVerificationService.verify(anyString(), anyString(), anyBoolean())).thenReturn(responseDtos);

        //when
        MvcResult result = mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        final var wrapper = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeVerifyResponseDtoWrapper.class);
        final var expectedDtoList = wrapper.getResponseDtoList();
        assertEquals(2, expectedDtoList.size(), "Should return exactly two tokens for swissCovid and notifyMe backend");
        final var dummies = Arrays.asList(DUMMY_FOO, DUMMY_BAR);
        expectedDtoList.forEach(expectedDto -> {
            assertTrue(dummies.contains(expectedDto.getAccessToken()));
        });
    }

    @Test
    void test_verify_not_found_exception() throws Exception {
        //given
        AuthorizationCodeVerificationDto verificationDto = new AuthorizationCodeVerificationDto(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);

        lenient().when(authCodeVerificationService.verify(anyString(), anyString(), anyBoolean())).thenReturn(null);

        //when
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", TEST_AUTHORIZATION_CODE)
                .content(mapper.writeValueAsString(verificationDto)))
                .andExpect(status().is(404));
    }
}
