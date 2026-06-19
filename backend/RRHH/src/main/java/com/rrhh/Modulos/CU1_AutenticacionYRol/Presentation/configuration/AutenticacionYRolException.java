package com.rrhh.Modulos.CU1_AutenticacionYRol.Presentation.configuration;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.AuthResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class AutenticacionYRolException{

    
     
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleGeneralException(Exception ex) {
        AuthResponseDTO response = new AuthResponseDTO(
            false, 
            "Error interno: " + ex.getMessage()
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
