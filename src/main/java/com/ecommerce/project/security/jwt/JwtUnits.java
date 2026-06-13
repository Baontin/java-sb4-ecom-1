package com.ecommerce.project.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUnits {

    private static final Logger logger = LoggerFactory.getLogger(JwtUnits.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpMs}")
    private int jwtExpirationMs;

    /* JWT structure (header - payload - signature)
     - header: Signing algorithm (HS256, RS256, etc.)
     - payload: user, username, roles, iat, exp,...
     - signature: a string-secret atleast 256-bits long (Ensures the token wasn't tampered with.)

     Format: Authorization: Bearer <Token>
     i.e: Authorization: Bearer <jlkdsfjkdslfldsfjldsjfjdfjlsfjl....>
     */

    // get Token from Header
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        logger.debug("Authorization Bearer: {}", bearerToken);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // get username from Token
    public String getUsernameFromJwtToken(String token) {
        // parser(): Used when reading / validating an existing JWT token.
        return Jwts.parser()
                .verifyWith((SecretKey) key()) // Check signature using our secret
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // generate JWT token from username
    public String generateJwtFromRequest(UserDetails userDetails) {
        String username = userDetails.getUsername();

        // builder(): Used when creating a new JWT token.
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key()) // Sign it securely
                .compact();
    }

    // generate SignKey
    public Key key() {
        return Keys.hmacShaKeyFor(
            // Decoders is a helper class from the jjwt library used to decode encoded strings.
            Decoders.BASE64.decode(jwtSecret)
        );
    }

    // validate JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(authToken);

            return true;
        } catch (MalformedJwtException e) {
            // the correct JWT format (e.g., corrupted, missing parts, or invalid Base64 encoding).
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // if the token string is null or empty, meaning there’s nothing to parse.
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
