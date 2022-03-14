package ch.admin.bag.covidcode.authcodegeneration.lockdown;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Profile("lockdown")
@Slf4j
@ControllerAdvice
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(value = {LockdownException.class})
    protected ResponseEntity<Object> handleLockdownException(LockdownException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
}
