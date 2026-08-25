package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import java.time.LocalDate;

public class FamiliarRequestDTO {
    public String nombres;
    public String apellidos;
    public String parentesco;
    public String numeroDi;
    public LocalDate fechaNacimiento;
    public Boolean esDerechohabiente;
    public Boolean elegibleAsignacionFamiliar;
    public Boolean validado;
    public LocalDate coberturaDesde;
    public LocalDate coberturaHasta;
}
