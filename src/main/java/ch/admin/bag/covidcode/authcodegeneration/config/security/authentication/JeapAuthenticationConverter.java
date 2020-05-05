package ch.admin.bag.covidcode.authcodegeneration.config.security.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;


@Slf4j
public class JeapAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String USER_ROLES_CLAIM = "userroles";
    private static final String BUSINESS_PARTNER_ROLES_CLAIM = "bproles";
    private static final String ROLES_CLAIM = "roles";

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Set<String> userRoles = extractUserRoles(jwt);
        Map<String, Set<String>> businesspartnerRoles = extractBusinesspartnerRoles(jwt);
        return new JeapAuthenticationToken(jwt, userRoles, businesspartnerRoles);
    }

    private Set<String> extractUserRoles(Jwt jwt) {
        List userrolesClaim = Optional.of(jwt)
                .map(Jwt::getClaims)
                .flatMap(map -> getIfPossible(map, USER_ROLES_CLAIM, List.class))
                .orElse(Collections.emptyList());

        Set<String> userRoles = new HashSet<>();
        userrolesClaim.forEach( userroleObject -> {
            try {
                userRoles.add((String) userroleObject);
            } catch (ClassCastException e) {
                log.warn("Ignoring non String user role.");
            }
        });

        return userRoles;
    }

    private Map<String, Set<String>> extractBusinesspartnerRoles(Jwt jwt) {
        Map bprolesClaim = JeapAuthenticationContext.isB2B(jwt)
                ? extractBusinesspartnerRolesFromB2B(jwt)
                : extractBusinesspartnerRolesFromSysOrUser(jwt);

        Map<String, Set<String>> businesspartnerRoles = new HashMap<>();
        Set<String> businesspartnersInError = new HashSet<>();
        bprolesClaim.forEach( (businesspartnerObject, rolesObject) -> {
            try {
                String businessPartner = (String) businesspartnerObject;
                Set<String> roles = new HashSet<>((Collection<String>) rolesObject);
                businesspartnerRoles.put(businessPartner, roles);
            } catch (ClassCastException e) {
                businesspartnersInError.add(businesspartnerObject.toString());
            }
        });

        if (!businesspartnersInError.isEmpty()) {
            log.warn("Extracting the roles of some business partners failed: {}.",
                    String.join(", ", businesspartnersInError));
        }

        return businesspartnerRoles;
    }

    private Map extractBusinesspartnerRolesFromB2B(Jwt jwt) {
        List roles = Optional.of(jwt)
                .map(Jwt::getClaims)
                .flatMap(map -> getIfPossible(map, ROLES_CLAIM, List.class))
                .orElse(Collections.emptyList());

        Collection<String> bpRoles = new HashSet<>();
        roles.forEach( role -> {
            try {
                bpRoles.add((String) role);
            } catch (ClassCastException e) {
                log.warn("Ignoring non String B2B role.");
            }
        });
        return Collections.singletonMap(jwt.getSubject(), bpRoles);
    }

    private Map extractBusinesspartnerRolesFromSysOrUser(Jwt jwt) {
        return Optional.of(jwt)
                .map(Jwt::getClaims)
                .flatMap(map -> getIfPossible(map, BUSINESS_PARTNER_ROLES_CLAIM, Map.class))
                .orElse(Collections.emptyMap());
    }

    private <T> Optional<T> getIfPossible(Map map, String key, Class<T> klass) {
        Object value = map.get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(klass.cast(value));
        } catch (ClassCastException e) {
            log.warn("Unable to map value of entry {} to class {}, ignoring the entry.", key, klass.getSimpleName());
            return Optional.empty();
        }
    }
}
