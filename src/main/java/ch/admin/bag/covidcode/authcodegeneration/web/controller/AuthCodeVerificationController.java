package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerificationDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeVerifyResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/onset")
@RequiredArgsConstructor
@Slf4j
public class AuthCodeVerificationController {

    private final AuthCodeVerificationService authCodeVerificationService;

    @Operation(summary = "Authorization code verification method")
    @PostMapping()
    public AuthorizationCodeVerifyResponseDto verify(@Valid @RequestBody AuthorizationCodeVerificationDto verificationDto) {
        log.debug("Call of Verify with authCode '{}'.", verificationDto.getAuthorizationCode());
        return authCodeVerificationService.verify(verificationDto.getAuthorizationCode(), verificationDto.getFake());
    }

}
