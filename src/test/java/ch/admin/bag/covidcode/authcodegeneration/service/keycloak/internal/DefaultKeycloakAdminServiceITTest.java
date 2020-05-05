package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import ch.admin.bag.covidcode.authcodegeneration.config.RestConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration(classes = {RestConfig.class})
@AutoConfigureWebClient
@SpringBootTest(classes = DefaultKeycloakAdminService.class)
@ActiveProfiles({"local"})
class DefaultKeycloakAdminServiceITTest {

    @Autowired
    DefaultKeycloakAdminService service;

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void getAccessTokenToMasterRealm_found() {
        String accessTokenToMasterRealm = service.getAccessTokenToMasterRealm();
        assertNotNull(accessTokenToMasterRealm);
    }

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void createUser_ok() {
        String token = service.getAccessTokenToMasterRealm();
        service.createUser("test12345", "2020-03-15", "0", "0", token);
        assertNotNull(token);
    }

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void getUserIdValue_found() {
        String token = service.getAccessTokenToMasterRealm();
        String userIdValue = service.getUserIdValue("test12345", token);
        assertNotNull(userIdValue);
    }

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void resetPassword_ok() {
        String token = service.getAccessTokenToMasterRealm();
        service.resetPassword("466f9968-6a7f-417a-b96a-9d9c2d9fb18a", "secret", token);
        assertNotNull(token);
    }

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void getAccessTokenToBackend_found() {
        String accessTokenToMasterRealm = service.getAccessTokenToBackend("test12345", "secret");
        assertNotNull(accessTokenToMasterRealm);
    }

    @Test
    @Disabled("Only runs locally for smoke tests.")
    public void deleteUser_ok() {
        String token = service.getAccessTokenToMasterRealm();
        service.deleteUser("466f9968-6a7f-417a-b96a-9d9c2d9fb18a", token);
        assertNotNull(token);
    }




}
