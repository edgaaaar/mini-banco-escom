package com.escom.banco;

import com.escom.banco.service.BancoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BancoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoApplication.class, args);
    }

    @Bean
    public CommandLineRunner inicializarBanco(BancoService bancoService) {
        return args -> {
            String rutaCsv = System.getenv("SNAPSHOT_CSV");
            if (rutaCsv != null && !rutaCsv.isEmpty()) {
                bancoService.cargarBaseDeDatosInicial(rutaCsv);
            } else {
                System.out.println("====== ADVERTENCIA: Variable SNAPSHOT_CSV no definida ======");
            }
        };
    }
}