package ch.admin.bag.covidcode.authcodegeneration.config.security;

import lombok.Data;

/**
 * Configuration properties to configure the B2B gateway that the OAuth2 resource server will accept tokens from.
 */

@Data
public class B2BGatewayConfigProperties {
    private String issuer;
    private String jwkSetUri;
}

