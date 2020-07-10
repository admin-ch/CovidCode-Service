package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AuthCodeGenerationService {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_NUMBER_LENGTH = 12;

    @Value("${authcodegeneration.service.onsetSubtractionDays}")
    private int onsetSubtractionDays;

    @Value("${authcodegeneration.service.codeExpirationDelay}")
    private int codeExpirationDelay;

    @Transactional
    public AuthorizationCodeResponseDto create(AuthorizationCodeCreateDto createDto) {
        validateOnsetDate(createDto.getOnsetDate());
        String authCode = generateAuthCode();

        while (authorizationCodeRepository.existsByCode(authCode)) {
            log.error("Created a duplicate AuthCode: {}", authCode);
            authCode = generateAuthCode();
        }

        AuthorizationCode authorizationCode = new AuthorizationCode(authCode, createDto.getOnsetDate(), createDto.getOnsetDate().minusDays(onsetSubtractionDays), ZonedDateTime.now().plusMinutes(codeExpirationDelay));
        authorizationCodeRepository.saveAndFlush(authorizationCode);
        log.info("New authorizationCode saved: {}, {}, {}.", kv("id", authorizationCode.getId()), kv("creationDateTime", authorizationCode.getCreationDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)), kv("onsetDate",authorizationCode.getOnsetDate()));
        return new AuthorizationCodeResponseDto(authorizationCode.getCode());
    }

    private String generateAuthCode(){
        return String.format ("%012d", generateRandom(RANDOM_NUMBER_LENGTH)); // 12-digit random numeric code
    }

    private void validateOnsetDate(LocalDate onsetDate) {
        if (onsetDate.isBefore(LocalDate.now().minusWeeks(4))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Onset date: " + onsetDate + " should not be more than 4 weeks in the past!");
        }

        if (onsetDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Onset date: " + onsetDate + " should not be in the future!");
        }
    }

    private static long generateRandom(int length) {
        char[] digits = new char[length];
        for (int i = 0; i < length; i++) {
            digits[i] = (char) (RANDOM.nextInt(10) + '0');
        }
        return Long.parseLong(new String(digits));
    }

}
