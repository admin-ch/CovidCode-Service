package ch.admin.bag.covidcode.authcodegeneration.config.security.validation;

import ch.admin.bag.covidcode.authcodegeneration.config.security.authentication.JeapAuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * This class implements a JwtDecoder that delegates the decoding to one of the two configured JwtDecoder
 * instances depending on the authentication context specified by the raw JWT token to decode. Tokens issued for the
 * authentication contexts USER and SYSTEM are delegated to the authenticationServerJwtDecoder. Tokens issued for the
 * authentication context B2B are delegated to the b2bGatewayJwtDecoder. This allows to validate B2B tokens differently
 * than the authentication server tokens.
 */
@RequiredArgsConstructor
class JeapJwtDecoder implements JwtDecoder {

    private final JwtDecoder authenticationServerJwtDecoder;
    private final JwtDecoder b2bGatewayJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        JeapAuthenticationContext context = RawJwtTokenParser.extractAuthenticationContext(token);
        if (context == JeapAuthenticationContext.B2B) {
            return b2bGatewayJwtDecoder.decode(token);
        }
        else {
            return authenticationServerJwtDecoder.decode(token);
        }
    }

}
