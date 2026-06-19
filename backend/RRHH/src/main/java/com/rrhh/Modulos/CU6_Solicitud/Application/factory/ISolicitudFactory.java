package com.rrhh.Modulos.CU6_Solicitud.Application.factory;

import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;

public interface ISolicitudFactory {
    Solicitud crearSolicitudVacaciones(String motivo, String archivoAdjunto);
    Solicitud crearSolicitudPermiso(String tipo, String motivo);
}
