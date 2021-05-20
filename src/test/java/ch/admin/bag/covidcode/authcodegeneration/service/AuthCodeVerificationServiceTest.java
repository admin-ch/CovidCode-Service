package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.TokenType;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static ch.admin.bag.covidcode.authcodegeneration.api.TokenType.DP3T_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCodeVerificationServiceTest {

    private static final String FAKE_NOT_FAKE = "0";
    private static final String FAKE_FAKE = "1";
    private static final String CALL_COUNT_LIMIT_KEY = "callCountLimit";
    private static final String TEST_AUTHORIZATION_CODE = "123456789";
    private static final String TEST_ACCESS_TOKEN = "QRMwjii77";
    private static final int CODE_EXPIRATION_DELAY_IN_SECONDS = 10;
    private static final int CALL_COUNT_LIMIT = 3;

    @Mock
    private AuthorizationCodeRepository repository;

    @Mock
    private CustomTokenProvider tokenProvider;

    @InjectMocks
    private AuthCodeVerificationService testee;

    @Test
    void test_verify() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);


        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    void test_verify_with_yml_prop_callCountLimit() throws Exception {
        //setup
        Path file = Path.of("", "src/main/resources").resolve("application.yml");
        Map<String, Object> yamlMaps = new Yaml().load(Files.readString(file));
        final Map<String, Map<String, Object>> obj = (Map<String, Map<String, Object>>) yamlMaps.get("authcodegeneration");
        int callCountLimit = Integer.parseInt(obj.get("service").get(CALL_COUNT_LIMIT_KEY).toString());
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, callCountLimit);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    void test_verify_token_onset_date_is_equal_original_minus_3_days() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);


        //when
        AuthorizationCodeVerifyResponseDto responseDto = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        verify(tokenProvider).createToken(eq(LocalDate.now().minusDays(3).toString()), anyString(), eq(DP3T_TOKEN));
        assertNotNull(responseDto.getAccessToken());
        assertEquals(TEST_ACCESS_TOKEN, responseDto.getAccessToken());
    }

    @Test
    void test_verify_call_count_reached() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);


        //when
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNull(testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }


    @Test
    void test_verify_call_fake_count_never_reached() {
        //given
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);


        //when
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);
        //then
        AuthorizationCodeVerifyResponseDto verify = testee.verify(TEST_AUTHORIZATION_CODE, FAKE_FAKE);

        assertNotNull(verify);
    }


    @Test
    void test_verify_code_not_found() {
        //given
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        //when
        //then
        assertNull(testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }

    @Test
    void test_verify_code_validity_expired() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now());
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        //when
        //then
        assertNull(testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE));
    }

    @Test
    void test_getOnset() {
        //given
        final var onsetAsDate = LocalDate.now().minusDays(3);
        final var onsetAsString = onsetAsDate.format(DateTimeFormatter.ofPattern("YYYY-MM-dd"));
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), onsetAsDate, ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        //when
        String onset = testee.getOnsetForAuthCode(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE).getOnset();
        //then
        assertNotNull(onset);
        assertEquals(onsetAsString, onset);
    }

    @Test
    void test_getOnset_code_not_found() {
        //given
        when(repository.findByCode(anyString())).thenReturn(Optional.empty());
        //when
        //then
        assertNull(testee.getOnsetForAuthCode(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE).getOnset());
    }

    @Test
    void test_getOnset_code_validy_expired() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now());
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        //when
        //then
        assertNull(testee.getOnsetForAuthCode(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE).getOnset());
    }

    @Test
    void test_getOnset_code_call_count_reached() {
        //given
        AuthorizationCode authCode = new AuthorizationCode(TEST_AUTHORIZATION_CODE, LocalDate.now(), LocalDate.now().minusDays(3), ZonedDateTime.now().plusSeconds(CODE_EXPIRATION_DELAY_IN_SECONDS));
        ReflectionTestUtils.setField(testee, CALL_COUNT_LIMIT_KEY, CALL_COUNT_LIMIT);
        when(repository.findByCode(anyString())).thenReturn(Optional.of(authCode));
        when(tokenProvider.createToken(anyString(), anyString(), any())).thenReturn(TEST_ACCESS_TOKEN);
        //when
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        testee.verify(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE);
        //then
        assertNull(testee.getOnsetForAuthCode(TEST_AUTHORIZATION_CODE, FAKE_NOT_FAKE).getOnset());
    }

}
