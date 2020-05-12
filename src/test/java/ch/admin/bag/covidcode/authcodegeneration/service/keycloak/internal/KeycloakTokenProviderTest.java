package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import ch.admin.bag.covidcode.authcodegeneration.service.keycloak.KeycloakAdminService;
import ch.admin.bag.covidcode.authcodegeneration.service.keycloak.KeycloakTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakTokenProviderTest {

    private static final String FAKE_NOT_FAKE = "0";

    @Mock
    private KeycloakAdminService keycloakAdminService;
    @InjectMocks
    private KeycloakTokenProvider testee;

    private static final String TEST_ACCESS_TOKEN = "QRMwjii77";


    @Test
    public void test_createToken() {
        //given
        when(keycloakAdminService.getAccessTokenToMasterRealm()).thenReturn(TEST_ACCESS_TOKEN);
        when(keycloakAdminService.getAccessTokenToBackend(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);

        //when
        String token = testee.createToken("2020-08-15", FAKE_NOT_FAKE);
        //then
        assertNotNull(token);
    }

}
