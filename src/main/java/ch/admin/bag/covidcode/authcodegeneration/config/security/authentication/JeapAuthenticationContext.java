package ch.admin.bag.covidcode.authcodegeneration.config.security.authentication;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The supported authentication contexts.
 */
public enum JeapAuthenticationContext {

    SYS, B2B, USER;

    private final static String CONTEXT_CLAIM_NAME = "ctx";

    public static JeapAuthenticationContext readFromJwt(Jwt jwt) {
        String context = jwt.getClaimAsString(CONTEXT_CLAIM_NAME);
        if(context == null) {
            throw new IllegalArgumentException("Context claim '" + CONTEXT_CLAIM_NAME + "' is missing from the JWT.");
        }
        return JeapAuthenticationContext.valueOf(context);
    }

    public static boolean isB2B(Jwt jwt) {
        try {
            return readFromJwt(jwt).equals(B2B);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getContextJwtClaimName() {
        return CONTEXT_CLAIM_NAME;
    }

}
