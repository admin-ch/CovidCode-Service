package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import ch.admin.bag.covidcode.authcodegeneration.service.keycloak.KeycloakAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
@SuppressWarnings("squid:S2068")
public class DefaultKeycloakAdminService implements KeycloakAdminService {
    private static final String CALL_KEYCLOAK_LOG_MESSAGE = "Call keycloak with url {}";
    private static final String KEYCLOAK_RESPONSE_LOG_MESSAGE = "Keycloak Response: {}";
    private static final String EMPTY_RESPONSE_BODY_ERROR_MESSAGE = "Response Body is null";
    private static final String SERVICE_CALL_ERROR_MESSAGE = "Exception during call of service";
    private static final String RESPONSE_STATUS_IS = "Response Status is ";
    private static final String USERNAME = "username";
    private static final String PASSWORD_TYPE = "password";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_ID = "client_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String ADMIN_CLI = "admin-cli";
    private static final String TEMPORARY_NAME_JWT = "temporary_name_jwt";
    private static final String KEYCLOAK_USERNAME = "keycloak";
    private static final String PTA_APP_BACKEND = "pta-app-backend";
    private static final String USERS = "users";
    private static final String ADMIN = "admin";
    private static final String REALMS = "realms";

    private final RestTemplate restTemplate;

    @Value("${authcodegeneration.service.keycloak.realm}")
    private String realm;

    @Value("${authcodegeneration.service.keycloak.uri}")
    private String serviceUrl;

    @Value("${authcodegeneration.service.keycloak.masterPassword}")
    private String masterPassword;

    @Value("${authcodegeneration.service.keycloak.clientSecret}")
    private String clientSecret;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getAccessTokenToMasterRealm() {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).path("/realms/master/protocol/openid-connect/token/");

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        try {

            MultiValueMap<String, String> bodyValues= new LinkedMultiValueMap<>();
            bodyValues.add(GRANT_TYPE, PASSWORD_TYPE);
            bodyValues.add(USERNAME, KEYCLOAK_USERNAME);
            bodyValues.add(PASSWORD_TYPE, masterPassword);
            bodyValues.add(CLIENT_ID, ADMIN_CLI);
            ResponseEntity<String> response = getAccessToken(builder.toUriString(), bodyValues);

            log.debug(KEYCLOAK_RESPONSE_LOG_MESSAGE, response.getBody());

            HashMap<String, String> map = (HashMap<String, String>) mapper.readValue(response.getBody(), Map.class);
            String accessToken = map.get(ACCESS_TOKEN);
            log.debug("Returning access_token '{}'", accessToken);
            return accessToken;
        } catch (RestClientResponseException | JsonProcessingException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }

    }


    @Override
    public String getUserIdValue(String username, String accessToken) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).pathSegment(ADMIN).pathSegment(REALMS).pathSegment(realm).pathSegment(USERS).queryParam(USERNAME, username);

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

            if (!response.hasBody()) {
                throw new IllegalStateException(EMPTY_RESPONSE_BODY_ERROR_MESSAGE);
            }

            log.debug(KEYCLOAK_RESPONSE_LOG_MESSAGE, response.getBody());
            ArrayList<HashMap<String, String>> values = (ArrayList<HashMap<String, String>>) mapper.readValue(response.getBody(), List.class);

            String userId = values.get(0).get("id");

            log.debug("userId for user: '{}'", userId);

            return userId;
        } catch (RestClientResponseException | JsonProcessingException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void createUser(String username, String onset, String uuid, String fake, String accessToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).pathSegment(ADMIN).pathSegment(REALMS).pathSegment(realm).pathSegment(USERS);

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        log.debug("Create user with username '{}', onset '{}' and uuid '{}'.", username, onset, uuid);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(new UserDto(username, TEMPORARY_NAME_JWT, TEMPORARY_NAME_JWT, new UserAttributesDto(onset, uuid, fake), true)), headers);
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                throw new IllegalStateException(RESPONSE_STATUS_IS + response.getStatusCode());
            }

        } catch (RestClientResponseException | JsonProcessingException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void resetPassword(String userId, String password, String accessToken) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).pathSegment(ADMIN).pathSegment(REALMS).pathSegment(realm).pathSegment(USERS).pathSegment(userId).pathSegment("reset-password");

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        log.debug("Update password for userId '{}'.", userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setCacheControl(CacheControl.noCache());
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(new UserPasswordDto(PASSWORD_TYPE, false, password)), headers);
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.PUT, entity, String.class);

            log.debug(KEYCLOAK_RESPONSE_LOG_MESSAGE, response);

            if (!response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                throw new IllegalStateException(RESPONSE_STATUS_IS + response.getStatusCode());
            }
        } catch (RestClientResponseException | JsonProcessingException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }
    }

    @Override
    public String getAccessTokenToBackend(String username, String password) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).path("/realms/").pathSegment(realm).pathSegment("protocol").pathSegment("openid-connect").pathSegment("token");

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        try {
            MultiValueMap<String, String> bodyValues= new LinkedMultiValueMap<>();
            bodyValues.add(GRANT_TYPE, PASSWORD_TYPE);
            bodyValues.add(USERNAME, username);
            bodyValues.add(PASSWORD_TYPE, password);
            bodyValues.add(CLIENT_ID, PTA_APP_BACKEND);
            bodyValues.add(CLIENT_SECRET, clientSecret);
            ResponseEntity<String> response = getAccessToken(builder.toUriString(), bodyValues);

            log.debug(KEYCLOAK_RESPONSE_LOG_MESSAGE, response.getBody());

            HashMap<String, String> map = (HashMap<String, String>) mapper.readValue(response.getBody(), new TypeReference<Map<String, String>>() { });
            String accessToken = map.get(ACCESS_TOKEN);
            log.debug("Returning access_token '{}'", accessToken);
            return accessToken;
        } catch (RestClientResponseException | JsonProcessingException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void deleteUser(String userId, String accessToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUrl).pathSegment(ADMIN).pathSegment(REALMS).pathSegment(realm).pathSegment(USERS).pathSegment(userId);

        log.debug(CALL_KEYCLOAK_LOG_MESSAGE, builder.toUriString());

        log.debug("Delete user with userId '{}'.", userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, String.class);

            log.debug(KEYCLOAK_RESPONSE_LOG_MESSAGE, response);

            if (!response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                throw new IllegalStateException(RESPONSE_STATUS_IS + response.getStatusCode());
            }
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(SERVICE_CALL_ERROR_MESSAGE, e);
        }
    }

    private ResponseEntity<String> getAccessToken(String uri, MultiValueMap<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setCacheControl(CacheControl.noCache());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);

        if (!response.hasBody()) {
            throw new IllegalStateException(EMPTY_RESPONSE_BODY_ERROR_MESSAGE);
        }
        return response;
    }
}
