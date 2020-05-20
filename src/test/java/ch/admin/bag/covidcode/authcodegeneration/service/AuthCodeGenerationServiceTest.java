package ch.admin.bag.covidcode.authcodegeneration.service;

import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeResponseDto;
import ch.admin.bag.covidcode.authcodegeneration.api.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCode;
import ch.admin.bag.covidcode.authcodegeneration.domain.AuthorizationCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCodeGenerationServiceTest {

    @Mock
    private AuthorizationCodeRepository repository;
    @InjectMocks
    private AuthCodeGenerationService testee;

    @Test
    void test_create() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now().minusWeeks(2));
        when(repository.existsByCode(anyString())).thenReturn(false);

        //when
        AuthorizationCodeResponseDto responseDto = testee.create(createDto);
        //then
        assertNotNull(responseDto.getAuthorizationCode());
        assertTrue(responseDto.getAuthorizationCode().matches("\\d{12}"));
        verify(repository, times(1)).saveAndFlush(ArgumentMatchers.any(AuthorizationCode.class));
    }

    @Test
    void test_create_code_already_exists() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now());
        when(repository.existsByCode(anyString())).thenReturn(true).thenReturn(false);

        //when
        AuthorizationCodeResponseDto responseDto = testee.create(createDto);
        //then
        assertNotNull(responseDto.getAuthorizationCode());
        assertTrue(responseDto.getAuthorizationCode().matches("\\d{12}"));
        verify(repository, times(1)).saveAndFlush(ArgumentMatchers.any(AuthorizationCode.class));
    }

    @Test
    void test_create_invalid_onset_date_in_future() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now().plusDays(1));
        //when
        //then
        assertThrows(ResponseStatusException.class, () -> testee.create(createDto));
    }

    @Test
    void test_create_invalid_onset_date_too_far_back() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.of(2017,7,7));
        //when
        //then
        assertThrows(ResponseStatusException.class, () -> testee.create(createDto));
    }

    @Test
    void test_create_invalid_onset_date_4_weeks_plus_one_day_back() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now().minusWeeks(4).minusDays(1));
        //when
        //then
        assertThrows(ResponseStatusException.class, () -> testee.create(createDto));
    }

    @Test
    void test_create_valid_onset_date_exactly_4_weeks_back() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now().minusWeeks(4));
        //when
        AuthorizationCodeResponseDto responseDto = testee.create(createDto);
        //then
        assertNotNull(responseDto.getAuthorizationCode());
        assertTrue(responseDto.getAuthorizationCode().matches("\\d{12}"));
        verify(repository, times(1)).saveAndFlush(ArgumentMatchers.any(AuthorizationCode.class));
    }

    @Test
    void test_create_valid_onset_date_exactly_now() {
        //given
        AuthorizationCodeCreateDto createDto = new AuthorizationCodeCreateDto(LocalDate.now());
        //when
        AuthorizationCodeResponseDto responseDto = testee.create(createDto);
        //then
        assertNotNull(responseDto.getAuthorizationCode());
        assertTrue(responseDto.getAuthorizationCode().matches("\\d{12}"));
        verify(repository, times(1)).saveAndFlush(ArgumentMatchers.any(AuthorizationCode.class));
    }
}
