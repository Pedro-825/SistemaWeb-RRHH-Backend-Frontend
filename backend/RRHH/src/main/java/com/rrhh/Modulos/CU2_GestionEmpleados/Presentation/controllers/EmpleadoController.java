package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.IEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes.EmpleadoApiRoutes;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaHistorialRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.config.security.AuthenticatedUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping(EmpleadoApiRoutes.EMPLEADO_BASE)
public class EmpleadoController {

    private final IEmpleadoService empleadoService;
    private final JpaHistorialRepository historialRepository;
    private final EmailService emailService;
    private final JpaEmpleadoRepository jpaEmpleadoRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;

    public EmpleadoController(IEmpleadoService empleadoService, JpaHistorialRepository historialRepository,
                              EmailService emailService, JpaEmpleadoRepository jpaEmpleadoRepository,
                              JpaUsuarioRepository jpaUsuarioRepository) {
        this.empleadoService = empleadoService;
        this.historialRepository = historialRepository;
        this.emailService = emailService;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    private ResponseEntity<EmpleadoResponseDTO> responder(EmpleadoResponseDTO dto) {
        return dto.isSuccess() ? ResponseEntity.ok(dto) : ResponseEntity.badRequest().body(dto);
    }

    @PostMapping(EmpleadoApiRoutes.REGISTRAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> registrarEmpleado(
            @Valid @RequestBody RegistrarEmpleadoRequestDTO dto
    ) {
        return responder(empleadoService.registrarEmpleado(dto));
    }

    @GetMapping(EmpleadoApiRoutes.LISTAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> listarEmpleados(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.listarEmpleados(pageable);
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleados(
            @RequestParam String filtro,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.buscarEmpleados(filtro, pageable);
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR_AVANZADO)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public Page<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idDpto,
            @RequestParam(required = false) String cargo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return empleadoService.buscarEmpleadosAvanzado(
                estado,
                idDpto,
                cargo,
                pageable
        );
    }

    @PutMapping(EmpleadoApiRoutes.ACTUALIZAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> actualizarEmpleado(
            @PathVariable Long idEmpleado,
            @Valid @RequestBody ActualizarEmpleadoRequestDTO dto
    ) {
        return responder(empleadoService.actualizarEmpleado(idEmpleado, dto));
    }

    @PutMapping(EmpleadoApiRoutes.DESACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> desactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody DesactivarEmpleadoRequestDTO dto
    ) {
        return responder(empleadoService.desactivarEmpleado(idEmpleado, dto));
    }

    @PutMapping(EmpleadoApiRoutes.SANCION)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> registrarSancion(
            @PathVariable Long idEmpleado,
            @Valid @RequestBody RegistrarSancionRequestDTO dto
    ) {
        return responder(empleadoService.registrarSancion(idEmpleado, dto));
    }

    @PutMapping(EmpleadoApiRoutes.REACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> reactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody ReactivarEmpleadoRequestDTO dto
    ) {
        return responder(empleadoService.reactivarEmpleado(idEmpleado, dto));
    }

    @PutMapping(EmpleadoApiRoutes.ASCENSO)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> registrarAscenso(
            @PathVariable Long idEmpleado,
            @RequestBody RegistrarAscensoRequestDTO dto,
            @AuthenticationPrincipal AuthenticatedUser usuarioActual
    ) {
        Long idEmpleadoActor = usuarioActual != null ? usuarioActual.getIdEmpleado() : null;
        String rolActor = usuarioActual != null ? usuarioActual.getRol() : null;
        return responder(empleadoService.registrarAscenso(idEmpleado, dto, idEmpleadoActor, rolActor));
    }

    @PutMapping(EmpleadoApiRoutes.CAMBIO_SALARIAL)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<EmpleadoResponseDTO> registrarCambioSalarial(
            @PathVariable Long idEmpleado,
            @RequestBody CambioSalarialRequestDTO dto
    ) {
        return responder(empleadoService.registrarCambioSalarial(idEmpleado, dto));
    }

    @PostMapping("/{idEmpleado}/enviar-app")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> enviarLinkApp(@PathVariable Long idEmpleado) {
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado))
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        if (empleado.getCorreo() == null || empleado.getCorreo().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El empleado no tiene correo registrado."));
        }
        try {
            emailService.enviarLinkDescargaApp(
                    empleado.getCorreo(),
                    empleado.getNombres() + " " + empleado.getApellidos()
            );
            return ResponseEntity.ok(Map.of("message", "Enlace enviado correctamente."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "No se pudo enviar el correo."));
        }
    }

    @PostMapping("/{idEmpleado}/confirmar-app")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> confirmarAppInstalada(@PathVariable Long idEmpleado) {
        UsuarioModel usuario = jpaUsuarioRepository.findByEmpleadoIdEmpleado(idEmpleado).orElse(null);
        if (usuario == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Usuario no encontrado."));
        }
        usuario.setAppMovilInstalada(true);
        jpaUsuarioRepository.save(usuario);
        return ResponseEntity.ok(Map.of("message", "App confirmada."));
    }

    @GetMapping(EmpleadoApiRoutes.HISTORIAL)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public ResponseEntity<List<Map<String, Object>>> getHistorialEmpleado(
            @PathVariable Long idEmpleado) {
        // AuditoriaEmpleadoService registra los cambios del empleado bajo distintos
        // "tablaAfectada" segun que se modifico (perfil, familiares, horario, etc.),
        // no solo "EMPLEADO". Hay que incluirlos todos para que el historial este completo.
        List<HistorialAuditoriaModel> historial = historialRepository
                .findByIdRegistroAfectadoAndTablaAfectadaIn(
                        idEmpleado,
                        List.of("EMPLEADO", "EMPLEADO_DERECHO_HABIENTES", "FAMILIA_INFO", "ASIGNACION_HORARIO"),
                        Sort.by(Sort.Direction.DESC, "fechaDeCambio"));
        List<Map<String, Object>> result = historial.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAuditoria", h.getIdAuditoria());
            m.put("accion", h.getAccion());
            m.put("tablaAfectada", h.getTablaAfectada());
            m.put("valorAnterior", h.getValorAnterior());
            m.put("valorNuevo", h.getValorNuevo());
            m.put("ipMaquina", h.getIpMaquina());
            m.put("fechaDeCambio", h.getFechaDeCambio());
            m.put("creadoEl", h.getCreadoEl());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
