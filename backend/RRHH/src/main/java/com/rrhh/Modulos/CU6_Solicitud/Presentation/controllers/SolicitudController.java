package com.rrhh.Modulos.CU6_Solicitud.Presentation.controllers;

import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.services.ISolicitudService;
import com.rrhh.Modulos.CU6_Solicitud.Application.services.SolicitudService;
import com.rrhh.Modulos.CU6_Solicitud.Presentation.routes.SolicitudApiRoutes;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SolicitudController {

    private final ISolicitudService solicitudService;
    private final SolicitudService solicitudServiceImpl;

    public SolicitudController(ISolicitudService solicitudService,
                                SolicitudService solicitudServiceImpl) {
        this.solicitudService = solicitudService;
        this.solicitudServiceImpl = solicitudServiceImpl;
    }

    @PostMapping(SolicitudApiRoutes.REGISTRAR)
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<?> registrar(
            @Valid @RequestBody RegistrarSolicitudRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            SolicitudResponseDTO response = solicitudService.registrarSolicitud(request, user.getIdUsuario());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(SolicitudApiRoutes.REVISAR_RRHH)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> revisarRrhh(
            @RequestBody RevisionRrhhRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            SolicitudResponseDTO response = solicitudService.revisarSolicitudRrhh(request, user.getIdUsuario());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(SolicitudApiRoutes.DECIDIR_GERENCIA)
    @PreAuthorize("hasRole('GERENCIA')")
    public ResponseEntity<?> decidirGerencia(
            @RequestBody DecisionGerenciaRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            SolicitudResponseDTO response = solicitudService.decidirSolicitudGerencia(request, user.getIdUsuario());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(SolicitudApiRoutes.POR_ESTADO)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(solicitudService.listarPorEstado(estado));
    }

    @GetMapping(SolicitudApiRoutes.POR_EMPLEADO)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorEmpleado(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(solicitudService.listarPorEmpleado(idEmpleado));
    }

    @GetMapping(SolicitudApiRoutes.BUSCAR_ID)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA', 'EMPLEADO')")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(solicitudService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(SolicitudApiRoutes.CLONAR)
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<?> clonar(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            SolicitudResponseDTO response = solicitudService.clonarSolicitud(id, user.getIdUsuario());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/v1/solicitudes/notificaciones/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH', 'GERENCIA')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarNotificaciones(
            @PathVariable Long idEmpleado) {
        return ResponseEntity.ok(solicitudServiceImpl.listarNotificacionesPorEmpleado(idEmpleado));
    }
}
