package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rrhh.Shared.config.RRHHProperties;
import java.math.BigDecimal;

@Service
public class CambioSalarialUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final RRHHProperties rrhhProperties;

    public CambioSalarialUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IUsuarioGestionRepository usuarioRepository, AuditoriaEmpleadoService auditoriaEmpleadoService, RRHHProperties rrhhProperties) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
        this.rrhhProperties = rrhhProperties;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            CambioSalarialRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeRecibirCambioSalarial()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede registrar cambio salarial a un empleado que no está activo",
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

        if (dto == null || dto.getNuevoSueldo() == null) {
            return new EmpleadoResponseDTO(
                    false,
                    "Debe ingresar el nuevo sueldo",
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

        if (dto.getNuevoSueldo().compareTo(rrhhProperties.getSalarioMinimo()) < 0) {
            return new EmpleadoResponseDTO(
                    false,
                    "El nuevo sueldo debe ser mayor o igual al salario minimo legal (S/ " + rrhhProperties.getSalarioMinimo() + ")",
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

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        if (contrato == null) {
            throw new RuntimeException("El empleado no tiene contrato activo");
        }

        BigDecimal sueldoAnterior = contrato.getSueldo();
        BigDecimal sueldoNuevo = dto.getNuevoSueldo();

        if (sueldoAnterior != null && sueldoAnterior.compareTo(sueldoNuevo) == 0) {
            return new EmpleadoResponseDTO(
                    false,
                    "El nuevo sueldo no puede ser igual al sueldo actual",
                    empleado.getIdEmpleado(),
                    empleado.getNombres() + " " + empleado.getApellidos(),
                    null,
                    null,
                    contrato.getCargo(),
                    contrato.getNombreDepartamento(),
                    null,
                    empleado.getEstado()
            );
        }

        contrato.cambiarSueldo(sueldoNuevo);

        Contrato contratoActualizado =
                contratoRepository.save(contrato);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        String motivo = "Sin motivo especificado";

        if (dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        String fechaCambio = dto.getFechaCambio() != null
                ? dto.getFechaCambio().toString()
                : "No especificada";

        String valorAnterior =
                "Sueldo anterior: " + sueldoAnterior
                        + " | Cargo: " + contratoActualizado.getCargo()
                        + " | Departamento: " + contratoActualizado.getNombreDepartamento();

        String valorNuevo =
                "Sueldo nuevo: " + sueldoNuevo
                        + " | Fecha cambio: " + fechaCambio
                        + " | Motivo: " + motivo;

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "CAMBIO_SALARIAL",
                "CONTRATO",
                valorAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Cambio salarial registrado correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contratoActualizado.getCargo(),
                contratoActualizado.getNombreDepartamento(),
                usuario != null ? usuario.getNombreRol() : null,
                empleado.getEstado()
        );
    }
}