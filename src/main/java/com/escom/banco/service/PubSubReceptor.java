package com.escom.banco.service;

import com.escom.banco.model.Cuenta;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.PubsubMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PubSubReceptor implements MessageReceiver, CommandLineRunner {

    private final BancoService bancoService;
    private final Storage storage;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String bucketName = System.getenv("GCS_BUCKET");
    private final String rolNodo = System.getenv("ROL_NODO");

    public PubSubReceptor(BancoService bancoService, Storage storage) {
        this.bancoService = bancoService;
        this.storage = storage;
    }

    @Override
    public void run(String... args) throws Exception {
        if ("REPLICA".equals(rolNodo)) {
            ejecutarCatchUp();
        }
    }

    private void ejecutarCatchUp() {
        System.out.println("Iniciando algoritmo de Catch-up...");
        long secuenciaLocal = bancoService.getSecuencia();
        long siguienteSecuencia = secuenciaLocal + 1;

        while (true) {
            try {
                Blob blob = storage.get(bucketName, siguienteSecuencia + ".json");
                if (blob == null) {
                    break;
                }

                String json = new String(blob.getContent(), StandardCharsets.UTF_8);
                procesarJsonTransaccion(json);
                bancoService.setSecuencia(siguienteSecuencia);
                
                siguienteSecuencia++;
            } catch (Exception e) {
                System.err.println("Error en Catch-up en secuencia " + siguienteSecuencia + ": " + e.getMessage());
                break;
            }
        }
        System.out.println("Catch-up completado. Sincronizado en secuencia: " + bancoService.getSecuencia());
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String json = message.getData().toStringUtf8();
            Map<String, Object> tx = objectMapper.readValue(json, Map.class);
            long secuenciaMsg = Long.parseLong(String.valueOf(tx.get("secuencia")));

            synchronized (bancoService) {
                if (secuenciaMsg == bancoService.getSecuencia() + 1) {
                    procesarJsonTransaccion(json);
                    bancoService.setSecuencia(secuenciaMsg);
                } else if (secuenciaMsg > bancoService.getSecuencia() + 1) {
                    ejecutarCatchUp();
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje de PubSub: " + e.getMessage());
        } finally {
            consumer.ack();
        }
    }

    private void procesarJsonTransaccion(String json) throws Exception {
        Map<String, Object> tx = objectMapper.readValue(json, Map.class);
        String origenId = String.valueOf(tx.get("sourceAccountId"));
        String destinoId = String.valueOf(tx.get("targetAccountId"));
        double monto = Double.parseDouble(String.valueOf(tx.get("amount")));

        bancoService.procesarTransferenciaLocal(origenId, destinoId, monto);
    }
}