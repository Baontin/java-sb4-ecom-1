package com.ecommerce.project.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPoint.class);


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        logger.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        // ObjectMapper: Converts Java Map → JSON string
        /*
        response.getOutputStream() = “Where to send the data” (a channel/pipe)
        mapper.writeValue(...) = “Convert my Java object (Map) to JSON and send it through that pipe”

        --> Together, they are responsible for sending a clean JSON error response to the client.


        response.getOutputStream() is like a pipe (or a tunnel) that connects your Java code to the client's HTTP response.
        The ObjectMapper prepares the JSON (by converting your Map).
        response.getOutputStream() is the pipe through which that JSON is sent out to the client.

            HTTP/1.1 401 Unauthorized
            Content-Type: application/json

            {
              "status": 401,
              "error": "Unauthorized",
              "message": "JWT token is expired or invalid",
              "path": "/api/admin/hello"
            }
        * */
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
