package com.rrhh.Modulos.CU3_Nomina.Application.factory;

import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import org.springframework.stereotype.Component;

@Component
public class NominaFactory implements INominaFactory {

    @Override
    public Nomina crearNominaBase() {
        return Nomina.builder()
                .estadoPago("CALCULADA")
                .build();
    }
}
