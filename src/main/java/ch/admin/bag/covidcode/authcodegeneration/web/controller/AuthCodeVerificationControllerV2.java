package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDtoWrapper;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/v2/onset")
@Slf4j
public class AuthCodeVerificationControllerV2 {

    private final AuthCodeVerificationService authCodeVerificationService;
    private final Duration requestTime;

    public AuthCodeVerificationControllerV2(AuthCodeVerificationService authCodeVerificationService, @Value("${authcodegeneration.service.requestTime}") long requestTime) {
        this.authCodeVerificationService = authCodeVerificationService;
        this.requestTime = Duration.ofMillis(requestTime);
    }

    @Operation(summary = "Authorization code verification method")
    @PostMapping()
    public ResponseEntity<AuthorizationCodeVerifyResponseDtoWrapper> verify(@Valid @RequestBody AuthorizationCodeVerificationDto verificationDto) {
        var now = Instant.now().toEpochMilli();
        log.debug("Call of Verify with authCode '{}'.", verificationDto.getAuthorizationCode());
        final AuthorizationCodeVerifyResponseDtoWrapper accessTokenWrapper = authCodeVerificationService.verify(verificationDto.getAuthorizationCode(), verificationDto.getFake(), true);
        normalizeRequestTime(now);
        if (accessTokenWrapper.getSwissCovidAccessToken() == null || accessTokenWrapper.getNotifyMeAccessToken() == null) {
            throw new ResourceNotFoundException(null);
        }
        return ResponseEntity.ok().body(accessTokenWrapper);
    }

    private void normalizeRequestTime(long now) {
        long after = Instant.now().toEpochMilli();
        long duration = after - now;
        try {
            Thread.sleep(Math.max(requestTime.minusMillis(duration).toMillis(), 0));
        } catch (Exception ex) {
            log.error("Error during sleep", ex);
        }
    }
}
