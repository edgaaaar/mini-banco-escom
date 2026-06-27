package com.escom.banco;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GeneradorCarga {

    private static final String IP_LIDER = "http://34.30.110.59:8080"; 
    private static final int HILOS_CONCURRENTES = 20; // Fuerza de ataque
    private static final int DURACION_SEGUNDOS = 60; // 1 minuto de estrés

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 INICIANDO GENERADOR DE CARGA MASIVA - MINI BANCO DISTRIBUIDO");
        System.out.println("🎯 Objetivo: " + IP_LIDER);
        System.out.println("🧵 Hilos concurrentes: " + HILOS_CONCURRENTES);
        System.out.println("⏳ Duración de la prueba: " + DURACION_SEGUNDOS + " segundos\n");

        ExecutorService executor = Executors.newFixedThreadPool(HILOS_CONCURRENTES);
        HttpClient client = HttpClient.newHttpClient();
        Random random = new Random();
        long tiempoFin = System.currentTimeMillis() + (DURACION_SEGUNDOS * 1000);

        // Lanzar los hilos de ataque continuo
        for (int i = 0; i < HILOS_CONCURRENTES; i++) {
            executor.execute(() -> {
                while (System.currentTimeMillis() < tiempoFin) {
                    try {
                        int probabilidad = random.nextInt(100);
                        // Generar cuentas aleatorias entre el rango de las 820,000 importadas
                        String cuentaOrigen = "CTA-" + (100000 + random.nextInt(820000));
                        String cuentaDestino = "CTA-" + (100000 + random.nextInt(820000));

                        if (probabilidad < 80) {
                            // 80% LECTURAS DE SALDO
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(IP_LIDER + "/api/accounts/" + cuentaOrigen))
                                    .GET()
                                    .build();
                            client.send(request, HttpResponse.BodyHandlers.discarding());
                        } else {
                            // 20% TRANSFERENCIAS LOCALES
                            String jsonPayload = String.format(
                                "{\"sourceAccountId\":\"%s\",\"targetAccountId\":\"%s\",\"amount\":%.2f}",
                                cuentaOrigen, cuentaDestino, (10.0 + (random.nextDouble() * 500.0))
                            );

                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(IP_LIDER + "/api/transactions/transfer"))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                                    .build();
                            client.send(request, HttpResponse.BodyHandlers.discarding());
                        }
                        // Pequeña pausa imperceptible para no saturar los hilos locales
                        Thread.sleep(5); 
                    } catch (Exception e) {
                        // Ignorar caídas esporádicas de red durante el estrés
                    }
                }
            });
        }

        // Contador visual en consola mientras corre el ataque
        for (int i = DURACION_SEGUNDOS; i > 0; i--) {
            System.out.print("\r🔥 Bombardeando el clúster... Quedan " + i + " segundos activos ");
            Thread.sleep(1000);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("\n\n🏁 ¡PRUEBA DE CARGA FINALIZADA CON ÉXITO!");
    }
}