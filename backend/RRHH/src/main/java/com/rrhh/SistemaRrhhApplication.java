package com.rrhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;
import java.util.Properties;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class SistemaRrhhApplication {

    public static void main(String[] args) {
        cargarDotEnv();
        SpringApplication.run(SistemaRrhhApplication.class, args);
    }

    private static void cargarDotEnv() {
        try (InputStream input = SistemaRrhhApplication.class
                .getClassLoader()
                .getResourceAsStream(".env")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                props.forEach((key, value) -> {
                    String k = key.toString().trim();
                    String v = value.toString().trim();
                    if (System.getProperty(k) == null) {
                        System.setProperty(k, v);
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }
}
