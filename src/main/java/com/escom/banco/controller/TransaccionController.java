package com.escom.banco.controller;

import com.escom.banco.model.Cuenta;
import com.escom.banco.service.BancoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class TransaccionController {

    private final BancoService bancoService;

    public TransaccionController(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> obtenerSaldo(@PathVariable String id) {
        Cuenta cuenta = bancoService.obtenerCuenta(id);
        if (cuenta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cuenta);
    }

    @PostMapping("/transactions/transfer")
    public ResponseEntity<?> transferir(@RequestBody Map<String, Object> payload) {
        String origenId = String.valueOf(payload.get("sourceAccountId"));
        String destinoId = String.valueOf(payload.get("targetAccountId"));
        double monto = Double.parseDouble(String.valueOf(payload.get("amount")));

        boolean exito = bancoService.procesarTransferenciaLocal(origenId, destinoId, monto);
        if (!exito) {
            return ResponseEntity.badRequest().body(Map.of("error", "Transferencia invalida o fondos insuficientes"));
        }

        return ResponseEntity.ok(Map.of("mensaje", "Transferencia procesada localmente"));
    }
}