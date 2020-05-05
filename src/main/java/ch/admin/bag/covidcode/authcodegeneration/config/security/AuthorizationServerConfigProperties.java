package ch.admin.bag.covidcode.authcodegeneration.config.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import lombok.Data;

/**
 * Configuration properties to configure the authorization server that the OAuth2 resource server will accept tokens from.
 */
@Data
public class AuthorizationServerConfigProperties {

    private static final String JWK_SET_URI_SUBPATH = "/protocol/openid-connect/certs";

    private String issuer;

    private String jwkSetUri;

    public String getJwkSetUri() {
        return StringUtils.isNotBlank(jwkSetUri) ? jwkSetUri : issuer + JWK_SET_URI_SUBPATH;
    }
}

