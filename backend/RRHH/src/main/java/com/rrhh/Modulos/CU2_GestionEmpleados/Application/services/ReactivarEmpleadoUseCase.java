
package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReactivarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final ISancionRepository sancionRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IContratoRepository contratoRepository;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final JpaUsuarioRepository jpaUsuarioRepository;

    private final HorarioService horarioService;

    public ReactivarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, ISancionRepository sancionRepository, IUsuarioGestionRepository usuarioRepository, IContratoRepository contratoRepository, AuditoriaEmpleadoService auditoriaEmpleadoService, JpaUsuarioRepository jpaUsuarioRepository, HorarioService horarioService) {
        this.empleadoRepository = empleadoRepository;
        this.sancionRepository = sancionRepository;
        this.usuarioRepository = usuarioRepository;
        this.contratoRepository = contratoRepository;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.horarioService = horarioService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            ReactivarEmpleadoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (empleado.estaActivo()) {
            return new EmpleadoResponseDTO(
                    false,
                    "El empleado ya se encuentra activo",
                    empleado.getIdEmpleado(),
                    empleado.getNombres() + " " + empleado.getApellidos(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    empleado.getEstado()
            );
        }

        String estadoAnterior = empleado.getEstado();

        String motivo = "Reactivación manual";

        if (dto != null && dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        List<Sancion> sancionesActivas =
                sancionRepository.findActivasConBloqueoByEmpleado(idEmpleado);

        for (Sancion sancion : sancionesActivas) {
            sancion.finalizar();
            sancionRepository.save(sancion);
        }

        empleado.activar();

        Empleado empleadoGuardado =
                empleadoRepository.save(empleado);

        horarioService.cerrarAsignacionesActivas(idEmpleado, LocalDate.now());

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        if (usuario != null) {
            usuario.activar();
            // Al reactivar se le pide reconfigurar el 2FA desde cero (como si fuera
            // primer acceso), en vez de arrastrar el secreto de la vinculacion anterior.
            usuario.setDosfaActivo(false);
            usuarioRepository.save(usuario);

            jpaUsuarioRepository.findByEmpleadoIdEmpleado(idEmpleado).ifPresent(model -> {
                model.setTokenInvalidoDesde(null);
                jpaUsuarioRepository.save(model);
            });
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        auditoriaEmpleadoService.registrar(
                empleadoGuardado.getIdEmpleado(),
                "REACTIVACION_EMPLEADO",
                "EMPLEADO",
                "Estado anterior: " + estadoAnterior,
                "Estado nuevo: ACTIVO | Motivo: " + motivo
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado reactivado correctamente. Acceso al sistema habilitado.",
                empleadoGuardado.getIdEmpleado(),
                empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleadoGuardado.getEstado()
        );
    }
}
