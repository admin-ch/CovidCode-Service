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
}
