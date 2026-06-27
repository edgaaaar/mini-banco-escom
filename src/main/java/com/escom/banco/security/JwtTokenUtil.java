package com.escom.banco.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtTokenUtil {

    private final String claveSecreta = System.getenv("JWT_SECRET");
    private final long tiempoExpiracion = 3600000;

    public String generarToken(String usuario) {
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(usuario)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracion))
                .signWith(SignatureAlgorithm.HS256, claveSecreta)
                .compact();
    }
}