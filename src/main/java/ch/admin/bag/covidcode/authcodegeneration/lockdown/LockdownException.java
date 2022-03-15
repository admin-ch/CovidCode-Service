package ch.admin.bag.covidcode.authcodegeneration.lockdown;

import org.springframework.core.NestedRuntimeException;

public class LockdownException extends NestedRuntimeException {

    public LockdownException(String path) {

        super(String.format("The access towards URI '%s' is deactivated", path));
    }
}
