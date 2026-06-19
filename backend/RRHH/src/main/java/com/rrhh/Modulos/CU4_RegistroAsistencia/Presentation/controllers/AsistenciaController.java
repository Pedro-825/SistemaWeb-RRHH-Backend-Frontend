package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.JustificacionService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/asistencia")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final JustificacionService justificacionService;

    @Value("${app.upload-dir:uploads/evidencias}")
    private String uploadDir;

    public AsistenciaController(AsistenciaService asistenciaService,
                                JustificacionService justificacionService) {
        this.asistenciaService = asistenciaService;
        this.justificacionService = justificacionService;
    }

    @PostMapping(value = "/upload-evidencia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH', 'GERENCIA')")
    public ResponseEntity<Map<String, String>> uploadEvidencia(
            @RequestParam("file") MultipartFile file) throws IOException {

        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);

        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(filename));

        String url = "/uploads/evidencias/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/marcar")
    public ResponseEntity<MarcarAsistenciaResponseDTO> marcarAsistencia(
            @RequestBody MarcarAsistenciaRequestDTO request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        MarcarAsistenciaResponseDTO response = asistenciaService.marcarAsistencia(request, ip);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/justificar")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<?> justificarTardanza(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long idRegistro = Long.valueOf(Objects.requireNonNull(body.get("idRegistroAsistencia"), "idRegistroAsistencia es requerido").toString());
        String motivo = Objects.requireNonNull(body.get("motivo"), "motivo es requerido").toString();
        String evidencia = Objects.toString(body.get("evidenciaUrl"), "");
        Long idEmpleado = Long.valueOf(Objects.requireNonNull(body.get("idEmpleado"), "idEmpleado es requerido").toString());
        JustificacionTardanzaModel j = justificacionService.registrar(idRegistro.intValue(), idEmpleado, motivo, evidencia);
        return ResponseEntity.ok(j);
    }

    @PutMapping("/justificar/{id}/revisar")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> revisarJustificacion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        String decision = Objects.requireNonNull(body.get("decision"), "decision es requerido").toString();
        String comentario = Objects.toString(body.get("comentario"), "");
        JustificacionTardanzaModel j = justificacionService.revisar(id, decision, user.getIdUsuario(), comentario);
        return ResponseEntity.ok(j);
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<RegistroAsistenciaModel>> listarPorEmpleado(
            @PathVariable Integer idEmpleado) {
        return ResponseEntity.ok(asistenciaService.listarPorEmpleado(idEmpleado));
    }

    @GetMapping("/hoy/{idEmpleado}")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<RegistroAsistenciaModel> buscarHoy(
            @PathVariable Integer idEmpleado) {
        Optional<RegistroAsistenciaModel> registro = asistenciaService.buscarHoy(idEmpleado);
        return registro.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<MarcarAsistenciaResponseDTO> registrarManual(
            @RequestBody MarcarAsistenciaManualRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        MarcarAsistenciaResponseDTO response = asistenciaService.registrarManual(request, user.getIdUsuario(), ip);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/justificaciones/pendientes")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPendientes() {
        return ResponseEntity.ok(justificacionService.listarPendientes());
    }

    @GetMapping("/justificaciones/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPorEmpleado(
            @PathVariable Long idEmpleado) {
        return ResponseEntity.ok(justificacionService.listarPorEmpleado(idEmpleado));
    }
}
