package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.EvidenciaStorageService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaGaleriaFotoRepository;
import com.rrhh.Shared.persistence.GaleriaFotoModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.config.security.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/galeria")
public class GaleriaController {

    private final JpaGaleriaFotoRepository galeriaRepo;
    private final EvidenciaStorageService storageService;
    private final JpaUsuarioRepository usuarioRepository;
    private final JpaDispositivoEmpleadoRepository dispositivoRepository;

    public GaleriaController(JpaGaleriaFotoRepository galeriaRepo,
                              EvidenciaStorageService storageService,
                              JpaUsuarioRepository usuarioRepository,
                              JpaDispositivoEmpleadoRepository dispositivoRepository) {
        this.galeriaRepo           = galeriaRepo;
        this.storageService        = storageService;
        this.usuarioRepository     = usuarioRepository;
        this.dispositivoRepository = dispositivoRepository;
    }

    /** Endpoint para la app móvil: valida con deviceToken en lugar de JWT */
    @PostMapping(value = "/mobile/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GaleriaFotoModel> subirFotoMobile(
            @RequestParam MultipartFile file,
            @RequestParam Long idEmpleado,
            @RequestParam String deviceToken) throws IOException {

        boolean valido = dispositivoRepository.existsByEmpleadoIdEmpleadoAndDeviceToken(idEmpleado, deviceToken);
        if (!valido) return ResponseEntity.status(403).build();

        String url = storageService.guardar(file);

        GaleriaFotoModel foto = new GaleriaFotoModel();
        foto.setIdEmpleado(idEmpleado);
        foto.setUrl(url);
        foto.setNombreArchivo(file.getOriginalFilename());

        return ResponseEntity.ok(galeriaRepo.save(foto));
    }

    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENCIA')")
    public ResponseEntity<GaleriaFotoModel> subirFoto(
            @RequestParam MultipartFile file,
            @RequestParam Long idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) throws IOException {

        validarPropietario(user, idEmpleado);

        String url = storageService.guardar(file);

        GaleriaFotoModel foto = new GaleriaFotoModel();
        foto.setIdEmpleado(idEmpleado);
        foto.setUrl(url);
        foto.setNombreArchivo(file.getOriginalFilename());

        return ResponseEntity.ok(galeriaRepo.save(foto));
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENCIA', 'RRHH')")
    public ResponseEntity<List<GaleriaFotoModel>> listar(
            @PathVariable Long idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {

        validarPropietario(user, idEmpleado);
        return ResponseEntity.ok(galeriaRepo.findByIdEmpleadoOrderByFechaSubidaDesc(idEmpleado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENCIA')")
    public ResponseEntity<Map<String, String>> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) throws IOException {

        GaleriaFotoModel foto = galeriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Foto no encontrada"));

        validarPropietario(user, foto.getIdEmpleado());

        storageService.eliminar(foto.getUrl());
        galeriaRepo.delete(foto);

        return ResponseEntity.ok(Map.of("mensaje", "Foto eliminada correctamente"));
    }

    private void validarPropietario(AuthenticatedUser user, Long idEmpleado) {
        if (user == null) return;
        if ("RRHH".equals(user.getRol())) return;

        Long idUsuario = user.getIdUsuario();
        if (idUsuario == null) throw new AccessDeniedException("Usuario no autenticado");
        UsuarioModel usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));

        if (usuario.getEmpleado() == null
                || !idEmpleado.equals(usuario.getEmpleado().getIdEmpleado())) {
            throw new AccessDeniedException("No puede acceder a la galería de otro empleado.");
        }
    }
}
