package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/v1/onset")
@Slf4j
public class AuthCodeVerificationController {

    private final AuthCodeVerificationService authCodeVerificationService;
    private final Duration requestTime;

    public AuthCodeVerificationController(AuthCodeVerificationService authCodeVerificationService, @Value("${authcodegeneration.service.requestTime}") long requestTime) {
        this.authCodeVerificationService = authCodeVerificationService;
        this.requestTime = Duration.ofMillis(requestTime);
    }

    @Operation(summary = "Authorization code verification method")
    @PostMapping()
    public AuthorizationCodeVerifyResponseDto verify(@Valid @RequestBody AuthorizationCodeVerificationDto verificationDto) {
        var now = Instant.now().toEpochMilli();
        log.debug("Call of Verify with authCode '{}'.", verificationDto.getAuthorizationCode());
        AuthorizationCodeVerifyResponseDto responseDto = authCodeVerificationService.verify(verificationDto.getAuthorizationCode(), verificationDto.getFake());
        normalizeRequestTime(now);
        if (responseDto == null) {
            throw new ResourceNotFoundException(null);
        }
        return responseDto;
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
