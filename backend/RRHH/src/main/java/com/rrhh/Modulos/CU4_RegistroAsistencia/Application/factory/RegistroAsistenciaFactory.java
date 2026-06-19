package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.factory;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RegistroAsistenciaFactory implements IRegistroAsistenciaAppFactory {

    @Override
    public RegistroAsistencia crearAsistenciaPuntual() {
        return RegistroAsistencia.builder()
                .estado("PUNTUAL")
                .minutosTardanza(0)
                .minutosExtra(0)
                .horasTrabajadas(BigDecimal.ZERO)
                .build();
    }
}
