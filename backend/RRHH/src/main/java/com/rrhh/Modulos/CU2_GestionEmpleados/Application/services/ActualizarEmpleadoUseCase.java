package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualizarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IDepartamentoRepository departamentoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    public ActualizarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IDepartamentoRepository departamentoRepository, IUsuarioGestionRepository usuarioRepository, AuditoriaEmpleadoService auditoriaEmpleadoService) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            ActualizarEmpleadoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeSerEditado()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede editar un empleado desvinculado",
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

        String valorAnterior =
                "fechaNac=" + empleado.getFechaNac()
                        + ", sexo=" + empleado.getSexo()
                        + ", estadoCivil=" + empleado.getEstadoCivil()
                        + ", direccion=" + empleado.getDireccion()
                        + ", correo=" + empleado.getCorreo()
                        + ", telefono=" + empleado.getTelefono();

        if (dto.getFechaNac() != null) {
            empleado.setFechaNac(dto.getFechaNac());
        }

        if (dto.getSexo() != null && !dto.getSexo().isBlank()) {
            empleado.setSexo(dto.getSexo());
        }

        if (dto.getEstadoCivil() != null && !dto.getEstadoCivil().isBlank()) {
            empleado.setEstadoCivil(dto.getEstadoCivil());
        }

        if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
            empleado.setDireccion(dto.getDireccion());
        }

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()
                && !dto.getCorreo().equalsIgnoreCase(empleado.getCorreo())) {
            if (empleadoRepository.existsByCorreo(dto.getCorreo())) {
                return new EmpleadoResponseDTO(
                        false,
                        "Ya existe un empleado registrado con ese correo",
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
            empleado.setCorreo(dto.getCorreo());
        }

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            empleado.setTelefono(dto.getTelefono());
        }

        Empleado empleadoActualizado =
                empleadoRepository.save(empleado);

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        String cambiosContrato = "";

        if (contrato != null) {
            if (dto.getSueldo() != null && dto.getSueldo().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contrato.cambiarSueldo(dto.getSueldo());
                cambiosContrato += ", sueldo=" + dto.getSueldo();
            }

            if (dto.getTipoContrato() != null && !dto.getTipoContrato().isBlank()) {
                contrato.setTipoContrato(dto.getTipoContrato());
                cambiosContrato += ", tipoContrato=" + dto.getTipoContrato();
            }

            if (dto.getIdDpto() != null) {
                Departamento dep = departamentoRepository.findById(dto.getIdDpto());
                if (dep != null) {
                    contrato.cambiarDepartamento(dep.getIdDpto(), dep.getNombre());
                    cambiosContrato += ", idDpto=" + dto.getIdDpto();
                }
            }

            if (!cambiosContrato.isEmpty()) {
                contratoRepository.save(contrato);
            }
        }

        String valorNuevo =
                "fechaNac=" + empleadoActualizado.getFechaNac()
                        + ", sexo=" + empleadoActualizado.getSexo()
                        + ", estadoCivil=" + empleadoActualizado.getEstadoCivil()
                        + ", direccion=" + empleadoActualizado.getDireccion()
                        + ", correo=" + empleadoActualizado.getCorreo()
                        + ", telefono=" + empleadoActualizado.getTelefono()
                        + cambiosContrato;

        auditoriaEmpleadoService.registrar(
                empleadoActualizado.getIdEmpleado(),
                "ACTUALIZACION_EMPLEADO",
                "EMPLEADO",
                valorAnterior,
                valorNuevo
        );

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        return new EmpleadoResponseDTO(
                true,
                "Empleado actualizado correctamente",
                empleadoActualizado.getIdEmpleado(),
                empleadoActualizado.getNombres() + " " + empleadoActualizado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleadoActualizado.getEstado()
        );
    }
}
