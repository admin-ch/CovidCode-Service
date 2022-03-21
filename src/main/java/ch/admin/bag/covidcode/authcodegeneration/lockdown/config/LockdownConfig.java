package ch.admin.bag.covidcode.authcodegeneration.lockdown.config;

import ch.admin.bag.covidcode.authcodegeneration.lockdown.LockdownInterceptor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import java.util.Collections;
import java.util.List;


@Profile("lockdown")
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "lockdown")
public class LockdownConfig implements WebMvcConfigurer {

    @NestedConfigurationProperty
    private List<Endpoint> endpoints = Collections.emptyList();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LockdownInterceptor(endpoints));
    }

    @PostConstruct
    public void validate() throws ConfigurationException {
        if (endpoints.isEmpty()) {
            log.warn("no active lockdowns - please consider deactivating profile 'lockdown'!");
        } else {
            for (Endpoint endpoint : endpoints) {
                if (endpoint.getApplicable().isEmpty()) {
                    log.warn("no active lockdowns for uri '{}' - please consider removing this endpoint!", endpoint.getUri());
                } else {
                    for (Endpoint.FromUntil fromUntil : endpoint.getApplicable()) {
                        if (fromUntil.getUntil()==null && fromUntil.getFrom()==null) {
                            log.warn("endlessly active lockdowns for endpoint '{}'  - please consider removing this from/until range!", endpoint.getUri());
                        } else if (fromUntil.getUntil()!=null && fromUntil.getFrom()!=null) {
                            if (!fromUntil.getFrom().isBefore(fromUntil.getUntil())) {
                                log.error("endpoint '{}': invalid active range from '{}' until '{}'", endpoint.getUri(), fromUntil.getFrom(), fromUntil.getUntil());
                                throw new ConfigurationException("Invalid range from="+fromUntil.getFrom()+" until="+fromUntil.getUntil()+" for Endpoint '"+endpoint.getUri()+"'");
                            }
                        }
                    }
                }
            }
        }
    }
}
