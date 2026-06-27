package com.escom.banco.service;

import com.escom.banco.model.Cuenta;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BancoService {

    private final ConcurrentHashMap<String, Cuenta> cuentas = new ConcurrentHashMap<>();
    private final AtomicLong secuenciaActual = new AtomicLong(0);
    private final AtomicLong contadorTransferencias = new AtomicLong(0);
    private final AtomicLong contadorLecturas = new AtomicLong(0);

    public void cargarBaseDeDatosInicial(String rutaCsv) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaCsv))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 3) {
                    String id = datos[0].trim();
                    String propietario = datos[1].trim();
                    double balance = Double.parseDouble(datos[2].trim());
                    cuentas.put(id, new Cuenta(id, propietario, balance));
                }
            }
            System.out.println("====== SNAPSHOT INITIAL CARGADA: " + cuentas.size() + " CUENTAS ======");
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo CSV: " + e.getMessage());
        }
    }

    public Cuenta obtenerCuenta(String id) {
        contadorLecturas.incrementAndGet();
        return cuentas.get(id);
    }

    public boolean procesarTransferenciaLocal(String origenId, String destinoId, double monto) {
        Cuenta origen = cuentas.get(origenId);
        Cuenta destino = cuentas.get(destinoId);

        if (origen == null || destino == null || origen.getBalance() < monto) {
            return false;
        }

        synchronized (origen) {
            synchronized (destino) {
                if (origen.getBalance() < monto) return false;
                origen.setBalance(origen.getBalance() - monto);
                destino.setBalance(destino.getBalance() + monto);
            }
        }

        contadorTransferencias.incrementAndGet();
        return true;
    }

    public long getSecuencia() { return secuenciaActual.get(); }
    public void setSecuencia(long nuevaSecuencia) { this.secuenciaActual.set(nuevaSecuencia); }
    public long incrementarSecuencia() { return secuenciaActual.incrementAndGet(); }
    
    public long getContadorTransferencias() { return contadorTransferencias.get(); }
    public void incrementarContadorTransferencias() { this.contadorTransferencias.incrementAndGet(); }
    public long getContadorLecturas() { return contadorLecturas.get(); }
    
    public double calcularSaldoTotalSistema() {
        return cuentas.values().stream().mapToDouble(Cuenta::getBalance).sum();
    }
    
    public int getTotalCuentas() { return cuentas.size(); }
}