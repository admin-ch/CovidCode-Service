package ch.admin.bag.covidcode.authcodegeneration.lockdown;

import ch.admin.bag.covidcode.authcodegeneration.lockdown.config.Endpoint;
import ch.admin.bag.covidcode.authcodegeneration.lockdown.config.LockdownConfig;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import javax.naming.ConfigurationException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class LockdownConfigTest {

    private LockdownConfig lockdownConfig;

    @BeforeEach
    private void init() {
        lockdownConfig =  new LockdownConfig();
    }

    @Test
    public void endpointWithRangeError() {
        List<Endpoint> endpoints = EndpointBuilder.have()
                .endpoint("xyz").fromUntil(LocalDateTime.MAX, LocalDateTime.MIN).build();

        lockdownConfig.setEndpoints(endpoints);

        assertThrows(ConfigurationException.class, () -> {
            lockdownConfig.validate();
        });
    }


    static class EndpointBuilder {
        private Map<String, List<Endpoint.FromUntil>> endpoints = new TreeMap<>();
        private List<Endpoint.FromUntil> fromUntils = new ArrayList<>();


        protected static EndpointBuilder have() {
            return new EndpointBuilder();
        }

        protected EndpointBuilder fromUntil(LocalDateTime from, LocalDateTime until) {

            Endpoint.FromUntil fromUntil = new Endpoint.FromUntil();
            fromUntil.setFrom(from);
            fromUntil.setUntil(until);
            fromUntils.add(fromUntil);

            return this;
        }

        protected EndpointBuilder endpoint(String uri) {

            List<Endpoint.FromUntil> current;
            if (endpoints.containsKey(uri)) {
                // endpoint followed by ranges
                current = endpoints.get(uri);
                if (current != fromUntils) {
                    current.addAll(fromUntils);
                }
                fromUntils = new ArrayList<>();
            } else {
                // ranges followed by endpoint
                endpoints.put(uri, fromUntils);
            }

            return this;
        }

        protected List<Endpoint> build() {
            List<Endpoint> result = new ArrayList<>();
            for (String uri : endpoints.keySet()) {
                Endpoint endpoint = new Endpoint();
                endpoint.setUri(uri);
                endpoint.setApplicable(endpoints.get(uri));
                result.add(endpoint);
            }

            this.endpoints = new TreeMap<>();
            this.fromUntils = new ArrayList<>();

            return result;
        }
    }
}
