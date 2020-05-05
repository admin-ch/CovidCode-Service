package ch.admin.bag.covidcode.authcodegeneration.service.keycloak;

public interface KeycloakAdminService {

    String getAccessTokenToMasterRealm();

    void createUser(String username, String onset, String uuid, String fake, String accessToken);

    String getUserIdValue(String username, String accessToken);

    void resetPassword(String userId, String password, String accessToken);

    String getAccessTokenToBackend(String username, String password);

    void deleteUser(String userId, String accessToken);
}
