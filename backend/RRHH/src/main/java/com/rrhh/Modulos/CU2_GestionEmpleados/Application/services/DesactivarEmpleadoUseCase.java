package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DesactivarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final JpaUsuarioRepository jpaUsuarioRepository;

    public DesactivarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IUsuarioGestionRepository usuarioRepository, AuditoriaEmpleadoService auditoriaEmpleadoService, JpaUsuarioRepository jpaUsuarioRepository) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            DesactivarEmpleadoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (empleado.estaDesvinculado()) {
            return new EmpleadoResponseDTO(
                    false,
                    "El empleado ya se encuentra desvinculado",
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

        String motivo = "Sin motivo especificado";

        if (dto != null && dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        empleado.desvincular();

        Empleado empleadoGuardado =
                empleadoRepository.save(empleado);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        if (usuario != null) {
            usuario.desactivar();
            usuarioRepository.save(usuario);

            jpaUsuarioRepository.findByEmpleadoIdEmpleado(idEmpleado).ifPresent(model -> {
                model.setTokenInvalidoDesde(LocalDateTime.now());
                jpaUsuarioRepository.save(model);
            });
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        if (contrato != null) {
            contrato.resolver();
            contratoRepository.save(contrato);
        }

        auditoriaEmpleadoService.registrar(
                empleadoGuardado.getIdEmpleado(),
                "DESACTIVACION_EMPLEADO",
                "EMPLEADO",
                estadoAnterior,
                "DESVINCULADO | Motivo: " + motivo
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado desactivado correctamente. Acceso al sistema bloqueado.",
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