package ch.admin.bag.covidcode.authcodegeneration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, UUID>, JpaSpecificationExecutor<AuthorizationCode> {

    Optional<AuthorizationCode> findByCode(String code);

    boolean existsByCode(String code);

    List<AuthorizationCode> findByExpiryDateBefore(ZonedDateTime now);
}
