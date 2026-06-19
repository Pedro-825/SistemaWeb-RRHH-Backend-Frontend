package com.rrhh.Modulos.CU5_Reporte.Application.factory;

import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;
import org.springframework.stereotype.Component;

@Component
public class ReporteFactory implements IReporteFactory {

    @Override
    public ReporteGenerado crearReporteBase() {
        return ReporteGenerado.builder()
                .totalRegistros(0)
                .build();
    }
}
