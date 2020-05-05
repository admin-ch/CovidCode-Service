package ch.admin.bag.covidcode.authcodegeneration.web.monitoring;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AuthorizationCodeCountMeterTest {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Test
    void bindTo_meterRegistry_ok() {
        AuthorizationCodeCountMeter authorizationCodeCountMeter = new AuthorizationCodeCountMeter(authorizationCodeRepository);
        authorizationCodeCountMeter.bindTo(meterRegistry);
        assertNotNull(authorizationCodeCountMeter);
    }
}
