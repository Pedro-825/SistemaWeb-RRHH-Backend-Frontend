package com.rrhh.Modulos.CU6_Solicitud.Application.factory;

import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudFactory implements ISolicitudFactory {

    @Override
    public Solicitud crearSolicitudVacaciones(String motivo, String archivoAdjunto) {
        Solicitud solicitud = new Solicitud();
        solicitud.setTipoSolicitud("VACACIONES");
        solicitud.setEstado("PENDIENTE");
        solicitud.setMotivo(motivo);
        solicitud.setArchivoAdjunto(archivoAdjunto);
        return solicitud;
    }

    @Override
    public Solicitud crearSolicitudPermiso(String tipo, String motivo) {
        Solicitud solicitud = new Solicitud();
        solicitud.setTipoSolicitud(tipo);
        solicitud.setEstado("PENDIENTE");
        solicitud.setMotivo(motivo);
        return solicitud;
    }
}
