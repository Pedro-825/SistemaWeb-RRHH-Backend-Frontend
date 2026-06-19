package com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IContratoFactory {
    Contrato crearIndefinido(Long idEmpleado, Integer idDpto, String nombreDepartamento,
                              String cargo, LocalDate fechaInicio, LocalDate fechaFin,
                              BigDecimal sueldo);
    Contrato crearPracticas(Long idEmpleado, Integer idDpto, String nombreDepartamento,
                             String cargo, LocalDate fechaInicio, LocalDate fechaFin,
                             BigDecimal sueldo);
}
