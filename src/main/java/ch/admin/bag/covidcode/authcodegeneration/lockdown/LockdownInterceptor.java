package ch.admin.bag.covidcode.authcodegeneration.lockdown;

import ch.admin.bag.covidcode.authcodegeneration.lockdown.config.Endpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LockdownInterceptor implements HandlerInterceptor {

    private final List<Endpoint> endpoints;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        isUriLocked(LocalDateTime.now(), request.getRequestURI(), endpoints);

        return true;
    }

    protected boolean isUriLocked(LocalDateTime now, String requestUri, List<Endpoint> endpoints) {

        String uri = removeTrailingLeading(requestUri, '/');
        log.debug("intercepting call to uri '{}'", uri);

        Optional<Endpoint.FromUntil> result = endpoints.stream()
                // check for restrictions on given url
                .filter(endpoint -> sameUri(uri, endpoint))
                // change to the endpoints range restrictions
                .flatMap(endpoint -> endpoint.getApplicable().stream())
                // and check if range is active
                .filter(fromUntil -> isInRange(now, fromUntil))
                .findFirst();

        // any result rejects the call
        if (result.isPresent()) {
            throw new LockdownException(requestUri);
        }

        return false;
    }

    private boolean isInRange(LocalDateTime now, Endpoint.FromUntil fromUntil) {

        LocalDateTime from = (fromUntil.getFrom() == null ? LocalDateTime.MIN : fromUntil.getFrom());
        LocalDateTime until = (fromUntil.getUntil() == null ? LocalDateTime.MAX : fromUntil.getUntil());

        boolean isBetween = Range.between(from, until).contains(now);
        return isBetween;
    }

    private boolean sameUri(String requestUri, Endpoint endpoint) {

        boolean sameUri = false;

        if (requestUri != null && endpoint != null) {
            String endpointUri = removeTrailingLeading(endpoint.getUri(), '/');
            sameUri = (requestUri.equalsIgnoreCase(endpointUri));
        }

        return sameUri;
    }

    private String removeTrailingLeading(String text, Character character) {

        String result = text;

        if (result != null) {
            result = StringUtils.trimLeadingCharacter(result, character);
            result = StringUtils.trimTrailingCharacter(result, character);
        }

        return result;
    }
}
