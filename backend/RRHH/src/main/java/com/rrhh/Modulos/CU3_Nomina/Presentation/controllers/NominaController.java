package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.services.INominaService;
import com.rrhh.Modulos.CU3_Nomina.Application.services.NominaService;
import com.rrhh.Modulos.CU3_Nomina.Presentation.routes.NominaApiRoutes;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.persistence.NotificacionEnvioModel;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class NominaController {

    private final INominaService nominaService;
    private final NominaService nominaServicePdf;
    private final EmailService emailService;
    private final IUsuarioGestionRepository usuarioGestionRepository;
    private final JpaEmpleadoRepository jpaEmpleadoRepository;

    @PersistenceContext
    private EntityManager em;

    public NominaController(INominaService nominaService,
                            NominaService nominaServicePdf,
                            EmailService emailService,
                            IUsuarioGestionRepository usuarioGestionRepository,
                            JpaEmpleadoRepository jpaEmpleadoRepository) {
        this.nominaService = nominaService;
        this.nominaServicePdf = nominaServicePdf;
        this.emailService = emailService;
        this.usuarioGestionRepository = usuarioGestionRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
    }

    @PostMapping(NominaApiRoutes.CALCULAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> calcularNomina(
            @RequestBody CalcularNominaRequestDTO request,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            List<NominaResponseDTO> resultado = nominaService.calcularNomina(request, idUsuario);
            return ResponseEntity.ok(resultado);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(NominaApiRoutes.BUSCAR_ID)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        try {
            NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
            if (hayRol(user, "EMPLEADO") && !esPropia(nomina.getIdEmpleado(), user.getIdUsuario())) {
                return ResponseEntity.status(403).body("No tiene permiso para ver esta nomina.");
            }
            return ResponseEntity.ok(nomina);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(NominaApiRoutes.POR_PERIODO)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorPeriodo(
            @PathVariable String periodo) {
        return ResponseEntity.ok(nominaService.buscarPorPeriodo(periodo));
    }

    @GetMapping(NominaApiRoutes.POR_EMPLEADO)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<?> buscarPorEmpleado(
            @PathVariable Long idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {
        if (hayRol(user, "EMPLEADO") && !esPropia(idEmpleado, user.getIdUsuario())) {
            return ResponseEntity.status(403).body("No tiene permiso para ver nominas de otros empleados.");
        }
        return ResponseEntity.ok(nominaService.buscarPorEmpleado(idEmpleado));
    }

    @GetMapping(NominaApiRoutes.POR_NOMBRE)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().body("El parametro 'nombre' es obligatorio.");
        }
        List<NominaResponseDTO> resultados = nominaService.buscarPorNombreEmpleado(nombre);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping(NominaApiRoutes.BUSCAR_ID + "/comprobante")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<?> descargarComprobante(
            @PathVariable Integer id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
        if (hayRol(user, "EMPLEADO") && !esPropia(nomina.getIdEmpleado(), user.getIdUsuario())) {
            return ResponseEntity.status(403).body("No tiene permiso para este comprobante.");
        }
        byte[] pdf = nominaServicePdf.generarComprobantePDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Comprobante_Nomina_" + nomina.getPeriodo() + ".pdf")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_PDF))
                .body(pdf);
    }

    @PutMapping(NominaApiRoutes.AJUSTAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> ajustarNomina(
            @PathVariable Integer id,
            @RequestBody AjustarNominaRequestDTO request,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            return ResponseEntity.ok(nominaService.ajustarNomina(id, request, idUsuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(NominaApiRoutes.BUSCAR_ID + "/enviar")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> enviarComprobanteEmail(@PathVariable Integer id) {
        try {
            NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
            byte[] pdf = nominaServicePdf.generarComprobantePDF(id);

            var empleadoOpt = jpaEmpleadoRepository.findById(Objects.requireNonNull(nomina.getIdEmpleado()));
            if (empleadoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Empleado no encontrado."));
            }

            var empleado = empleadoOpt.get();
            String correo = empleado.getCorreo() != null ? empleado.getCorreo() : "";

            UsuarioGestion usuario = usuarioGestionRepository.findByEmpleadoId(nomina.getIdEmpleado());
            if (usuario != null && usuario.getCorreoInst() != null && !usuario.getCorreoInst().isBlank()) {
                correo = usuario.getCorreoInst();
            }

            if (correo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message",
                        "El empleado no tiene correo registrado."));
            }

            String errorMsg = null;
            boolean exito = false;

            for (int intento = 1; intento <= 3; intento++) {
                try {
                    emailService.enviarComprobanteNomina(correo,
                            empleado.getNombres() + " " + empleado.getApellidos(),
                            nomina.getPeriodo(), pdf);

                    NotificacionEnvioModel notif = new NotificacionEnvioModel();
                    notif.setIdNomina(id);
                    notif.setTipo("COMPROBANTE_NOMINA");
                    notif.setEstado("ENVIADO");
                    notif.setIntentos(intento);
                    notif.setFechaEnvio(java.time.LocalDateTime.now());
                    em.persist(notif);

                    exito = true;
                    break;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    if (intento < 3) {
                        Thread.sleep(1000);
                    }
                }
            }

            if (exito) {
                return ResponseEntity.ok(Map.of("success", true, "message",
                        "Comprobante enviado a " + correo));
            }

            NotificacionEnvioModel notif = new NotificacionEnvioModel();
            notif.setIdNomina(id);
            notif.setTipo("COMPROBANTE_NOMINA");
            notif.setEstado("FALLIDO");
            notif.setIntentos(3);
            notif.setError(errorMsg);
            em.persist(notif);

            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error al enviar tras 3 intentos: " + errorMsg));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error al enviar: " + e.getMessage()));
        }
    }

    private boolean hayRol(AuthenticatedUser user, String rol) {
        return user != null && user.getRol() != null && user.getRol().equalsIgnoreCase(rol);
    }

    private boolean esPropia(Long idEmpleadoNomina, Long idUsuario) {
        if (idUsuario == null || idEmpleadoNomina == null) return false;
        UsuarioGestion usuario = usuarioGestionRepository.findByEmpleadoId(idEmpleadoNomina);
        return usuario != null && usuario.getIdUsuario() != null
                && usuario.getIdUsuario().equals(idUsuario);
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
