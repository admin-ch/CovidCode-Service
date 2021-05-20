package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeOnsetResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDtoWrapper;
import ch.admin.bag.covidcode.authcodegeneration.api.TokenType;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ch.admin.bag.covidcode.authcodegeneration.api.TokenType.CHECKIN_USERUPLOAD_TOKEN;
import static ch.admin.bag.covidcode.authcodegeneration.api.TokenType.DP3T_TOKEN;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AuthCodeVerificationService {

  private static final String FAKE_STRING = "1";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd");
  private final AuthorizationCodeRepository authorizationCodeRepository;
  private final CustomTokenProvider tokenProvider;

  @Value("${authcodegeneration.service.callCountLimit}")
  private int callCountLimit;

  @Transactional
  public AuthorizationCodeVerifyResponseDto verify(String code, String fake) {
    final var accessTokens = verify(code, fake, false);
    return accessTokens.getDP3TAccessToken();
  }

  /**
   * @param code Authorization code as provided by the health authority
   * @param fake String to request fake token
   * @param needCheckInToken Needs a second token for purple (checkIn) backend
   * @return a wrapper containing two access tokens, which are null if authCode is invalid
   */
  @Transactional
  public AuthorizationCodeVerifyResponseDtoWrapper verify(
      String code, String fake, boolean needCheckInToken) {
    final var accessTokens = new AuthorizationCodeVerifyResponseDtoWrapper();
    if (FAKE_STRING.equals(fake)) {
      final var dp3tToken =
          new AuthorizationCodeVerifyResponseDto(
              tokenProvider.createToken(
                  AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER),
                  FAKE_STRING,
                  DP3T_TOKEN));
      accessTokens.setDP3TAccessToken(dp3tToken);
      if (needCheckInToken) {
        final var checkInToken =
            new AuthorizationCodeVerifyResponseDto(
                tokenProvider.createToken(
                    AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER),
                    FAKE_STRING,
                    CHECKIN_USERUPLOAD_TOKEN));
        accessTokens.setCheckInAccessToken(checkInToken);
      }
      return accessTokens;
    }

    AuthorizationCode existingCode = authorizationCodeRepository.findByCode(code).orElse(null);

    if (existingCode == null) {
      log.error("No AuthCode found with code '{}'", code);
      return accessTokens;
    } else if (codeValidityHasExpired(existingCode.getExpiryDate())) {
      log.error("AuthCode '{}' expired at {}", code, existingCode.getExpiryDate());
      return accessTokens;
    } else if (existingCode.getCallCount() >= this.callCountLimit) {
      log.error("AuthCode '{}' reached call limit {}", code, existingCode.getCallCount());
      return accessTokens;
    }

    existingCode.incrementCallCount();
    log.debug(
        "AuthorizationCode verified: '{}', '{}', '{}', '{}', '{}'",
        kv("id", existingCode.getId()),
        kv("callCount", existingCode.getCallCount()),
        kv(
            "creationDateTime",
            existingCode.getCreationDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
        kv("onsetDate", existingCode.getOnsetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),
        kv(
            "originalOnsetDate",
            existingCode.getOriginalOnsetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
    final var swissCovidToken =
        new AuthorizationCodeVerifyResponseDto(
            tokenProvider.createToken(
                existingCode.getOnsetDate().format(DATE_FORMATTER), fake, DP3T_TOKEN));
    accessTokens.setDP3TAccessToken(swissCovidToken);
    if (needCheckInToken) {
      final var checkInToken =
          new AuthorizationCodeVerifyResponseDto(
              tokenProvider.createToken(
                  existingCode.getOnsetDate().format(DATE_FORMATTER), fake, CHECKIN_USERUPLOAD_TOKEN));
      accessTokens.setCheckInAccessToken(checkInToken);
    }
    return accessTokens;
  }

  /**
   * @param authorizationCode Authorization code as provided by the health authority
   * @param fake String to request fake token
   * @return object containing a formatted string representing the onset date, or null if auth code is invalid
   */
  @Transactional(readOnly = true)
  public AuthorizationCodeOnsetResponseDto getOnsetForAuthCode(String authorizationCode, String fake) {
    if (FAKE_STRING.equals(fake)) {
      return new AuthorizationCodeOnsetResponseDto(AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER));
    }
    AuthorizationCode existingCode = authorizationCodeRepository.findByCode(authorizationCode).orElse(null);
    if (existingCode == null) {
      log.error("No AuthCode found with code '{}'", authorizationCode);
      return new AuthorizationCodeOnsetResponseDto(null);
    } else if (codeValidityHasExpired(existingCode.getExpiryDate())) {
      log.error("AuthCode '{}' expired at {}", authorizationCode, existingCode.getExpiryDate());
      return new AuthorizationCodeOnsetResponseDto(null);
    } else if (existingCode.getCallCount() >= this.callCountLimit) {
      log.error("AuthCode '{}' reached call limit {}", authorizationCode, existingCode.getCallCount());
      return new AuthorizationCodeOnsetResponseDto(null);
    }
    return new AuthorizationCodeOnsetResponseDto(existingCode.getOnsetDate().format(DATE_FORMATTER));
  }

  private boolean codeValidityHasExpired(ZonedDateTime expiryDate) {
    return expiryDate.isBefore(ZonedDateTime.now());
  }
}
