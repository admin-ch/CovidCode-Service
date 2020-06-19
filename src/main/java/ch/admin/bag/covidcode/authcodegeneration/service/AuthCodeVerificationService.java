package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

        AuthorizationCode existingCode;

        if (FAKE_STRING.equals(fake)) {
            log.debug("Fake Call of verification !");
            existingCode = AuthorizationCode.createFake();
        } else {

            existingCode = authorizationCodeRepository.findByCode(code).orElse(null);

            if (existingCode == null) {
                log.error("No AuthCode found with code '{}'", code);
                throw new ResourceNotFoundException(null);
            } else if (codeValidityHasExpired(existingCode.getExpiryDate())) {
                log.error("AuthCode '{}' expired at {}", code, existingCode.getExpiryDate());
                throw new ResourceNotFoundException(null);
            } else if (existingCode.getCallCount() >= this.callCountLimit) {
                log.error("AuthCode '{}' reached call limit {}", code, existingCode.getCallCount());
                throw new ResourceNotFoundException(null);
            }

        }

        try {
            String token = tokenProvider.createToken(existingCode.getOnsetDate().format(DATE_FORMATTER), fake);
            existingCode.incrementCallCount();
            return new AuthorizationCodeVerifyResponseDto(token);
        } catch (Exception e) {
            log.error("Error during Token Generation", e);
            throw new IllegalStateException("Internal Error");
        }
    }

    private boolean codeValidityHasExpired(ZonedDateTime expiryDate) {
        return expiryDate.isBefore(ZonedDateTime.now());
    }
}
