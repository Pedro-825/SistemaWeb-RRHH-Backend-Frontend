package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RegistrarSancionUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final ISancionRepository sancionRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IContratoRepository contratoRepository;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    public RegistrarSancionUseCase(IEmpleadoRepository empleadoRepository, ISancionRepository sancionRepository, IUsuarioGestionRepository usuarioRepository, IContratoRepository contratoRepository, AuditoriaEmpleadoService auditoriaEmpleadoService) {
        this.empleadoRepository = empleadoRepository;
        this.sancionRepository = sancionRepository;
        this.usuarioRepository = usuarioRepository;
        this.contratoRepository = contratoRepository;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            RegistrarSancionRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeSerSancionado()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede registrar sanción a un empleado desvinculado",
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

        if (dto == null || dto.getJustificacion() == null || dto.getJustificacion().isBlank()) {
            return new EmpleadoResponseDTO(
                    false,
                    "Debe ingresar una justificación para la sanción",
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
        boolean esSuspension = "SUSPENSION".equals(dto.getTipoSancion());
        int dias = dto.getDiasSuspension();

        Sancion sancion = new Sancion();

        int secuencia = ThreadLocalRandom.current().nextInt(1000, 9999);
        String codigo = "SANC-" + Year.now().getValue() + "-" + empleado.getIdEmpleado() + "-" + secuencia;
        sancion.setCodigo(codigo);

        sancion.setIdEmpleado(empleado.getIdEmpleado());
        sancion.setMotivo(dto.getJustificacion());
        sancion.setDescripcion("Tipo: " + dto.getTipoSancion());
        sancion.setFechaInicio(LocalDate.now());
        sancion.setFechaFin(esSuspension && dias > 0 ? LocalDate.now().plusDays(dias) : LocalDate.now());
        sancion.setBloqueaAcceso(esSuspension);
        sancion.setEstado("ACTIVA");

        Sancion sancionGuardada = null;
        int maxReintentos = 5;
        for (int intento = 0; intento < maxReintentos; intento++) {
            try {
                sancionGuardada = sancionRepository.save(sancion);
                break;
            } catch (Exception e) {
                if (intento == maxReintentos - 1) {
                    throw new RuntimeException("No se pudo generar un codigo unico para la sancion tras " + maxReintentos + " intentos", e);
                }
                secuencia = ThreadLocalRandom.current().nextInt(1000, 9999);
                sancion.setCodigo("SANC-" + Year.now().getValue() + "-" + empleado.getIdEmpleado() + "-" + secuencia);
            }
        }

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        if (usuario != null && esSuspension) {
            usuario.desactivar();
            usuarioRepository.save(usuario);

            empleado.suspender();
            empleado = empleadoRepository.save(empleado);
        }

        if (sancionGuardada == null) {
            throw new IllegalStateException("La sancion no pudo ser registrada.");
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        String valorNuevo =
                "Codigo: " + sancionGuardada.getCodigo()
                        + " | Sancion ID: " + sancionGuardada.getIdSancion()
                        + " | Tipo: " + dto.getTipoSancion()
                        + " | Motivo: " + sancionGuardada.getMotivo()
                        + " | Inicio: " + sancionGuardada.getFechaInicio()
                        + " | Fin: " + sancionGuardada.getFechaFin()
                        + " | Bloquea acceso: " + sancionGuardada.getBloqueaAcceso()
                        + " | Estado empleado nuevo: " + empleado.getEstado();

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "SANCION_EMPLEADO",
                "EMPLEADO",
                "Estado anterior: " + estadoAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Sanción registrada correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleado.getEstado()
        );
    }
}
