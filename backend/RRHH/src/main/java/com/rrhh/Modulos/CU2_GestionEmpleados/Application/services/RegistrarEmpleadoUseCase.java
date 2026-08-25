package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;

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

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory.IContratoFactory;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Shared.persistence.HorarioModel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rrhh.Shared.security.HashService;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistrarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IDepartamentoRepository departamentoRepository;

    private final IRolRepository rolRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final NombreUsuarioService nombreUsuarioService;

    private final HashService hashService;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final IContratoFactory contratoFactory;

    private final HorarioService horarioService;

    public RegistrarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IDepartamentoRepository departamentoRepository, IRolRepository rolRepository, IUsuarioGestionRepository usuarioRepository, NombreUsuarioService nombreUsuarioService, HashService hashService, AuditoriaEmpleadoService auditoriaEmpleadoService, IContratoFactory contratoFactory, HorarioService horarioService) {
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.departamentoRepository = departamentoRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.nombreUsuarioService = nombreUsuarioService;
        this.hashService = hashService;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
        this.contratoFactory = contratoFactory;
        this.horarioService = horarioService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(RegistrarEmpleadoRequestDTO dto) {

        if (empleadoRepository.existsByNumeroDi(dto.getNumeroDi())) {
            return new EmpleadoResponseDTO(
                    false,
                    "Ya existe un empleado con ese documento de identidad",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()
                && empleadoRepository.existsByCorreo(dto.getCorreo())) {
            return new EmpleadoResponseDTO(
                    false,
                    "Ya existe un empleado registrado con ese correo",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        Departamento departamento =
                departamentoRepository.findById(dto.getIdDpto());

        if (departamento == null) {
            throw new RuntimeException("Departamento no encontrado");
        }

        Empleado empleado = new Empleado();

        empleado.setNombres(dto.getNombres());
        empleado.setApellidos(dto.getApellidos());
        empleado.setDocIdentidad(dto.getDocIdentidad());
        empleado.setNumeroDi(dto.getNumeroDi());
        empleado.setFechaNac(dto.getFechaNac());
        empleado.setSexo(dto.getSexo());
        empleado.setEstadoCivil(dto.getEstadoCivil());
        empleado.setDireccion(dto.getDireccion());
        empleado.setCorreo(dto.getCorreo());
        empleado.setTelefono(dto.getTelefono());
        empleado.setEstado("ACTIVO");

        Empleado empleadoGuardado =
                empleadoRepository.save(empleado);

        String tipoContratoFinal = (dto.getTipoContrato() != null && !dto.getTipoContrato().isBlank())
                ? dto.getTipoContrato()
                : "INDEFINIDO";

        LocalDate fechaInicioContrato = dto.getFechaInicio() != null
                ? dto.getFechaInicio()
                : LocalDate.now();

        Contrato contrato = contratoFactory.crearIndefinido(
                empleadoGuardado.getIdEmpleado(),
                departamento.getIdDpto(),
                departamento.getNombre(),
                dto.getCargo(),
                fechaInicioContrato,
                dto.getFechaFin(),
                dto.getSueldo()
        );
        contrato.setTipoContrato(tipoContratoFinal);

        contratoRepository.save(contrato);

        Rol rol =
                rolRepository.findByNombreRol(dto.getRol());

        if (rol == null) {
            throw new RuntimeException("Rol no encontrado: " + dto.getRol());
        }

        String nombreUsuarioGenerado =
                nombreUsuarioService.generarNombreUsuarioUnico(
                        dto.getNombres(),
                        dto.getApellidos()
                );

        String correoInstitucionalGenerado =
                nombreUsuarioService.generarCorreoInstitucional(
                        nombreUsuarioGenerado
                );

        UsuarioGestion usuario = new UsuarioGestion();

        usuario.setNombreUsuario(nombreUsuarioGenerado);
        usuario.setCorreoInst(correoInstitucionalGenerado);
        usuario.setContrasenia(hashService.encriptar(dto.getNumeroDi()));
        usuario.setActivo(true);
        usuario.setDosfaActivo(false);
        usuario.setIdEmpleado(empleadoGuardado.getIdEmpleado());
        usuario.setIdRol(rol.getIdRol());
        usuario.setNombreRol(rol.getNombreRol());

        usuarioRepository.save(usuario);

        // Asignar horario predeterminado (Turno Mañana o el primer turno activo)
        List<HorarioModel> horarios = horarioService.listar();
        if (!horarios.isEmpty()) {
            HorarioModel predeterminado = horarios.stream()
                    .filter(h -> h.getNombreTurno() != null && h.getNombreTurno().toLowerCase().contains("mañana"))
                    .findFirst()
                    .orElse(horarios.get(0));
            horarioService.asignarHorario(
                    empleadoGuardado.getIdEmpleado(),
                    predeterminado.getIdHorario(),
                    LocalDate.now(),
                    null,
                    false
            );
        }

        auditoriaEmpleadoService.registrar(
                empleadoGuardado.getIdEmpleado(),
                "REGISTRO_EMPLEADO",
                "EMPLEADO",
                null,
                "Empleado registrado: "
                        + empleadoGuardado.getNombres()
                        + " "
                        + empleadoGuardado.getApellidos()
                        + " | Usuario: "
                        + nombreUsuarioGenerado
                        + " | Correo institucional: "
                        + correoInstitucionalGenerado
                        + " | Cargo: "
                        + contrato.getCargo()
                        + " | Departamento: "
                        + departamento.getNombre()
                        + " | Rol: "
                        + rol.getNombreRol()
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado registrado correctamente",
                empleadoGuardado.getIdEmpleado(),
                empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos(),
                nombreUsuarioGenerado,
                correoInstitucionalGenerado,
                contrato.getCargo(),
                departamento.getNombre(),
                rol.getNombreRol(),
                empleadoGuardado.getEstado()
        );
    }
}
