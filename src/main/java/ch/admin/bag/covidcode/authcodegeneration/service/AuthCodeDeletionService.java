package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthCodeDeletionService {

    private final AuthorizationCodeRepository authorizationCodeRepository;

    @Transactional
    @Scheduled(cron = "${authcodegeneration.service.deletionCron}")
    public void deleteOldAuthCode() {
        ZonedDateTime now = ZonedDateTime.now();
        log.info("Delete old AuthCodes expired before '{}'.", now);
        List<AuthorizationCode> expiredAuthCodes = authorizationCodeRepository.findByExpiryDateBefore(now);

        log.info("Found {} AuthCodes to delete.", expiredAuthCodes.size());

        expiredAuthCodes.forEach(ac -> {
            log.info("Deleting code '{}' with expiryDate '{}'.", ac.getCode(), ac.getExpiryDate());
            authorizationCodeRepository.delete(ac);
        });

        log.info("All old AuthCodes expired before '{}' are now deleted.", now);

    }

}
