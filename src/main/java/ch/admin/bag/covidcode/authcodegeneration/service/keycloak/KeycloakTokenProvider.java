package ch.admin.bag.covidcode.authcodegeneration.service.keycloak;

import ch.admin.bag.covidcode.authcodegeneration.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Profile("keycloak-token-provider")
public class KeycloakTokenProvider implements TokenProvider {

    private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final KeycloakAdminService keycloakAdminService;

    @Override
    public String createToken(String onsetDate, String fake) {
        //POST Request to get the access token for the master realm
        String accessTokenToMasterRealm = keycloakAdminService.getAccessTokenToMasterRealm();

        //POST Request to create a temporary user with onset and UUID
        String username = RandomStringUtils.random(10, VALID_CHARS);
        String password = RandomStringUtils.random(10, VALID_CHARS);
        String uuid = UUID.randomUUID().toString();

        keycloakAdminService.createUser(username, onsetDate, uuid, fake, accessTokenToMasterRealm);

        //GET Request to get the userId of the new user
        String userId = keycloakAdminService.getUserIdValue(username, accessTokenToMasterRealm);

        //PUT Request to set the password for the temporary user
        keycloakAdminService.resetPassword(userId, password, accessTokenToMasterRealm);

        //POST Request to get the access token for the red backend
        String accessToken = keycloakAdminService.getAccessTokenToBackend(username, password);

        //DELETE Request to delete the temporary user
        keycloakAdminService.deleteUser(userId, accessTokenToMasterRealm);


        return accessToken;
    }


}
