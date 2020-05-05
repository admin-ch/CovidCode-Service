package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import ch.admin.bag.covidcode.authcodegeneration.service.keycloak.KeycloakAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCodeVerificationServiceTest {

    private static final String FAKE_NOT_FAKE = "0";
    private static final String FAKE_FAKE = "1";

    @Mock
    private AuthorizationCodeRepository repository;
    @Mock
    private KeycloakAdminService keycloakAdminService;
    @InjectMocks
    private AuthCodeVerificationService testee;

    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String TEST_ACCESS_TOKEN = "QRMwjii77";
    private static final int CODE_EXPIRATION_DELAY_IN_SECONDS = 10;
    private static final int CALL_COUNT_LIMIT = 3;

    @Test
    public void test_verify() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, "callCountLimit", CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    public void test_verify_with_yml_prop_callCountLimit() throws Exception {
        //setup
        Path file = Path.of("","src/main/resources").resolve("application.yml");
        Map<String, Object> yamlMaps = new Yaml().load(Files.readString(file));
        final Map<String, Map<String, Object>> obj = (Map<String, Map<String, Object>>) yamlMaps.get("authcodegeneration");
        int callCountLimit = Integer.parseInt(obj.get("service").get("callCountLimit").toString());
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, "callCountLimit", callCountLimit);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    public void test_verify_token_onset_date_is_equal_original_minus_3_days() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, "callCountLimit", CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        verify(keycloakAdminService).createUser(anyString(), eq(LocalDate.now().minusDays(3).toString()), anyString(), anyString(), anyString());
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    public void test_verify_call_count_reached() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, "callCountLimit", CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertThrows(ResourceNotFoundException.class, () -> testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }



    @Test
    public void test_verify_call_fake_count_never_reached() {
        //given
        ReflectionTestUtils.setField(testee, "callCountLimit", CALL_COUNT_LIMIT);
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        //then
        AuthorizationCodeVerifyResponseDto verify = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);

        assertNotNull(verify);
    }

    @Test
    public void test_verify_exception() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, "callCountLimit", CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        //when
        //then
        assertThrows(IllegalStateException.class, () -> testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }

    @Test
    public void test_verify_code_not_found() {
        //given
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        //when
        //then
        assertThrows(ResourceNotFoundException.class, () -> testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }

    @Test
    public void test_verify_code_validity_expired() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now());
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        //when
        //then
        assertThrows(ResourceNotFoundException.class, () -> testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }

}
