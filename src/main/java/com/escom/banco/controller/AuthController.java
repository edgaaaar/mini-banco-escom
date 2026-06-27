package com.escom.banco.controller;

import com.escom.banco.security.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final ConcurrentHashMap<String, String> usuariosDb = new ConcurrentHashMap<>();

    public AuthController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> credenciales) {
        String usuario = credenciales.get("username");
        String password = credenciales.get("password");

        if (usuario == null || password == null || usuariosDb.containsKey(usuario)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario invalido o ya existente"));
        }

        usuariosDb.put(usuario, password);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado con exito"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String usuario = credenciales.get("username");
        String password = credenciales.get("password");

        String passwordGuardada = usuariosDb.get(usuario);
        if (passwordGuardada == null || !passwordGuardada.equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas"));
        }

        String token = jwtTokenUtil.generarToken(usuario);
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", token);
        return ResponseEntity.ok(respuesta);
    }
}