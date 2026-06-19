package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SancionAutomaticaService {

    private final ISancionRepository sancionRepository;

    private final IEmpleadoRepository empleadoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IHistorialRepository historialRepository;

    public SancionAutomaticaService(ISancionRepository sancionRepository, IEmpleadoRepository empleadoRepository, IUsuarioGestionRepository usuarioRepository, IHistorialRepository historialRepository) {
        this.sancionRepository = sancionRepository;
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
    }

    /*
     * Para pruebas puedes usar:
     * @Scheduled(fixedRate = 60000)
     *
     * Para producción queda:
     * @Scheduled(cron = "0 0 0 * * *")
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void finalizarSancionesVencidas() {

        LocalDate hoy = LocalDate.now();

        List<Sancion> sancionesVencidas =
                sancionRepository.findActivasVencidasConBloqueo(hoy);

        for (Sancion sancion : sancionesVencidas) {

            Long idEmpleado = sancion.getIdEmpleado();

            if (idEmpleado == null) {
                continue;
            }

            Empleado empleado =
                    empleadoRepository.findById(idEmpleado);

            if (empleado == null) {
                continue;
            }

            String estadoAnterior = empleado.getEstado();

            sancion.finalizar();
            sancionRepository.save(sancion);

            List<Sancion> sancionesActivas =
                    sancionRepository.findActivasConBloqueoByEmpleado(idEmpleado);

            if (
                    sancionesActivas.isEmpty()
                    && !empleado.estaDesvinculado()
            ) {

                empleado.activar();
                empleadoRepository.save(empleado);

                UsuarioGestion usuario =
                        usuarioRepository.findByEmpleadoId(idEmpleado);

                if (usuario != null) {
                    usuario.activar();
                    usuarioRepository.save(usuario);
                }

                Historial historial = new Historial(
                        1L,
                        idEmpleado,
                        "REACTIVACION_AUTOMATICA",
                        "EMPLEADO",
                        "SISTEMA",
                        "Estado anterior: " + estadoAnterior,
                        "Estado nuevo: ACTIVO | Sanción finalizada automáticamente. ID sanción: "
                                + sancion.getIdSancion()
                );

                historialRepository.save(historial);
            }
        }
    }
}