package ch.admin.bag.covidcode.authcodegeneration.web.security;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/v1/onset/*", "/v2/onset/*", "/v1/authcode/*"})
public class HttpResponseHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Content-Security-Policy", "default-src 'self'");
        httpServletResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpServletResponse.setHeader("Feature-Policy", "microphone 'none'; payment 'none'; camera 'none'");
        chain.doFilter(request, response);
    }
}
