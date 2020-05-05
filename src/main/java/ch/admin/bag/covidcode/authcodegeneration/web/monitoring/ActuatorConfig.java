package ch.admin.bag.covidcode.authcodegeneration.web.monitoring;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActuatorConfig {
 
    @Bean
    AuthorizationCodeCountMeter dataSourceStatusProbe(AuthorizationCodeRepository authorizationCodeRepository) {
        return new AuthorizationCodeCountMeter(authorizationCodeRepository);
    }
}
