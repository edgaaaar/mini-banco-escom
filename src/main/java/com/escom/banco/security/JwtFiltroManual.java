package com.escom.banco.security;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFiltroManual implements Filter {

    private final String claveSecreta = System.getenv("JWT_SECRET");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        if (path.equals("/api/register") || path.equals("/api/login") || path.contains("/api/metrics")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String usuario = Jwts.parser()
                        .setSigningKey(claveSecreta)
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();

                if (usuario != null) {
                    httpRequest.setAttribute("usuarioAutenticado", usuario);
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error de validacion de token: " + e.getMessage());
            }
        }

        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\": \"Acceso denegado, requiere JWT valido\"}");
    }
}