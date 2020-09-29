package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:~/test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "CF_INSTANCE_INDEX=0"
})
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthCodeDeletionServiceITTest {

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Autowired
    private AuthCodeDeletionService authCodeDeletionService;

    @Test
    @Transactional
    void deleteOldAuthCode_foundTwo_deleteTwo() {
        //given
        String codeInThePast = "123456789123";
        String codeInTheFuture= "111111111111";
        authorizationCodeRepository.saveAndFlush(generateAuthCode(codeInThePast, ZonedDateTime.now().minusMinutes(1)));
        authorizationCodeRepository.saveAndFlush(generateAuthCode(codeInTheFuture, ZonedDateTime.now().plusMinutes(5)));
        assertThat(authorizationCodeRepository.findAll().size(), is(2));

        //when
        authCodeDeletionService.deleteOldAuthCode();

        //then
        assertThat(authorizationCodeRepository.findByCode(codeInThePast).isPresent(), is(false));
        assertThat(authorizationCodeRepository.findByCode(codeInTheFuture).isPresent(), is(true));
    }

    private AuthorizationCode generateAuthCode(String code, ZonedDateTime expiryDate){
        return new AuthorizationCode(code, LocalDate.now(), LocalDate.now().minusDays(3), expiryDate);
    }

}
