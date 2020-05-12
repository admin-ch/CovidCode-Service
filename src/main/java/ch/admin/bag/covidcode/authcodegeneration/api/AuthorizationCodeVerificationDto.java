package ch.admin.bag.covidcode.authcodegeneration.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthorizationCodeVerificationDto {

        private String authorizationCode;

        private String fake;
}
