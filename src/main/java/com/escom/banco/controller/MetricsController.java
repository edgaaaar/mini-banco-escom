package com.escom.banco.controller;

import com.escom.banco.service.BancoService;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MetricsController {

    private final BancoService bancoService;
    private final OperatingSystemMXBean osBean;

    public MetricsController(BancoService bancoService) {
        this.bancoService = bancoService;
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> obtenerMetricas() {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("rolNodo", System.getenv("ROL_NODO"));
        metrics.put("nodoId", System.getenv("NODO_ID"));
        metrics.put("totalCuentas", bancoService.getTotalCuentas());
        metrics.put("saldoTotal", bancoService.calcularSaldoTotalSistema());
        metrics.put("ultimaSecuencia", bancoService.getSecuencia());
        metrics.put("transferenciasRealizadas", bancoService.getContadorTransferencias());
        metrics.put("lecturasRealizadas", bancoService.getContadorLecturas());

        double cpuLoad = osBean.getCpuLoad() * 100;
        metrics.put("usoCpu", Double.isNaN(cpuLoad) || cpuLoad < 0 ? 0.1 : Math.round(cpuLoad * 10.0) / 10.0);

        long memoriaTotal = osBean.getTotalMemorySize();
        long memoriaLibre = osBean.getFreeMemorySize();
        long memoriaUsada = memoriaTotal - memoriaLibre;
        double porcentajeRam = ((double) memoriaUsada / memoriaTotal) * 100;
        metrics.put("usoRam", Math.round(porcentajeRam * 10.0) / 10.0);

        File disco = new File("/");
        long discoTotal = disco.getTotalSpace();
        long discoLibre = disco.getFreeSpace();
        long discoUsado = discoTotal - discoLibre;
        double porcentajeDisco = ((double) discoUsado / discoTotal) * 100;
        metrics.put("usoDisco", Math.round(porcentajeDisco * 10.0) / 10.0);

        return ResponseEntity.ok(metrics);
    }
}