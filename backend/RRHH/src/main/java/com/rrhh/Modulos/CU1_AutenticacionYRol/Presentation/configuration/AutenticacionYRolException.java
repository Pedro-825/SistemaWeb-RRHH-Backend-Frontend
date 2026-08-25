package com.rrhh.Modulos.CU1_AutenticacionYRol.Presentation.configuration;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.AuthResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class AutenticacionYRolException{

    private static final Logger log = LoggerFactory.getLogger(AutenticacionYRolException.class);

    // Errores no controlados (500): el detalle tecnico se queda en el log del
    // servidor, nunca se manda al cliente -- evita filtrar nombres de tablas,
    // consultas o estructura interna a un endpoint publico como el login/recovery.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleGeneralException(Exception ex) {
        log.error("Error no controlado en modulo de autenticacion", ex);
        AuthResponseDTO response = new AuthResponseDTO(
            false,
            "Ocurrio un error inesperado. Intente nuevamente más tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    
    
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<AuthResponseDTO> handleDatabaseException(SQLException ex) {
        AuthResponseDTO response = new AuthResponseDTO(
            false, 
            "Error de conexión con la base de datos. Intente más tarde."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

   
   
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        AuthResponseDTO response = new AuthResponseDTO(
            false, 
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
