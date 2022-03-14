package ch.admin.bag.covidcode.authcodegeneration.lockdown;

import ch.admin.bag.covidcode.authcodegeneration.lockdown.config.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LockdownInterceptorTest {

    private LockdownInterceptor lockdownInterceptor;

    @BeforeEach
    private void init() {
        lockdownInterceptor =  new LockdownInterceptor(Collections.emptyList());
    }

    @Test
    public void withoutConfiguredRestrictions() {
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.now(), "any-uri", Collections.emptyList()));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MIN, "any-uri", Collections.emptyList()));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MAX, "any-uri", Collections.emptyList()));
    }

    @Test
    public void withNonUriMatchingRestrictions() {
        Endpoint endpoint = create("any-other-uri");
        List<Endpoint> endpoints = Arrays.asList(endpoint);

        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.now(), "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MIN, "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MAX, "any-uri", endpoints));
    }

    @Test
    public void withUriMatchingRestrictions_withoutFromUntil() {
        Endpoint endpoint = create("any-uri");
        List<Endpoint> endpoints = Arrays.asList(endpoint);

        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.now(), "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MIN, "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.MAX, "any-uri", endpoints));
    }

    @Test
    public void withUriMatchingRestrictions_withOpenMatchingFromUntil() {
        Endpoint endpoint = create("any-uri");
        LocalDateTime now = LocalDateTime.now();
        // from TOMORROW until end of time
        addFromUntil(endpoint, now.plusDays(1), null);
        // from start of time until YESTERDAY
        addFromUntil(endpoint, null, now.minusDays(1));

        List<Endpoint> endpoints = Arrays.asList(endpoint);

        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.now(), "any-uri", endpoints));
        assertExceptionWith(LocalDateTime.MIN, "any-uri", endpoints);
        assertExceptionWith(LocalDateTime.MAX, "any-uri", endpoints);
    }

    @Test
    public void withUriMatchingRestrictions_withMatchingFromUntil() {
        Endpoint endpoint = create("any-uri");
        LocalDateTime now = LocalDateTime.now();
        // from TOMORROW until end of time
        addFromUntil(endpoint, now.plusDays(1), LocalDateTime.MAX);
        // from start of time until YESTERDAY
        addFromUntil(endpoint, LocalDateTime.MIN, now.minusDays(1));

        List<Endpoint> endpoints = Arrays.asList(endpoint);

        assertFalse(lockdownInterceptor.isUriLocked(LocalDateTime.now(), "any-uri", endpoints));
        assertExceptionWith(LocalDateTime.MIN, "any-uri", endpoints);
        assertExceptionWith(LocalDateTime.MAX, "any-uri", endpoints);
    }

    @Test
    public void withUriMatchingRestrictions_withBlocksOfFromUntil() {
        Endpoint endpoint = create("any-uri");
        LocalDateTime now = LocalDateTime.now();
        // from +2 until end +4 days
        addFromUntil(endpoint, now.plusDays(2), now.plusDays(4));
        // from -4 until end +2 days
        addFromUntil(endpoint, now.minusDays(4), now.minusDays(2));

        List<Endpoint> endpoints = Arrays.asList(endpoint);

        // all points-in-time are outside of the locked blocks
        assertFalse(lockdownInterceptor.isUriLocked(now, "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(now.plusDays(5), "any-uri", endpoints));
        assertFalse(lockdownInterceptor.isUriLocked(now.minusDays(5), "any-uri", endpoints));

        // all points-in-time are inside of locked blocks
        assertExceptionWith(now.plusDays(3), "any-uri", endpoints);
        assertExceptionWith(now.minusDays(3), "any-uri", endpoints);
    }

    private void assertExceptionWith(LocalDateTime now, String requestUri, List<Endpoint> endpoints) {
        assertThrows(LockdownException.class, () -> {
            lockdownInterceptor.isUriLocked(now, requestUri, endpoints);
        });
    }

    private Endpoint create(String uri) {

        Endpoint endpoint = new Endpoint();
        endpoint.setUri(uri);
        endpoint.setApplicable(new ArrayList<>());

        return endpoint;
    }

    private Endpoint addFromUntil(Endpoint endpoint, LocalDateTime from, LocalDateTime until) {

        Endpoint.FromUntil fromUntil = new Endpoint.FromUntil();
        fromUntil.setFrom(from);
        fromUntil.setUntil(until);

        endpoint.getApplicable().add(fromUntil);

        return endpoint;
    }
}
