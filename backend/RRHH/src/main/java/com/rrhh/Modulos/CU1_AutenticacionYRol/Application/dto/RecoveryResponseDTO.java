package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class RecoveryResponseDTO {

    private boolean success;
    private String message;

    public RecoveryResponseDTO() {
    }

    public RecoveryResponseDTO(
            boolean success,
            String message
    ) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}