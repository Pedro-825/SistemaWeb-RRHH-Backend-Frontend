package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services;

import org.springframework.stereotype.Service;

@Service
public class RolAsignacionService {

    public String determinarRol(String nombreDepartamento, String cargo) {

        String departamento = nombreDepartamento == null
                ? ""
                : nombreDepartamento.trim().toUpperCase();

        String cargoEmpleado = cargo == null
                ? ""
                : cargo.trim().toUpperCase();

        if (
                cargoEmpleado.contains("DIRECTOR") ||
                cargoEmpleado.contains("DIRECTORA") ||
                cargoEmpleado.contains("GERENTE") ||
                cargoEmpleado.contains("GERENTA") ||
                cargoEmpleado.contains("JEFE") ||
                cargoEmpleado.contains("JEFA") ||
                cargoEmpleado.contains("ADMINISTRADOR") ||
                cargoEmpleado.contains("ADMINISTRADORA")
        ) {
            return "GERENCIA";
        }

        if (
                departamento.contains("RECURSOS HUMANOS") ||
                departamento.contains("RRHH")
        ) {
            return "RRHH";
        }

        return "EMPLEADO";
    }
}