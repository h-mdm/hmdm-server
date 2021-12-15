/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.security.jwt;

import java.io.IOException;
import java.util.Date;

import com.google.inject.Singleton;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.domain.User;

/**
 * <p>A provider for JWT tokens.</p>
 *
 * @author isv
 */
@Singleton
public class TokenProvider {

    /**
     * <p>A logger used for logging various events encountered during the lifecycle.</p>
     */
    private final Logger log = LoggerFactory.getLogger("JWTAuth");

    private static final String TOKEN_KEY = "token";

    /**
     * <p>A secret key (configurable) which is used for JWT tokens generation.</p>
     */
    private final String secretKey;

    /**
     * <p>A period validity of tokens.</p>
     */
    private final long tokenValidityInMilliseconds;

    /**
     * <p>A period validity of tokens with "Remember Me" option enabled..</p>
     */
    private final long tokenValidityInMillisecondsForRememberMe;

    /**
     * <p>Constructs new <code>TokenProvider</code> instance with specified configuration.</p>
     */
    public TokenProvider() {
        this.secretKey = "20c68f0d9185b1d18cf6add1e8b491fd89529a44";
        this.tokenValidityInMilliseconds = 1000 * 86400; // 24 hours
        this.tokenValidityInMillisecondsForRememberMe = 1000 * 2592000L; // 30 days
    }

    /**
     * <p>Generates new JWT token for the specified authenticated principal.</p>
     *
     * @param user a representation of authenticated principal.
     * @param rememberMe an optional flag indicating if <code>Remember Me</code> option is enabled.
     * @return a generated JWT token which can be used for further authentications of the specified principal.
     */
    public String createToken(User user, Boolean rememberMe) throws IOException {
        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        return Jwts.builder()
            .setSubject(user.getLogin())
            .claim(TOKEN_KEY, user.getAuthToken())
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .setExpiration(validity)
            .compact();
    }

    /**
     * <p>Parses the specified JWT token into authenticated principal.</p>
     *
     * @param jwtToken a JWT token to be parsed.
     * @return an authenticated principal presentation constructed from the data provided by specified token.
     */
    User getAuthentication(String jwtToken) throws IOException {
        Claims claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(jwtToken)
            .getBody();

        String login = claims.getSubject();
        String authToken = claims.get(TOKEN_KEY).toString();
        User user = new User();
        user.setLogin(login);
        user.setAuthToken(authToken);

        return user;
    }

    /**
     * <p>Validates the specified authentication token provided by the client.</p>
     *
     * @param jwtToken a JWT authentication token to be validated.
     * @return <code>true</code> if specified token is valid; <code>false</code> otherwise.
     */
    boolean validateToken(String jwtToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace:", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace:", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace:", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace:", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace:", e);
        }
        return false;
    }
}
