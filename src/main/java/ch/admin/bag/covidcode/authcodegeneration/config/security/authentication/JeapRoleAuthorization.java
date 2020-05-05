package ch.admin.bag.covidcode.authcodegeneration.config.security.authentication;

import org.springframework.security.core.Authentication;

import java.util.Objects;
import java.util.Optional;

/**
 * This class provides role specific authorization checks against an authentication.
 */
public class JeapRoleAuthorization {

    /**
     * Check if the given authentication contains the given user role.
     *
     * @param role The user role to check
     * @param authentication The authentication to check the user role against
     * @return <code>true</code> if the authentication contains the given user role, <code>false</code> otherwise.
     */
    private boolean hasUserRole(final String role, final Authentication authentication) {
        return getAsJeapAuthenticationToken(authentication)
                .map(JeapAuthenticationToken::getUserRoles)
                .filter(Objects::nonNull)
                .filter(roles -> roles.contains(role))
                .isPresent();
    }

    private Optional<JeapAuthenticationToken> getAsJeapAuthenticationToken(final Authentication authentication) {
        return authentication instanceof JeapAuthenticationToken ?
                Optional.of((JeapAuthenticationToken) authentication) :
                Optional.empty();
    }

}
