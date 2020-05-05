package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import lombok.*;

@AllArgsConstructor
@ToString
@Getter
public class UserDto {

    private String username;

    private String firstName;

    private String lastName;

    private UserAttributesDto attributes;

    private boolean enabled;

}
