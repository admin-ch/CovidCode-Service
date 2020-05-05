package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import lombok.*;

@AllArgsConstructor
@ToString
@Getter
public class UserAttributesDto {

    private String onset;

    private String uuid;

    private String fake;
}
