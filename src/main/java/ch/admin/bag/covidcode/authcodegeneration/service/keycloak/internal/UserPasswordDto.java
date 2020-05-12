package ch.admin.bag.covidcode.authcodegeneration.service.keycloak.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class UserPasswordDto {

    private String type;

    private boolean temporary;

    private String value;
}
