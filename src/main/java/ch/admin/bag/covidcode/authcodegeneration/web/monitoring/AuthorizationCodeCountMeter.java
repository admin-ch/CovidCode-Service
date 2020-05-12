package ch.admin.bag.covidcode.authcodegeneration.web.monitoring;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class AuthorizationCodeCountMeter implements MeterBinder {
 
    private final String name;
    private final String description;

    private final AuthorizationCodeRepository authorizationCodeRepository;

    public AuthorizationCodeCountMeter(final AuthorizationCodeRepository authorizationCodeRepository) {
        Objects.requireNonNull(authorizationCodeRepository, "authorizationCodeRepository cannot be null");
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.name = "authcode_stats";
        this.description = "AuthorizationCode Statistics";
    }

    @Override
    public void bindTo(final MeterRegistry meterRegistry) {
        Gauge.builder(name, this, value -> authorizationCodeRepository.count() )
                .description(description)
                .baseUnit("count")
                .register(meterRegistry);
    }

}
