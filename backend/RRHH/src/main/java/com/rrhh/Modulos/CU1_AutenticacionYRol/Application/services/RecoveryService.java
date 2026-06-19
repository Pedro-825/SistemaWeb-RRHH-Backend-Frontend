package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.ResetPasswordRequestDTO;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaPasswordResetTokenRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;

import com.rrhh.Shared.persistence.PasswordResetTokenModel;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecoveryService {

    private final JpaUsuarioRepository usuarioRepository;
    private final JpaPasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public RecoveryService(JpaUsuarioRepository usuarioRepository,
                           JpaPasswordResetTokenRepository tokenRepository,
                           EmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository   = tokenRepository;
        this.emailService      = emailService;
        this.passwordEncoder   = passwordEncoder;
    }

    @Transactional(timeout = 60)
    public RecoveryResponseDTO solicitarRecuperacion(RecoveryRequestDTO dto) {

        if (dto == null || dto.getIdentifier() == null || dto.getIdentifier().isBlank()) {
            return new RecoveryResponseDTO(false, "Debe ingresar su usuario o correo");
        }

        // findByIdentifier usa JOIN FETCH — el empleado ya está cargado en memoria
        UsuarioModel usuario = usuarioRepository
                .findByIdentifier(dto.getIdentifier().trim())
                .orElse(null);

        // No revelamos si el identificador existe o no (evita enumeración de usuarios)
        if (usuario == null) {
            return new RecoveryResponseDTO(
                    true,
                    "Si el usuario o correo está registrado, recibirá las instrucciones en su correo personal."
            );
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            return new RecoveryResponseDTO(false, "La cuenta está inactiva. Contacte con RRHH.");
        }

        // Correo personal del empleado (cargado por JOIN FETCH)
        String correoPersonal = (usuario.getEmpleado() != null && usuario.getEmpleado().getCorreo() != null)
                ? usuario.getEmpleado().getCorreo()
                : usuario.getCorreoInst();

        String token = UUID.randomUUID().toString();

        PasswordResetTokenModel resetToken = new PasswordResetTokenModel();
        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpiracion(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsado(false);
        tokenRepository.save(resetToken);

        String link = frontendUrl + "/reset-password?token=" + token;

        // El envío de correo va en try-catch: si falla no se revierte el token guardado
        try {
            emailService.enviarCorreoRecuperacion(correoPersonal, link);
        } catch (Exception e) {
            // El token quedó guardado; el usuario puede reintentar o contactar soporte
        }

        return new RecoveryResponseDTO(
                true,
                "Si el usuario o correo está registrado, recibirá las instrucciones en su correo personal."
        );
    }

    @Transactional
    public RecoveryResponseDTO cambiarContrasenia(ResetPasswordRequestDTO dto) {

        if (dto == null || dto.getToken() == null || dto.getToken().isBlank()) {
            return new RecoveryResponseDTO(false, "Token inválido");
        }

        if (dto.getNuevaContrasenia() == null || dto.getNuevaContrasenia().isBlank()) {
            return new RecoveryResponseDTO(false, "Debe ingresar una nueva contraseña");
        }

        if (!dto.getNuevaContrasenia().equals(dto.getConfirmarContrasenia())) {
            return new RecoveryResponseDTO(false, "Las contraseñas no coinciden");
        }

        PasswordResetTokenModel resetToken = tokenRepository
                .findByToken(dto.getToken())
                .orElse(null);

        if (resetToken == null) {
            return new RecoveryResponseDTO(false, "Token inválido");
        }

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            return new RecoveryResponseDTO(false, "El token ya fue utilizado");
        }

        if (resetToken.getExpiracion().isBefore(LocalDateTime.now())) {
            return new RecoveryResponseDTO(false, "El token ha expirado");
        }

        UsuarioModel usuario = resetToken.getUsuario();
        usuario.setContrasenia(passwordEncoder.encode(dto.getNuevaContrasenia()));
        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueada(false);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        return new RecoveryResponseDTO(true, "Contraseña actualizada correctamente");
    }
}
