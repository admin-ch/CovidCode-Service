package ch.admin.bag.covidcode.authcodegeneration.config.security.validation;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class JeapJwtDecoderFactory {

    /**
     * Create a JWT decoder that accepts tokens from the authorization server and optionally also from the B2B gateway.
     * Tokens from the B2B gateway must have been issued for the B2B context. All tokens are validated by the given
     * JWT validator.
     *
     * @param authorizationServerJwkSetUri JWK set URI of the authorization server
     * @param b2bGatewayJwkSetUri JWK set URI of the B2B gateway, may be <code>null</code> if there is no need to accept B2B gateway tokens.
     * @param jwtValidator The JWT validator
     * @return The requested JWT decoder.
     */
    public static JwtDecoder createJwtDecoder(String authorizationServerJwkSetUri, String b2bGatewayJwkSetUri, OAuth2TokenValidator<Jwt> jwtValidator) {
        if (StringUtils.isBlank(b2bGatewayJwkSetUri)) {
            return createDefaultJwtDecoder(authorizationServerJwkSetUri, jwtValidator);
        }
        else {
            return new JeapJwtDecoder(
                    createDefaultJwtDecoder(authorizationServerJwkSetUri, jwtValidator),
                    createDefaultJwtDecoder(b2bGatewayJwkSetUri, jwtValidator));
        }
    }

    private static JwtDecoder createDefaultJwtDecoder(String jwkSetUri, OAuth2TokenValidator<Jwt> jwtValidator) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.
                withJwkSetUri(jwkSetUri).
                jwsAlgorithm(SignatureAlgorithm.RS256).
                jwsAlgorithm(SignatureAlgorithm.RS512).
                build();
        jwtDecoder.setJwtValidator(jwtValidator);
        return jwtDecoder;
    }

}
