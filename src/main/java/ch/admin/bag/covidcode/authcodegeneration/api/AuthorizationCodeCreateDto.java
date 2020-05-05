package ch.admin.bag.covidcode.authcodegeneration.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "Dto with information for creating an authorization code.")
public class AuthorizationCodeCreateDto {

    @Schema(description = "Infection date")
    private LocalDate onsetDate;

}
