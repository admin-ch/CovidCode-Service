package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("local")
class AuthCodeDeletionServiceTest {

    @Mock
    private AuthorizationCodeRepository repository;

    @InjectMocks
    private AuthCodeDeletionService authCodeDeletionService;


    @Test
    void deleteOldAuthCode_foundTwo_deleteTwo() {
        //given
        List<AuthorizationCode> codes = new ArrayList<>();
        codes.add(createAuthorizationCode());
        codes.add(createAuthorizationCode());
        when(repository.findByExpiryDateBefore(any(ZonedDateTime.class))).thenReturn(codes);

        //when
        authCodeDeletionService.deleteOldAuthCode();

        //then
        verify(repository, times(2)).delete(any(AuthorizationCode.class));
    }

    private AuthorizationCode createAuthorizationCode() {
        AuthorizationCode authorizationCode = mock(AuthorizationCode.class);
        when(authorizationCode.getCreationDateTime()).thenReturn(ZonedDateTime.now());
        when(authorizationCode.getExpiryDate()).thenReturn(ZonedDateTime.now());
        when(authorizationCode.getOnsetDate()).thenReturn(LocalDate.now());
        when(authorizationCode.getOriginalOnsetDate()).thenReturn(LocalDate.now());
        return authorizationCode;
    }

    @Test
    void deleteOldAuthCode_foundNone_deleteNone() {
        //given
        List<AuthorizationCode> codes = new ArrayList<>();
        when(repository.findByExpiryDateBefore(any(ZonedDateTime.class))).thenReturn(codes);

        //when
        authCodeDeletionService.deleteOldAuthCode();

        //then
        verify(repository, never()).delete(any(AuthorizationCode.class));
    }
}
