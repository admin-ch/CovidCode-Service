package ch.admin.bag.covidcode.authcodegeneration.lockdown.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Endpoint {

    private String uri;
    @NestedConfigurationProperty
    private List<FromUntil> applicable;

    @Data
    public static class FromUntil {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime from;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime until;
    }
}
