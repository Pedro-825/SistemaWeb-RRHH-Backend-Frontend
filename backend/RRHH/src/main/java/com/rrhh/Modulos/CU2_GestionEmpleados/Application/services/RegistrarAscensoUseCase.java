package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarAscensoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IDepartamentoRepository departamentoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IRolRepository rolRepository;

    private final RolAsignacionService rolAsignacionService;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    public RegistrarAscensoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IDepartamentoRepository departamentoRepository, IUsuarioGestionRepository usuarioRepository, IRolRepository rolRepository, RolAsignacionService rolAsignacionService, AuditoriaEmpleadoService auditoriaEmpleadoService) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.rolAsignacionService = rolAsignacionService;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            RegistrarAscensoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeRecibirAscenso()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede registrar ascenso a un empleado que no está activo",
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

        if (dto == null || dto.getNuevoCargo() == null || dto.getNuevoCargo().isBlank()) {
            return new EmpleadoResponseDTO(
                    false,
                    "Debe ingresar el nuevo cargo del empleado",
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

        String cargoAnterior = contrato.getCargo();
        String departamentoAnterior = contrato.getNombreDepartamento();

        Departamento departamentoNuevo = null;

        if (dto.getNuevoIdDpto() != null) {
            departamentoNuevo =
                    departamentoRepository.findById(dto.getNuevoIdDpto());

            if (departamentoNuevo == null) {
                throw new RuntimeException("Departamento nuevo no encontrado");
            }

            contrato.cambiarDepartamento(
                    departamentoNuevo.getIdDpto(),
                    departamentoNuevo.getNombre()
            );
        } else {
            departamentoNuevo = new Departamento();
            departamentoNuevo.setIdDpto(contrato.getIdDpto());
            departamentoNuevo.setNombre(contrato.getNombreDepartamento());
        }

        String nuevoCargo = dto.getNuevoCargo();

        contrato.cambiarCargo(nuevoCargo);

        Contrato contratoActualizado =
                contratoRepository.save(contrato);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        String rolAnterior = null;
        String rolNuevo = null;

        if (usuario != null) {

            rolAnterior = usuario.getNombreRol();

            String nombreRolCalculado =
                    rolAsignacionService.determinarRol(
                            departamentoNuevo.getNombre(),
                            nuevoCargo
                    );

            Rol rol =
                    rolRepository.findByNombreRol(nombreRolCalculado);

            if (rol == null) {
                throw new RuntimeException("Rol no encontrado: " + nombreRolCalculado);
            }

            usuario.setIdRol(rol.getIdRol());
            usuario.setNombreRol(rol.getNombreRol());

            usuarioRepository.save(usuario);

            rolNuevo = rol.getNombreRol();
        }

        String motivo = "Sin motivo especificado";

        if (dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        String fechaAscenso = dto.getFechaAscenso() != null
                ? dto.getFechaAscenso().toString()
                : "No especificada";

        String valorAnterior =
                "Cargo anterior: " + cargoAnterior
                        + " | Departamento anterior: " + departamentoAnterior
                        + " | Rol anterior: " + rolAnterior;

        String valorNuevo =
                "Nuevo cargo: " + contratoActualizado.getCargo()
                        + " | Nuevo departamento: " + contratoActualizado.getNombreDepartamento()
                        + " | Nuevo rol: " + rolNuevo
                        + " | Fecha ascenso: " + fechaAscenso
                        + " | Motivo: " + motivo;

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "ASCENSO_EMPLEADO",
                "CONTRATO",
                valorAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Ascenso registrado correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contratoActualizado.getCargo(),
                contratoActualizado.getNombreDepartamento(),
                rolNuevo,
                empleado.getEstado()
        );
    }
}