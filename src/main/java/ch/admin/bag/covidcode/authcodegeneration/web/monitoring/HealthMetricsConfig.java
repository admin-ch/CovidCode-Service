package ch.admin.bag.covidcode.authcodegeneration.web.monitoring;


import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class HealthMetricsConfig {

    @Bean
    public MeterRegistryCustomizer prometheusHealthCheck(HealthEndpoint healthEndpoint) {
        return registry -> registry.gauge("health", healthEndpoint, HealthMetricsConfig::healthToCode);
    }

    private static int healthToCode(HealthEndpoint ep) {
        Status status = ep.health().getStatus();
        return status.equals(Status.UP) ? 1 : 0;
    }

}
