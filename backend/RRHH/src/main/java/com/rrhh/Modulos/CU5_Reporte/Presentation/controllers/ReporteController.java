package com.rrhh.Modulos.CU5_Reporte.Presentation.controllers;

import com.rrhh.Modulos.CU5_Reporte.Application.dto.*;
import com.rrhh.Modulos.CU5_Reporte.Application.services.IReporteService;
import com.rrhh.Modulos.CU5_Reporte.Presentation.routes.ReporteApiRoutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rrhh.config.security.AuthenticatedUser;

@RestController
public class ReporteController {

    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);
    private static final String ERROR_INTERNO_REPORTE =
            "No se pudo generar el reporte. Verifique los filtros e intente nuevamente.";

    private final IReporteService reporteService;

    public ReporteController(IReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping(ReporteApiRoutes.EMPLEADOS)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteEmpleados(
            @RequestBody FiltroEmpleadoDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            ReporteResponseDTO dto = reporteService.generarReporteEmpleados(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("EMPLEADOS", formato,
                        dto.getDatos());
                return buildFileResponse(archivo, "reporte_empleados", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error generando reporte de empleados", e);
            return ResponseEntity.internalServerError().body(ERROR_INTERNO_REPORTE);
        }
    }

    @PostMapping(ReporteApiRoutes.ASISTENCIA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteAsistencia(
            @RequestBody FiltroAsistenciaDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            ReporteResponseDTO dto = reporteService.generarReporteAsistencia(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("ASISTENCIA", formato,
                        dto.getDatos());
                return buildFileResponse(archivo, "reporte_asistencia", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error generando reporte de asistencia", e);
            return ResponseEntity.internalServerError().body(ERROR_INTERNO_REPORTE);
        }
    }

    @PostMapping(ReporteApiRoutes.NOMINA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteNomina(
            @RequestBody FiltroNominaDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            ReporteResponseDTO dto = reporteService.generarReporteNomina(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("NOMINA", formato,
                        dto.getDatos());
                return buildFileResponse(archivo, "reporte_nomina", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error generando reporte de nomina", e);
            return ResponseEntity.internalServerError().body(ERROR_INTERNO_REPORTE);
        }
    }

    @PostMapping(ReporteApiRoutes.SOLICITUDES)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestBody FiltroSolicitudDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            ReporteResponseDTO dto = reporteService.generarReporteSolicitudes(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("SOLICITUDES", formato,
                        dto.getDatos());
                return buildFileResponse(archivo, "reporte_solicitudes", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error generando reporte de solicitudes", e);
            return ResponseEntity.internalServerError().body(ERROR_INTERNO_REPORTE);
        }
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] archivo, String nombre, String formato) {
        String extension;
        MediaType mediaType;
        if ("PDF".equalsIgnoreCase(formato)) {
            extension = ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("CSV".equalsIgnoreCase(formato)) {
            extension = ".csv";
            mediaType = new MediaType("text", "csv");
        } else {
            extension = ".xlsx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", nombre + extension);
        headers.setContentLength(archivo.length);

        return ResponseEntity.ok().headers(headers).body(archivo);
    }

    private Long obtenerIdUsuario(Authentication authentication) {
        if (authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser.getIdUsuario();
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
