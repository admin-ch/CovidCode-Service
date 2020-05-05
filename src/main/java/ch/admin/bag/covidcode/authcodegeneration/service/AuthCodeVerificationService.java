package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import ch.admin.bag.covidcode.authcodegeneration.service.keycloak.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AuthCodeVerificationService {

    private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd");
    private static final String FAKE_STRING = "1";

    private final AuthorizationCodeRepository authorizationCodeRepository;

    private final KeycloakAdminService keycloakAdminService;

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

            //POST Request to get the access token for the master realm
            String accessTokenToMasterRealm = keycloakAdminService.getAccessTokenToMasterRealm();

            //POST Request to create a temporary user with onset and UUID
            String username = RandomStringUtils.random(10, VALID_CHARS);
            String password = RandomStringUtils.random(10, VALID_CHARS);
            String uuid = UUID.randomUUID().toString();

            keycloakAdminService.createUser(username, existingCode.getOnsetDate().format(DATE_FORMATTER), uuid, fake, accessTokenToMasterRealm);

            //GET Request to get the userId of the new user
            String userId = keycloakAdminService.getUserIdValue(username, accessTokenToMasterRealm);

            //PUT Request to set the password for the temporary user
            keycloakAdminService.resetPassword(userId, password, accessTokenToMasterRealm);

            //POST Request to get the access token for the red backend
            String accessToken = keycloakAdminService.getAccessTokenToBackend(username, password);

            //DELETE Request to delete the temporary user
            keycloakAdminService.deleteUser(userId, accessTokenToMasterRealm);

            existingCode.incrementCallCount();

            return new AuthorizationCodeVerifyResponseDto(accessToken);
        } catch (Exception e) {
            log.error("Error during Keycloak Token Generation", e);
            throw new IllegalStateException("Internal Error");
        }
    }

    private boolean codeValidityHasExpired(ZonedDateTime expiryDate) {
        return expiryDate.isBefore(ZonedDateTime.now());
    }
}
