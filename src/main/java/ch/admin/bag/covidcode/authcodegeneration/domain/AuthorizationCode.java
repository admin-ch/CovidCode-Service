package ch.admin.bag.covidcode.authcodegeneration.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"code"}, name = "UQ_AUTHORIZATION_CODE_CODE")})
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA
@Getter
public class AuthorizationCode {

    @Id
    private UUID id;

    @NotNull
    @Size(min = 12, max = 12)
    private String code;

    @NotNull
    private LocalDate originalOnsetDate;

    @NotNull
    private LocalDate onsetDate;

    @NotNull
    private ZonedDateTime expiryDate;

    @NotNull
    private ZonedDateTime creationDateTime;

    @NotNull
    private Integer callCount;

    public AuthorizationCode(String code, LocalDate originalOnsetDate, LocalDate onsetDate, ZonedDateTime expiryDate) {
        this.id = UUID.randomUUID();
        this.creationDateTime = ZonedDateTime.now();
        this.code = code;
        this.originalOnsetDate = originalOnsetDate;
        this.onsetDate = onsetDate;
        this.expiryDate = expiryDate;
        this.callCount = 0;
    }

    public static AuthorizationCode createFake(){
        return new AuthorizationCode(null, LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 1), null);
    }

    @SuppressWarnings("findbugs:DLS_DEAD_LOCAL_STORE")
    public void incrementCallCount(){
        this.callCount++;
    }
}
