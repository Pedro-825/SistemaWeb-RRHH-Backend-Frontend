package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.*;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IUsuarioRepository;
import com.rrhh.config.security.JwtConfig;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.TwoFactorService;

import java.util.List;


import org.springframework.stereotype.Service;

import com.rrhh.Shared.security.HashService;

@Service
public class AuthService implements IAuthService {

    private final JwtConfig jwtConfig;

    private final TwoFactorService twoFactorService;

    private final IUsuarioRepository usuarioRepository;

    private final IHistorialRepository historialRepository;

    private final HashService hashService;

    public AuthService(JwtConfig jwtConfig, TwoFactorService twoFactorService, IUsuarioRepository usuarioRepository, IHistorialRepository historialRepository, HashService hashService) {
        this.jwtConfig = jwtConfig;
        this.twoFactorService = twoFactorService;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
        this.hashService = hashService;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto, String ip) {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        dto.getNombreUsuario(),
                        dto.getNombreUsuario()
                );

        if (user == null) {
            return new AuthResponseDTO(
                    false,
                    "Credenciales invalidas"
            );
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            return new AuthResponseDTO(
                    false,
                    "Usuario inactivo o desvinculado. No tiene acceso al sistema."
            );
        }

        if (!user.puedeIntentarLogin()) {
            return new AuthResponseDTO(
                    false,
                    "Cuenta bloqueada por 5 minutos"
            );
        }

        if (hashService.verificar(dto.getPassword(), user.getContrasenia())) {

            user.resetearIntentos();
            usuarioRepository.save(user);

            Historial log =
                    new Historial(
                            user.getId(),
                            "LOGIN_EXITOSO",
                            ip
                    );
            log.setValorNuevo("Rol: " + user.getNombreRol());

            historialRepository.save(log);

            if (!user.isDosFAActivado()) {
                List<String> permisos = getPermisosPorRol(user.getNombreRol());
                String token = jwtConfig.generarToken(
                        user.getNombreUsuario(),
                        user.getNombreRol(),
                        user.getId(),
                        permisos,
                        user.getTokenInvalidoDesde()
                );
                boolean recordar2FA = !user.getNombreUsuario().equals("admin.rrhh");
                boolean esPrimerAcceso = user.getNumeroDi() != null
                        && dto.getPassword().equals(user.getNumeroDi());
                AuthResponseDTO resp = new AuthResponseDTO(
                        true,
                        "Acceso concedido.",
                        user.getNombreRol(),
                        false,
                        token,
                        recordar2FA
                );
                resp.setRequiereCambioPassword(esPrimerAcceso);
                return resp;
            }

            return new AuthResponseDTO(
                    true,
                    "2FA requerido",
                    user.getNombreRol(),
                    true
            );

        } else {

            user.registrarFallo();
            usuarioRepository.save(user);

            Historial logFallo =
                    new Historial(
                            user.getId(),
                            "LOGIN_FALLIDO",
                            ip
                    );
            logFallo.setValorNuevo("Rol: " + user.getNombreRol());

            historialRepository.save(logFallo);

            String msg =
                    user.isCuentaBloqueada()
                            ? "Cuenta bloqueada"
                            : "Contrasenia incorrecta";

            return new AuthResponseDTO(
                    false,
                    msg
            );
        }
    }

    @Override
    public TwoFactorResponseDTO verify2FA(TwoFactorRequestDTO dto) {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        dto.getUsername(),
                        dto.getUsername()
                );

        if (user == null) {
            return new TwoFactorResponseDTO(
                    false,
                    "Usuario no encontrado",
                    null,
                    null
            );
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            return new TwoFactorResponseDTO(
                    false,
                    "Usuario inactivo o desvinculado. No tiene acceso al sistema.",
                    null,
                    null
            );
        }

        if (user.getSecret2FA() == null || user.getSecret2FA().isBlank()) {
            return new TwoFactorResponseDTO(
                    false,
                    "El usuario no tiene 2FA activado",
                    null,
                    null
            );
        }

        if (!user.puedeIntentar2FA()) {
            return new TwoFactorResponseDTO(
                    false,
                    "Verificacion 2FA bloqueada por 5 minutos. Demasiados intentos fallidos.",
                    null,
                    null
            );
        }

        boolean codigoValido =
                twoFactorService.verificarCodigo(
                        user.getSecret2FA(),
                        Integer.parseInt(dto.getCode())
                );

        if (!codigoValido) {
            user.registrarFallo2FA();
            usuarioRepository.save(user);
            return new TwoFactorResponseDTO(
                    false,
                    "Codigo incorrecto. Intentos restantes: " + (5 - user.getIntentosFalla2fa()),
                    null,
                    null
            );
        }

        user.resetearIntentos2FA();
        usuarioRepository.save(user);

        List<String> permisos = getPermisosPorRol(user.getNombreRol());

        String token =
                jwtConfig.generarToken(
                        user.getNombreUsuario(),
                        user.getNombreRol(),
                        user.getId(),
                        permisos,
                        user.getTokenInvalidoDesde()
                );

        TwoFactorResponseDTO resp2fa = new TwoFactorResponseDTO(
                true,
                "Acceso concedido",
                token,
                user.getNombreRol()
        );
        return resp2fa;
    }

    @Override
    public long getJwtExpirationSeconds() {
        return jwtConfig.getExpirationTime() / 1000;
    }

    private List<String> getPermisosPorRol(String nombreRol) {
        return List.of(
            switch (nombreRol.toUpperCase()) {
                case "EMPLEADO" -> new String[]{"VER_ASISTENCIA", "REGISTRAR_SOLICITUD", "VER_NOMINA_PROPIA"};
                case "RRHH" -> new String[]{"GESTIONAR_EMPLEADOS", "CALCULAR_NOMINA", "VER_SANCIONES", "REVISAR_SOLICITUDES", "VER_REPORTES", "MARCAR_ASISTENCIA"};
                case "GERENCIA" -> new String[]{"DECIDIR_SOLICITUDES", "VER_REPORTES", "VER_EMPLEADOS"};
                default -> new String[]{};
            }
        );
    }

    @Override
    public AuthResponseDTO changePassword(ChangePasswordRequestDTO dto, String username) {

        Usuario user = usuarioRepository.findByUsername(username);

        if (user == null) {
            return new AuthResponseDTO(false, "Usuario no encontrado");
        }

        if (!hashService.verificar(dto.getOldPassword(), user.getContrasenia())) {
            return new AuthResponseDTO(false, "La contrasenia actual es incorrecta");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return new AuthResponseDTO(false, "Las contrasenias no coinciden");
        }

        user.setContrasenia(hashService.encriptar(dto.getNewPassword()));
        usuarioRepository.save(user);

        return new AuthResponseDTO(true, "Contrasenia actualizada correctamente");
    }

    @Override
    public AuthResponseDTO setPassword(String newPassword, String confirmPassword, String username) {
        Usuario user = usuarioRepository.findByUsername(username);
        if (user == null) {
            return new AuthResponseDTO(false, "Usuario no encontrado");
        }
        if (!newPassword.equals(confirmPassword)) {
            return new AuthResponseDTO(false, "Las contrasenias no coinciden");
        }
        if (newPassword.length() < 8) {
            return new AuthResponseDTO(false, "La contrasenia debe tener al menos 8 caracteres");
        }
        user.setContrasenia(hashService.encriptar(newPassword));
        usuarioRepository.save(user);
        return new AuthResponseDTO(true, "Contrasenia establecida correctamente");
    }

    @Override
    public byte[] enable2FA(String username) throws Exception {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        username,
                        username
                );

        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            throw new RuntimeException(
                    "Usuario inactivo o desvinculado. No puede activar 2FA."
            );
        }

        String secret =
                twoFactorService.generarSecret();

        user.setSecret2FA(secret);
        user.setDosFAActivado(true);

        usuarioRepository.save(user);

        String qrText =
                twoFactorService.generarQRUrl(
                        username,
                        secret
                );

        return twoFactorService.generarQRImagen(qrText);
    }
}
