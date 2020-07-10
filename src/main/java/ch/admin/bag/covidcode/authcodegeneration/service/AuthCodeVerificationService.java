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

        if (FAKE_STRING.equals(fake)) {
            log.debug("Fake Call of verification !");
            return new AuthorizationCodeVerifyResponseDto(tokenProvider.createToken(AuthorizationCode.createFake().getOnsetDate().format(DATE_FORMATTER), fake));
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
        log.info("AuthorizationCode verified: '{}', '{}', '{}', '{}'", kv("id", existingCode.getId()), kv("callCount", existingCode.getCallCount()), kv("creationDateTime", existingCode.getCreationDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), kv("onsetDate",existingCode.getOnsetDate()));
        return new AuthorizationCodeVerifyResponseDto(tokenProvider.createToken(existingCode.getOnsetDate().format(DATE_FORMATTER), fake));

    }

    private boolean codeValidityHasExpired(ZonedDateTime expiryDate) {
        return expiryDate.isBefore(ZonedDateTime.now());
    }
}
