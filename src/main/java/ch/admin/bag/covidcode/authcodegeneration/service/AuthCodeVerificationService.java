package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
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
    final var dtos = verify(code, fake, false);
    if (dtos != null) {
      return dtos.get(0);
    } else {
      return null;
    }
  }

  /**
   * @param code
   * @param fake
   * @param needNotifyMeToken Needs a second token for purple (notifyMe) backend
   * @return a list containing one or two tokens, depending on needNotifyMeToken param
   */
  @Transactional
  public List<AuthorizationCodeVerifyResponseDto> verify(
      String code, String fake, boolean needNotifyMeToken) {
    final var tokenList = new ArrayList<AuthorizationCodeVerifyResponseDto>();
    if (FAKE_STRING.equals(fake)) {
      log.debug("Fake Call of verification !");
      final var swissCovidToken =
          new AuthorizationCodeVerifyResponseDto(
              tokenProvider.createToken(
                  AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER),
                  fake,
                  false));
      tokenList.add(swissCovidToken);
      if (needNotifyMeToken) {
        final var notifyMeToken =
            new AuthorizationCodeVerifyResponseDto(
                tokenProvider.createToken(
                    AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER),
                    fake,
                    true));
        tokenList.add(notifyMeToken);
      }
      return tokenList;
    }

    AuthorizationCode existingCode = authorizationCodeRepository.findByCode(code).orElse(null);

    if (existingCode == null) {
      log.error("No AuthCode found with code '{}'", code);
      return null;
    } else if (codeValidityHasExpired(existingCode.getExpiryDate())) {
      log.error("AuthCode '{}' expired at {}", code, existingCode.getExpiryDate());
      return null;
    } else if (existingCode.getCallCount() >= this.callCountLimit) {
      log.error("AuthCode '{}' reached call limit {}", code, existingCode.getCallCount());
      return null;
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
                existingCode.getOnsetDate().format(DATE_FORMATTER), fake, false));
    tokenList.add(swissCovidToken);
    if (needNotifyMeToken) {
      final var notifyMeToken =
          new AuthorizationCodeVerifyResponseDto(
              tokenProvider.createToken(
                  existingCode.getOnsetDate().format(DATE_FORMATTER), fake, true));
      tokenList.add(notifyMeToken);
    }
    return tokenList;
  }

  private boolean codeValidityHasExpired(ZonedDateTime expiryDate) {
    return expiryDate.isBefore(ZonedDateTime.now());
  }
}
