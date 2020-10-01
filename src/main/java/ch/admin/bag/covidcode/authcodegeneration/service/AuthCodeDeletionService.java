package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(value = "CF_INSTANCE_INDEX", havingValue = "0")
public class AuthCodeDeletionService {

    private final AuthorizationCodeRepository authorizationCodeRepository;

    @Value("${authcodegeneration.service.sleepLogInterval}")
    private int sleepLogInterval;

    @Transactional
    @Scheduled(cron = "${authcodegeneration.service.deletionCron}")
    public void deleteOldAuthCode() {
        ZonedDateTime now = ZonedDateTime.now();
        log.info("Delete old AuthCodes expired before '{}'.", now);
        List<AuthorizationCode> expiredAuthCodes = authorizationCodeRepository.findByExpiryDateBefore(now);

        log.info("Found {} AuthCodes to delete.", expiredAuthCodes.size());

        expiredAuthCodes.forEach(ac -> {

            try {
                Thread.sleep(sleepLogInterval);
            } catch (InterruptedException e) {
                log.error("Exception during sleep", e);
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }

            log.info("AuthorizationCode-Statistic '{}', '{}', '{}', '{}', '{}', '{}'",
                    kv("id", ac.getId()),
                    kv("callCount", ac.getCallCount()),
                    kv("creationDateTime", ac.getCreationDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                    kv("onsetDate", ac.getOnsetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    kv("originalOnsetDate", ac.getOriginalOnsetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    kv("expiryDate", ac.getExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

            authorizationCodeRepository.delete(ac);
        });

        log.info("All old AuthCodes expired before '{}' are now deleted.", now);

    }

}
