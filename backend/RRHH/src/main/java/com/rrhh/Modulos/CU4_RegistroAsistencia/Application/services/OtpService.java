package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final ConcurrentHashMap<String, OtpRecord> store = new ConcurrentHashMap<>();
    private static final long EXPIRACION_MINUTOS = 5;
    private static final int MAX_INTENTOS = 5;
    private static final SecureRandom random = new SecureRandom();

    public OtpResult generarOtp(String dni) {
        String sessionId = UUID.randomUUID().toString();
        String codigo = String.format("%06d", random.nextInt(1000000));
        store.put(sessionId, new OtpRecord(dni, codigo, LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS), MAX_INTENTOS));
        return new OtpResult(sessionId, codigo);
    }

    public OtpVerificacion verificarOtp(String sessionId, String codigo) {
        OtpRecord record = store.get(sessionId);
        if (record == null) {
            return new OtpVerificacion(VerificacionResult.EXPIRADO_O_INEXISTENTE, null);
        }
        if (LocalDateTime.now().isAfter(record.expiraEn)) {
            store.remove(sessionId);
            return new OtpVerificacion(VerificacionResult.EXPIRADO_O_INEXISTENTE, null);
        }
        if (record.intentosRestantes <= 0) {
            store.remove(sessionId);
            return new OtpVerificacion(VerificacionResult.SIN_INTENTOS, null);
        }
        if (!record.codigo.equals(codigo)) {
            record.intentosRestantes--;
            if (record.intentosRestantes <= 0) {
                store.remove(sessionId);
            }
            return new OtpVerificacion(VerificacionResult.INVALIDO, null);
        }
        store.remove(sessionId);
        return new OtpVerificacion(VerificacionResult.OK, record.dni);
    }

    @Scheduled(fixedRate = 30000)
    public void limpiarExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        Iterator<Map.Entry<String, OtpRecord>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OtpRecord> entry = it.next();
            if (ahora.isAfter(entry.getValue().expiraEn) || entry.getValue().intentosRestantes <= 0) {
                it.remove();
            }
        }
    }

    public enum VerificacionResult {
        OK, INVALIDO, EXPIRADO_O_INEXISTENTE, SIN_INTENTOS
    }

    /** dni es el DNI para el que se genero el OTP (solo presente si resultado == OK),
     * para que el llamador pueda confirmar que coincide con el empleado que intenta vincular. */
    public record OtpVerificacion(VerificacionResult resultado, String dni) {}

    public record OtpResult(String sessionId, String codigo) {}

    private static class OtpRecord {
        final String dni;
        final String codigo;
        final LocalDateTime expiraEn;
        int intentosRestantes;

        OtpRecord(String dni, String codigo, LocalDateTime expiraEn, int intentosRestantes) {
            this.dni = dni;
            this.codigo = codigo;
            this.expiraEn = expiraEn;
            this.intentosRestantes = intentosRestantes;
        }
    }
}
