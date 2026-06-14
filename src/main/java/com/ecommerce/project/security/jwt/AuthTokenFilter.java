package com.ecommerce.project.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
- The AuthTokenFilter sets the SecurityContext for authenticated users
  when they send requests with a valid JWT.
- That means if a user is already logged in and making a request,
  the filter handles authentication automatically.
* */


// OncePerRequestFilter: Ensures the filter runs only once per request (even with multiple servlet mappings).
/*
* OncePerRequestFilter sets a marker (an attribute on the request object) after it runs.
  If the request comes back through the chain again, it checks the marker and skips execution.

  -->Result: your filter logic (like JWT validation) runs only once per request,
  no matter how many forwards/includes happen.
*/
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        try {
            // get Jwt
            String jwt = parseJwt(request);
            // check Jwt whether valid and non-null
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // take user-info from that
                UserDetails userDetails = userDetailsService.loadUserByUsername(jwt);
                // Mark that user was authenticated
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                // get extra info (IP address, session id,...) in request for user
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // set authentication in Context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java JWT: {}", jwt);
        return jwt;
    }
}
