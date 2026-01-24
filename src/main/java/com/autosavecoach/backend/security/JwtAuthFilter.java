package com.autosavecoach.backend.security;

import com.autosavecoach.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1️⃣ No token → allow request (signup/login/public APIs)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
//        System.out.println(token);
        String email = jwtService.extractEmail(token);
//        System.out.println(email);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            System.out.println("email is not null and authentication is not null");
            if (!jwtService.isTokenValid(token, email)) {
                throw new RuntimeException("Invalid JWT");
            }

//            System.out.println("token is validated");
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    Collections.emptyList()
            );
//            System.out.println(authentication);
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
//            System.out.println(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
//            System.out.println("check " + SecurityContextHolder.getContext().getAuthentication());

        }

        filterChain.doFilter(request, response);
    }
}

