package ch.admin.bag.covidcode.authcodegeneration.config.security.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JeapAuthenticationToken extends JwtAuthenticationToken {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Map<String, Set<String>> businessPartnerRoles;
    private final Set<String> userRoles;

    public JeapAuthenticationToken(Jwt jwt, Set<String> userRoles, Map<String, Set<String>> businessPartnerRoles) {
        super(jwt, deriveAuthoritiesFromRoles(userRoles, businessPartnerRoles));
        this.businessPartnerRoles = (businessPartnerRoles != null ? Collections.unmodifiableMap(businessPartnerRoles) : Collections.emptyMap());
        this.userRoles = (userRoles != null ? Collections.unmodifiableSet(userRoles) : Collections.emptySet());
    }

    /**
     * Get the client id specified in this token.
     *
     * @return The client id specified in this token.
     */
    public String getClientId() {
        return getToken().getClaimAsString("clientId");
    }

    /**
     * Get the name specified in this token.
     *
     * @return The name specified in this token.
     */
    public String getTokenName() {
        return getToken().getClaimAsString("name");
    }

    /**
     * Get the given name specified in this token.
     *
     * @return The given name specified in this token.
     */
    public String getTokenGivenName() {
        return getToken().getClaimAsString("given_name");
    }

    /**
     * Get the family name specified in this token.
     *
     * @return The family name specified in this token.
     */
    public String getTokenFamilyName() {
        return getToken().getClaimAsString("family_name");
    }

    /**
     * Get the subject specified in this token.
     *
     * @return The subject specified in this token.
     */
    public String getTokenSubject() {
        return getToken().getClaimAsString("sub");
    }

    /**
     * Get the locale specified in this token.
     *
     * @return The locale specified in this token.
     */
    public String getTokenLocale() {
        return getToken().getClaimAsString("locale");
    }

    /**
     * Get the jeap authentication context specified in this token.
     *
     * @return The jeap authentication context specified in this token.
     */
    public JeapAuthenticationContext getJeapAuthenticationContext() {
        return JeapAuthenticationContext.readFromJwt(getToken());
    }

    /**
     * Get the business partner roles listed in this token.
     *
     * @return The business partner roles grouped in sets by business partner id.
     */
    public Map<String, Set<String>> getBusinessPartnerRoles() {
        return businessPartnerRoles;
    }

    /**
     * Get the user roles listed in this token.
     *
     * @return The user roles
     */
    public Set<String> getUserRoles() {
        return userRoles;
    }

    @Override
    public String toString() {
        return String.format(
                "JeapAuthenticationToken{ subject (calling user): %s, client (calling system): %s, authorities (all roles): %s, user roles: %s, business partner roles: %s }",
                getName(), getClientId(), authoritiesToString(), userRolesToString(), businessPartnerRolesToString());
    }

    private static Collection<GrantedAuthority> deriveAuthoritiesFromRoles(Set<String> userRoles, Map<String, Set<String>> businesspartnerRoles) {
        return Stream.concat(userRoles.stream(), businesspartnerRoles.values().stream().flatMap(Set::stream))
                .map(s -> ROLE_PREFIX + s)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private String authoritiesToString() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> "'" + a + "'")
                .collect(Collectors.joining(","));
    }

    private String businessPartnerRolesToString() {
        return getBusinessPartnerRoles().entrySet().stream()
                .map(e -> e.getKey() + " [ " + e.getValue().stream().map( r -> "'" + r + "'").collect(Collectors.joining(", ")) + " ]")
                .collect(Collectors.joining(", "));
    }

    private String userRolesToString() {
        return  getUserRoles().stream().
                    map( r -> "'" + r + "'").
                    collect(Collectors.joining(", "));
    }

}
