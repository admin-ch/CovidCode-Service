package ch.admin.bag.covidcode.authcodegeneration.config.security.validation;

import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.JeapAuthenticationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class AudienceJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;
    private final OAuth2Error error;

    public AudienceJwtValidator(String audience) {
        this.audience = audience;
        this.error = new OAuth2Error("invalid_token", "The token is is not valid for audience '" + audience + "'.", null);
    }

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (JeapAuthenticationContext.isB2B(jwt)) {
            //In B2B, we never have a valid audience => We have to accept everything
            return OAuth2TokenValidatorResult.success();
        }
        if(jwt.getAudience() == null || jwt.getAudience().isEmpty()) {
            //If audience is missing this means token is valid for every system
            return OAuth2TokenValidatorResult.success();
        }
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        } else {
            log.warn(error.getDescription());
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}
