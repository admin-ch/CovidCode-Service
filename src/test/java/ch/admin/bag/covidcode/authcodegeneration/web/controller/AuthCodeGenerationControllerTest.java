package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeGenerationService;
import ch.admin.bag.covidcode.authcodegeneration.testutil.LocalDateSerializer;
import ch.admin.bag.covidcode.authcodegeneration.testutil.LoggerTestUtil;
import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.JeapAuthenticationToken;
import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.ServletJeapAuthorization;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthCodeGenerationControllerTest {

    private static final String URL = "/v1/authcode";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String DUMMY_STR = "test";
    private static final String DISPLAY_NAME_STR = "displayName";
    private static final Jwt JWT = Jwt.withTokenValue(DUMMY_STR)
            .header(DUMMY_STR, null).claim(DUMMY_STR, null).build();
    private static final Jwt JWT_WITH_CLAIM_DISPLAY_NAME = Jwt.withTokenValue(DUMMY_STR)
            .header(DUMMY_STR, null).claim(DISPLAY_NAME_STR, DUMMY_STR).build();
    private static final String JEAP_AUTHORIZATION_LOG_MESSAGE = "Authenticated User is 'test'.";

    @Mock
    private ServletJeapAuthorization jeapAuthorization;
    @Mock
    private AuthCodeGenerationService authCodeGenerationService;
    @InjectMocks
    private AuthCodeGenerationController controller;
    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        this.mockMvc = standaloneSetup(controller).build();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        mapper.registerModule(module);
    }

    @Test
    public void test_create() throws Exception {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now());
        AuthorizationCodeResponseDto responseDto = new AuthorizationCodeResponseDto(TEST_AUTHORIZATION_CODE);
        when(jeapAuthorization.getJeapAuthenticationToken()).thenReturn(new JeapAuthenticationToken(JWT, Collections.emptySet()));
        when(authCodeGenerationService.create(any(AuthorizationCodeCreateDto.class))).thenReturn(responseDto);

        //when
        MvcResult result = mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", DUMMY_STR)
                .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        AuthorizationCodeResponseDto expectedDto = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeResponseDto.class);
        assertEquals(TEST_AUTHORIZATION_CODE, expectedDto.getAuthorizationCode());
    }

    @Test
    public void test_create_with_claim_display_name() throws Exception {
        //setup
        ListAppender<ILoggingEvent> loggingEventListAppender = LoggerTestUtil.getListAppenderForClass(AuthCodeGenerationController.class);
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now());
        AuthorizationCodeResponseDto responseDto = new AuthorizationCodeResponseDto(TEST_AUTHORIZATION_CODE);
        when(jeapAuthorization.getJeapAuthenticationToken()).thenReturn(new JeapAuthenticationToken(JWT_WITH_CLAIM_DISPLAY_NAME, Collections.emptySet()));
        when(authCodeGenerationService.create(any(AuthorizationCodeCreateDto.class))).thenReturn(responseDto);

        //when
        MvcResult result = mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", DUMMY_STR)
                .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn();

        //then
        AuthorizationCodeResponseDto expectedDto = mapper.readValue(result.getResponse().getContentAsString(), AuthorizationCodeResponseDto.class);
        assertEquals(TEST_AUTHORIZATION_CODE, expectedDto.getAuthorizationCode());
        assertThat(loggingEventListAppender.list).extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple(JEAP_AUTHORIZATION_LOG_MESSAGE, Level.INFO));
    }

    @Test
    public void test_create_bad_request_exception() throws Exception {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now().plusDays(1));
        when(jeapAuthorization.getJeapAuthenticationToken()).thenReturn(new JeapAuthenticationToken(JWT, Collections.emptySet()));
        when(authCodeGenerationService.create(any(AuthorizationCodeCreateDto.class))).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        //when
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", DUMMY_STR)
                .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().is(400));
    }
}
