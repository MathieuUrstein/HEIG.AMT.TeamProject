package ch.heigvd.gamification.web.filter;

import ch.heigvd.gamification.dao.ApplicationRepository;
import ch.heigvd.gamification.error.FilterError;
import ch.heigvd.gamification.model.Application;
import ch.heigvd.gamification.util.JWTUtils;
import ch.heigvd.gamification.util.URIs;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFilter implements Filter {
    private final ApplicationRepository applicationRepository;

    @Autowired
    public AuthenticationFilter(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI().substring(request.getContextPath().length());
        String token = JWTUtils.extractToken(request.getHeader("Authorization"));
        boolean authRequest = uri.equals(URIs.AUTH);

        if (uri.equals(URIs.REGISTER) || (authRequest && token == null)) {
            chain.doFilter(servletRequest, response);
            return;
        }

        if (token == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "No token");
            return;
        }

        try {
            DecodedJWT jwt = JWTUtils.verifyToken(token);

            // if token is not valid
            if (jwt == null) {
                if (authRequest) {
                    chain.doFilter(request, response);
                    return;
                }
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
                return;
            }

            if (authRequest) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Already authenticated");
                return;
            }

            Application app = applicationRepository.findByName(jwt.getSubject());
            servletRequest.setAttribute("application", app);

            chain.doFilter(request, response);
        } catch (JWTDecodeException exception) {
            // FIXME log
            System.out.println("Invalid JWT format");
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT format");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setHeader("Content-Type", "application/json");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(new FilterError(message)));
    }

    @Override
    public void destroy() {}
}
