package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import ch.admin.bag.covidcode.authcodegeneration.testutil.LoggerTestUtil;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultKeycloakAdminServiceTest {
    private static final String TEST_USER = "U123";
    private static final String TEST_PWD = "test";
    private static final String TEST_TOKEN = "test";
    private static final String TEST_ONSET_DATE = "2020-02-01";
    private static final String TEST_UUID = UUID.randomUUID().toString();
    private static final String SERVICE_URL_KEY = "serviceUrl";
    private static final String SERVICE_URL_VALUE = "https://identity-r.bit.admin.ch";
    private static final String TOKEN_RESPONSE_BODY = "{\"access_token\": \"" + TEST_TOKEN + "\"}";
    private static final String USERS_RESPONSE_BODY = "[{\"id\": \"" + TEST_UUID + "\",\"username\":\"123456789\",\"firstName\":\"xyz\",\"lastName\":\"xyz\",\"attributes\":{\"onset\":[\"" +
            TEST_ONSET_DATE + "\"],\"uuid\":[\"0000-0000-0000-0000\"]}, \"email\":\"123456789@admin.ch\", \"enabled\":\"true\"}]";
    private static final String INVALID_USERS_RESPONSE_BODY = "{\"email\":\"123456789@admin.ch\"}";
    private static final String USER_CREATE_MESSAGE = "Create user with username '" + TEST_USER + "', onset '" + TEST_ONSET_DATE + "' and uuid '" + TEST_UUID + "'.";
    private static final String KEYCLOAK_RESPONSE_MESSAGE = "Keycloak Response: <204 NO_CONTENT No Content,{\"access_token\": \"test\"},[]>";

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private DefaultKeycloakAdminService testee;

    @Test
    void test_getAccessTokenToMasterRealm() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        String token = testee.getAccessTokenToMasterRealm();
        //then
        assertNotNull(token);
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void test_getAccessTokenToMasterRealm_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        //when
        //then
        assertThrows(ResponseStatusException.class, () -> testee.getAccessTokenToMasterRealm());
    }

    @Test
    void test_getAccessTokenToMasterRealm_no_response_body_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.getAccessTokenToMasterRealm());
    }

    @Test
    void test_getAccessTokenToBackend() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        String token = testee.getAccessTokenToBackend(TEST_USER, TEST_PWD);
        //then
        assertNotNull(token);
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void test_getUserIdValue() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(USERS_RESPONSE_BODY, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        String userIdValue = testee.getUserIdValue(TEST_USER, TEST_TOKEN);
        //then
        assertNotNull(userIdValue);
        assertEquals(TEST_UUID, userIdValue);
    }

    @Test
    void test_getUserIdValue_no_user_id_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(INVALID_USERS_RESPONSE_BODY, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.getUserIdValue(TEST_USER, TEST_TOKEN));
    }

    @Test
    void test_getUserIdValue_no_response_body_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.getUserIdValue(TEST_USER, TEST_TOKEN));
    }

    @Test
    void test_createUser() {
        //setup
        ListAppender<ILoggingEvent> loggingEventListAppender = LoggerTestUtil.getListAppenderForClass(DefaultKeycloakAdminService.class);
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        testee.createUser(TEST_USER, TEST_ONSET_DATE, TEST_UUID, "0", TEST_TOKEN);
        //then
        assertThat(loggingEventListAppender.list).extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple(USER_CREATE_MESSAGE, Level.DEBUG));
    }

    @Test
    void test_createUser_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.createUser(TEST_USER, TEST_ONSET_DATE, TEST_UUID, "0", TEST_TOKEN));
    }

    @Test
    void test_resetPassword() {
        //setup
        ListAppender<ILoggingEvent> loggingEventListAppender = LoggerTestUtil.getListAppenderForClass(DefaultKeycloakAdminService.class);
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        testee.resetPassword(TEST_USER, TEST_PWD, TEST_TOKEN);
        //then
        assertThat(loggingEventListAppender.list).extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple(KEYCLOAK_RESPONSE_MESSAGE, Level.DEBUG));
    }

    @Test
    void test_resetPassword_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.resetPassword(TEST_USER, TEST_PWD, TEST_TOKEN));
    }

    @Test
    void test_deleteUser() {
        //setup
        ListAppender<ILoggingEvent> loggingEventListAppender = LoggerTestUtil.getListAppenderForClass(DefaultKeycloakAdminService.class);
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        testee.deleteUser(TEST_USER, TEST_TOKEN);
        //then
        assertThat(loggingEventListAppender.list).extracting(ILoggingEvent::getFormattedMessage, ILoggingEvent::getLevel)
                .contains(Tuple.tuple(KEYCLOAK_RESPONSE_MESSAGE, Level.DEBUG));
    }

    @Test
    void test_deleteUser_exception() {
        //given
        ReflectionTestUtils.setField(testee, SERVICE_URL_KEY, SERVICE_URL_VALUE);
        ResponseEntity<String> response = new ResponseEntity<>(TOKEN_RESPONSE_BODY, HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), any(Class.class))).thenReturn(response);

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.deleteUser(TEST_USER, TEST_TOKEN));
    }
}
