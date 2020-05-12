package ch.admin.bag.covidcode.authcodegeneration.web.monitoring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * This will secure the prometheus endpoint with base auth. As it has order highest precedence it will
 * ignore any other security configuration for this endpoint
 * If this behavior is not desired, set a property authcodegeneration.monitor.prometheus.secure to disable this configuration
 * <p>
 * You can also enable the profile 'disableprometheussecurity' to get rid of this configuration
 */
@Configuration
@ConditionalOnProperty(value = "authcodegeneration.monitor.prometheus.secure", matchIfMissing = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActuatorSecurity {

    private static final String PROMETHEUS_ROLE = "PROMETHEUS";

    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 9)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ActuatorWebmvcSecurity extends WebSecurityConfigurerAdapter {

        @Value("${authcodegeneration.monitor.prometheus.user}")
        private String user;
        @Value("${authcodegeneration.monitor.prometheus.password}")
        private String password;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatcher(org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toAnyEndpoint()).
                    authorizeRequests().
                        requestMatchers(org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to(HealthEndpoint.class)).permitAll().
                        requestMatchers(org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to(InfoEndpoint.class)).permitAll().
                        requestMatchers(org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.to(PrometheusScrapeEndpoint.class)).hasRole(PROMETHEUS_ROLE).
                        anyRequest().denyAll().
                    and().
                    httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser(user).password(password).roles(PROMETHEUS_ROLE);
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class ActuatorWebfluxSecurity {

        @Value("${authcodegeneration.monitor.prometheus.user}")
        private String user;
        @Value("${authcodegeneration.monitor.prometheus.password}")
        private String password;

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE + 9)
        public SecurityWebFilterChain actuatorSecurityWebFilterChain(ServerHttpSecurity http) {
            http.securityMatcher(org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.toAnyEndpoint()).
                httpBasic(c -> c.authenticationManager(createReactivePrometheusAuthenticationManager(user, password))).
                authorizeExchange().
                    matchers(org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.to(HealthEndpoint.class)).permitAll().
                    matchers(org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.to(InfoEndpoint.class)).permitAll().
                    matchers(org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest.to(PrometheusScrapeEndpoint.class)).hasRole(PROMETHEUS_ROLE).
                    anyExchange().denyAll();
            return http.build();
        }

        private ReactiveAuthenticationManager createReactivePrometheusAuthenticationManager(String prometheusUserName, String prometheusUserPassword) {
            return new UserDetailsRepositoryReactiveAuthenticationManager(
                        new MapReactiveUserDetailsService(
                                User.withUsername(prometheusUserName).password(prometheusUserPassword).roles(PROMETHEUS_ROLE).build()));
        }

    }

}
