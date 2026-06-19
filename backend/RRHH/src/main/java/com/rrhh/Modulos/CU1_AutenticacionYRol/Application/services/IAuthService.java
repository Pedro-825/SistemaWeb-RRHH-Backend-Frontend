package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.*;

public interface IAuthService {

    /**
     * Paso 3.2.3: Validar credenciales e IP para el log (RNF6)
     * @param dto Datos de inicio de sesión
     * @param ip IP capturada desde el controlador
     * @return AuthResponseDTO con estado del login y si requiere 2FA
     */
    AuthResponseDTO login(LoginRequestDTO dto, String ip);

    /**
     * Paso 3.2.6: Validar código 2FA (RNF2)
     * @param dto Usuario y código de verificación
     * @return TwoFactorResponseDTO con el Token JWT y Rol asignado
     */
    TwoFactorResponseDTO verify2FA(TwoFactorRequestDTO dto);


    byte[] enable2FA(String username) throws Exception;

    AuthResponseDTO changePassword(ChangePasswordRequestDTO dto, String username);

    AuthResponseDTO setPassword(String newPassword, String confirmPassword, String username);

    long getJwtExpirationSeconds();
}
