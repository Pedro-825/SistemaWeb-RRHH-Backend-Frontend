package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.security.AuditoriaContext;

import org.springframework.stereotype.Service;

@Service
public class AuditoriaEmpleadoService {

    private final IHistorialRepository historialRepository;

    private final AuditoriaContext auditoriaContext;

    public AuditoriaEmpleadoService(IHistorialRepository historialRepository, AuditoriaContext auditoriaContext) {
        this.historialRepository = historialRepository;
        this.auditoriaContext = auditoriaContext;
    }

    public void registrar(
            Long idRegistroAfectado,
            String accion,
            String tablaAfectada,
            String valorAnterior,
            String valorNuevo
    ) {

        Long idUsuarioAuditoria =
                auditoriaContext.obtenerIdUsuarioActual();

        String ipAuditoria =
                auditoriaContext.obtenerIpActual();

        Historial historial = new Historial(
                idUsuarioAuditoria,
                idRegistroAfectado,
                accion,
                tablaAfectada,
                ipAuditoria,
                valorAnterior,
                valorNuevo
        );

        historialRepository.save(historial);
    }
}