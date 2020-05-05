package ch.admin.bag.covidcode.authcodegeneration.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:~/test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ActiveProfiles("local")
public class AuthorizationCodeRepositoryTest {

    @Autowired
    private AuthorizationCodeRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    public void findById_FoundOne() {
        //given
        AuthorizationCode initial = new AuthorizationCode("123456789000", LocalDate.now(), LocalDate.now(), ZonedDateTime.now());
        entityManager.persist(initial);
        //when
        Optional<AuthorizationCode> process = repository.findById(initial.getId());
        //then
        assertThat(process.isPresent(), is(true));
    }

}
