package com.autosavecoach.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // 🔐 Secret key (later move to application.properties)
    private static final String SECRET_KEY = "WJg/xoyaitiigDgC879okx6I4NO8BJHJHg1/bnz9CjY=";

    // ⏱ Token validity (24 hours)
    private static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000;

    // 🔑 Convert SECRET_KEY to signing Key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate token using email
    public String generateToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // External email from token
    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic claim extractor
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claimResolver.apply(claims);
    }

    // Check if token expired
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Validate token against email
    public boolean isTokenValid(String token, String email){
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }
}
