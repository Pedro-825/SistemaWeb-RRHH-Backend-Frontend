package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.OtpService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Shared.persistence.DispositivoEmpleadoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final JpaDispositivoEmpleadoRepository dispositivoRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final JpaUsuarioRepository usuarioRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public DispositivoController(JpaDispositivoEmpleadoRepository dispositivoRepository,
                                 JpaEmpleadoRepository empleadoRepository,
                                 JpaUsuarioRepository usuarioRepository,
                                 OtpService otpService,
                                 EmailService emailService) {
        this.dispositivoRepository = dispositivoRepository;
        this.empleadoRepository    = empleadoRepository;
        this.usuarioRepository     = usuarioRepository;
        this.otpService            = otpService;
        this.emailService          = emailService;
    }

    /** Envía un código OTP al correo del empleado para verificar su identidad */
    @PostMapping("/enviar-otp")
    public ResponseEntity<Map<String, Object>> enviarOtp(@RequestBody Map<String, String> body) {
        String dni = body.get("numeroDi");
        if (dni == null || dni.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "DNI requerido."));
        }

        EmpleadoModel emp = empleadoRepository.findByNumeroDi(dni)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con DNI: " + dni));

        if (!"ACTIVO".equals(emp.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Empleado inactivo o desvinculado."));
        }

        if (emp.getCorreo() == null || emp.getCorreo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El empleado no tiene correo registrado."));
        }

        OtpService.OtpResult otp = otpService.generarOtp(dni);
        emailService.enviarCodigoOtp(emp.getCorreo(), emp.getNombres() + " " + emp.getApellidos(), otp.codigo());

        return ResponseEntity.ok(Map.of("sessionId", otp.sessionId()));
    }

    /** Vincula o actualiza el celular de un empleado, previa verificación OTP */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> body) {
        Object tokenObj = body.get("deviceToken");
        if (tokenObj == null || tokenObj.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "deviceToken es requerido."));
        }
        String token = tokenObj.toString();
        String sessionId = body.getOrDefault("sessionId", "").toString();
        String codigo = body.getOrDefault("codigo", "").toString();

        if (sessionId.isBlank() || codigo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "sessionId y codigo son requeridos."));
        }

        OtpService.OtpVerificacion verificacion = otpService.verificarOtp(sessionId, codigo);
        switch (verificacion.resultado()) {
            case INVALIDO:
                return ResponseEntity.status(401).body(Map.of("mensaje", "Código OTP inválido."));
            case EXPIRADO_O_INEXISTENTE:
                return ResponseEntity.status(401).body(Map.of("mensaje", "Código OTP expirado o inválido. Solicita uno nuevo."));
            case SIN_INTENTOS:
                return ResponseEntity.status(429).body(Map.of("mensaje", "Demasiados intentos fallidos. Solicita un nuevo código."));
            case OK:
                break;
        }

        EmpleadoModel emp;
        if (body.containsKey("numeroDi")) {
            String dni = body.get("numeroDi").toString();
            emp = empleadoRepository.findByNumeroDi(dni)
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con DNI: " + dni));
        } else {
            Long idEmpleado = Long.valueOf(body.get("idEmpleado").toString());
            emp = empleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado))
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        }

        // El OTP se genera y envia para un DNI especifico (enviar-otp); el empleado que
        // se termina vinculando aqui debe ser exactamente ese mismo, para que nadie use
        // su propio OTP valido para vincular su telefono a la cuenta de otro empleado.
        if (!emp.getNumeroDi().equals(verificacion.dni())) {
            return ResponseEntity.status(403).body(Map.of("mensaje", "El código OTP no corresponde a este empleado."));
        }

        if (!"ACTIVO".equals(emp.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Empleado inactivo o desvinculado."));
        }

        DispositivoEmpleadoModel dispositivo = dispositivoRepository
                .findByEmpleadoIdEmpleado(emp.getIdEmpleado())
                .orElse(new DispositivoEmpleadoModel());

        dispositivo.setEmpleado(emp);
        dispositivo.setDeviceToken(token);
        dispositivoRepository.save(dispositivo);

        // Marca la app como instalada para este empleado: el celular no tiene sesion web
        // (isAuthenticated()) durante este flujo por OTP, asi que no puede llamar al
        // endpoint /confirmar-app; la verificacion OTP ya cumplida es suficiente prueba.
        usuarioRepository.findByEmpleadoIdEmpleado(emp.getIdEmpleado()).ifPresent(usuario -> {
            usuario.setAppMovilInstalada(true);
            usuarioRepository.save(usuario);
        });

        return ResponseEntity.ok(Map.of(
                "mensaje",    "Dispositivo registrado correctamente.",
                "idEmpleado", emp.getIdEmpleado(),
                "nombres",    emp.getNombres(),
                "apellidos",  emp.getApellidos()
        ));
    }

    /** Busca empleado por DNI — usado por la app móvil al registrarse */
    @GetMapping("/dni/{numeroDi}")
    public ResponseEntity<Map<String, Object>> getEmpleadoPorDni(@PathVariable String numeroDi) {
        Optional<EmpleadoModel> opt = empleadoRepository.findByNumeroDi(numeroDi);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmpleadoModel e = opt.get();
        if (!"ACTIVO".equals(e.getEstado())) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(Map.of(
                "idEmpleado", e.getIdEmpleado(),
                "nombres",    e.getNombres(),
                "apellidos",  e.getApellidos()
        ));
    }

    /** La app consulta datos del empleado para mostrar su nombre en la pantalla */
    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<Map<String, Object>> getEmpleado(@PathVariable Long idEmpleado) {
        Optional<EmpleadoModel> opt = empleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado));
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmpleadoModel e = opt.get();
        return ResponseEntity.ok(Map.of(
                "idEmpleado", e.getIdEmpleado(),
                "nombres",    e.getNombres(),
                "apellidos",  e.getApellidos(),
                "estado",     e.getEstado() != null ? e.getEstado() : "ACTIVO"
        ));
    }
}
