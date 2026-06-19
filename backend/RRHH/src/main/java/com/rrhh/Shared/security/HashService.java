package com.rrhh.Shared.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encriptar(String textoPlano) {
        return encoder.encode(textoPlano);
    }

    public boolean verificar(String textoPlano, String hash) {
        return encoder.matches(textoPlano, hash);
    }
}
