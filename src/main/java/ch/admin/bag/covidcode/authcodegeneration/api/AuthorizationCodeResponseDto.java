package ch.admin.bag.covidcode.authcodegeneration.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "Response dto with a 9 digit authorization code.")
public class AuthorizationCodeResponseDto {

    @Schema(description = "9 digit authorization code")
    private String authorizationCode;

}
