package io.github.manojohnsons.financeapi.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.manojohnsons.financeapi.domain.model.User;
import io.github.manojohnsons.financeapi.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenServiceImpl implements TokenService {

    private final SecretKey secretKey;
    private final long expirationHours;
    private final String issuer;
    private final String zoneOffset;

    public TokenServiceImpl(
            @Value("${api.security.token.secret}") String jwtSecret,
            @Value("${api.security.token.expiration-hours}") long expirationHours,
            @Value("${api.security.token.issuer}") String issuer,
            @Value("${api.security.token.zone-offset}") String zoneOffset) {

        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.expirationHours = expirationHours;
        this.issuer = issuer;
        this.zoneOffset = zoneOffset;
    }

    @Override
    public String generateToken(User user) {
        var expirationTime = generateExpirationTime();

        return Jwts.builder()
                .issuer(this.issuer)
                .subject(user.getEmail())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expirationTime))
                .signWith(this.secretKey)
                .compact();
    }

    private Instant generateExpirationTime() {
        return LocalDateTime.now()
                .plusHours(this.expirationHours)
                .toInstant(ZoneOffset.of(this.zoneOffset));
    }
}
