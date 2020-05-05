package ch.admin.bag.covidcode.authcodegeneration.web.controller;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.service.AuthCodeGenerationService;
import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.JeapAuthenticationToken;
import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.ServletJeapAuthorization;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1/authcode")
@RequiredArgsConstructor
@Slf4j
public class AuthCodeGenerationController {

    private final ServletJeapAuthorization jeapAuthorization;

    private final AuthCodeGenerationService authCodeGenerationService;

    @Operation(summary = "Authorization code generation method")
    @PostMapping()
    @PreAuthorize("hasRole('bag-pts-allow')")
    public AuthorizationCodeResponseDto create(@Valid @RequestBody AuthorizationCodeCreateDto createDto, HttpServletRequest request) {
        log.debug("Call of Create with onset date '{}'.", createDto.getOnsetDate());
        logAuthorizationInfo(request);
        return authCodeGenerationService.create(createDto);
    }

    private void logAuthorizationInfo(HttpServletRequest request) {
        // A request to the OAuth2 protected resource includes the access token in the 'Authorization' header.
        // This token is the base of the Spring Security Authentication associated with the authenticated request.
        log.debug("Access token: {}.", request.getHeader("Authorization"));

        // Access the Spring Security Authentication as JeapAuthenticationToken
        JeapAuthenticationToken jeapAuthenticationToken = jeapAuthorization.getJeapAuthenticationToken();
        log.debug(jeapAuthenticationToken.toString());

        String displayName = jeapAuthenticationToken.getToken().getClaimAsString("displayName");

        if (displayName == null) {
            displayName = jeapAuthenticationToken.getTokenName();
        }

        log.info("Authenticated User is '{}'.", displayName);

    }
}
