package com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class ContratoFactory implements IContratoFactory {

    @Override
    public Contrato crearIndefinido(Long idEmpleado, Integer idDpto, String nombreDepartamento,
                                     String cargo, LocalDate fechaInicio, LocalDate fechaFin,
                                     BigDecimal sueldo) {
        return Contrato.builder()
                .idEmpleado(idEmpleado)
                .idDpto(idDpto)
                .nombreDepartamento(nombreDepartamento)
                .cargo(cargo)
                .tipoContrato("INDEFINIDO")
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .sueldo(sueldo)
                .estado("ACTIVO")
                .build();
    }

    @Override
    public Contrato crearPracticas(Long idEmpleado, Integer idDpto, String nombreDepartamento,
                                    String cargo, LocalDate fechaInicio, LocalDate fechaFin,
                                    BigDecimal sueldo) {
        return Contrato.builder()
                .idEmpleado(idEmpleado)
                .idDpto(idDpto)
                .nombreDepartamento(nombreDepartamento)
                .cargo(cargo)
                .tipoContrato("PRACTICAS")
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .sueldo(sueldo)
                .estado("ACTIVO")
                .build();
    }
}
