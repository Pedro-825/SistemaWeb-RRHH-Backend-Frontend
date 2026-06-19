package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    private final JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final IHistorialRepository historialRepository;

    @PersistenceContext
    private EntityManager em;

    public AsistenciaService(JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository,
                             JpaEmpleadoRepository empleadoRepository,
                             IHistorialRepository historialRepository) {
        this.registroAsistenciaRepository = registroAsistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.historialRepository = historialRepository;
    }

    public MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request, String ip) {

        if (request.getIdEmpleado() == null) {
            registrarLogFallo(null, "ID_EMPLEADO_NULO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            registrarLogFallo(request.getIdEmpleado().longValue(), "EMPLEADO_NO_ENCONTRADO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            registrarLogFallo(empleado.getIdEmpleado(), "EMPLEADO_INACTIVO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. El empleado esta inactivo.");
        }

        String huellaTemplate = request.getHuellaTemplate();
        if (huellaTemplate == null || huellaTemplate.isBlank()
                || empleado.getHuellaTemplate() == null
                || !huellaTemplate.equals(empleado.getHuellaTemplate())) {
            registrarLogFallo(empleado.getIdEmpleado(), "HUELLA_NO_COINCIDE", ip);
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. La huella no coincide.");
        }

        LocalDate fechaActual = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                        request.getIdEmpleado(), fechaActual, "ORIGINAL");

        if (registroOptional.isEmpty()) {
            return registrarEntrada(empleado, fechaActual, horaActual);
        }

        RegistroAsistenciaModel registro = registroOptional.get();

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() == null) {
            return registrarSalida(registro, horaActual);
        }

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() != null) {
            registrarLogFallo(empleado.getIdEmpleado(), "REGISTRO_DUPLICADO", ip);
            return new MarcarAsistenciaResponseDTO(
                    false,
                    "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy."
            );
        }

        registrarLogFallo(empleado.getIdEmpleado(), "REGISTRO_INCONSISTENTE", ip);
        return new MarcarAsistenciaResponseDTO(false, "Registro inconsistente. Revise la asistencia manualmente.");
    }

    public MarcarAsistenciaResponseDTO registrarManual(MarcarAsistenciaManualRequestDTO request,
                                                         Long idUsuarioRrhh, String ip) {

        if (request.getIdEmpleado() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            return new MarcarAsistenciaResponseDTO(false, "El empleado no esta activo.");
        }

        if (request.getFechaHora() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar la fecha y hora del registro.");
        }

        LocalDate fecha = request.getFechaHora().toLocalDate();
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy.minusDays(1)) || fecha.isAfter(hoy.plusDays(1))) {
            return new MarcarAsistenciaResponseDTO(false,
                    "El registro manual solo permite un desfase maximo de 1 dia.");
        }

        boolean esEntrada = "ENTRADA".equalsIgnoreCase(request.getTipo());
        boolean esCorreccion = "CORRECCION".equalsIgnoreCase(request.getTipoRegistro());

        if (esCorreccion) {
            Optional<RegistroAsistenciaModel> existente = registroAsistenciaRepository
                    .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                            request.getIdEmpleado(), fecha, "CORRECCION");
            RegistroAsistenciaModel registro = existente.orElseGet(() -> {
                RegistroAsistenciaModel r = new RegistroAsistenciaModel();
                r.setEmpleado(empleado);
                r.setFecha(fecha);
                r.setTipoRegistro("CORRECCION");
                return r;
            });
            if (esEntrada) {
                registro.setHoraEntrada(request.getFechaHora().toLocalTime());
                registro.setTipoUltimoRegistro("ENTRADA");
                registro.setMinutosTardanza(0);
                registro.setEstado("JUSTIFICADA");
            } else {
                registro.setHoraSalida(request.getFechaHora().toLocalTime());
                registro.setTipoUltimoRegistro("SALIDA");
                if (registro.getHoraEntrada() != null) {
                    long mins = Duration.between(registro.getHoraEntrada(), registro.getHoraSalida()).toMinutes();
                    registro.setHorasTrabajadas(BigDecimal.valueOf(mins)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                }
                registro.setEstado("JUSTIFICADA");
            }
            registro.setObservacion(request.getMotivo() != null ? request.getMotivo() : "Correccion de tardanza justificada");
            RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

            Historial log = new Historial(idUsuarioRrhh, "REGISTRO_MANUAL_CORRECCION", ip);
            log.setTablaAfectada("REGISTRO_ASISTENCIA");
            log.setValorNuevo("CORRECCION | Empleado: " + empleado.getIdEmpleado()
                    + " | Tipo: " + (esEntrada ? "ENTRADA" : "SALIDA")
                    + " | Fecha: " + fecha);
            historialRepository.save(log);

            return new MarcarAsistenciaResponseDTO(
                    true, "Corrección registrada correctamente.",
                    esEntrada ? "ENTRADA" : "SALIDA",
                    guardado.getFecha(), guardado.getHoraEntrada(), guardado.getHoraSalida(),
                    guardado.getHorasTrabajadas(), guardado.getMinutosTardanza(),
                    guardado.getMinutosExtra(), guardado.getEstado()
            );
        }

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                        request.getIdEmpleado(), fecha, "ORIGINAL");

        RegistroAsistenciaModel registro;
        if (registroOptional.isEmpty()) {
            registro = new RegistroAsistenciaModel();
            registro.setEmpleado(empleado);
            registro.setFecha(fecha);
        } else {
            registro = registroOptional.get();
            if (registro.getHoraEntrada() != null && registro.getHoraSalida() != null) {
                return new MarcarAsistenciaResponseDTO(false,
                        "El empleado ya tiene entrada y salida registradas para este dia.");
            }
        }

        String motivo = request.getMotivo() != null ? request.getMotivo() : "Ajuste manual por RRHH";

        if (esEntrada) {
            registro.setHoraEntrada(request.getFechaHora().toLocalTime());
            registro.setTipoUltimoRegistro("ENTRADA");

            LocalTime horaLimite = obtenerHoraEntradaProgramada(empleado).plusMinutes(obtenerTolerancia(empleado));
            LocalTime horaEntradaReal = registro.getHoraEntrada();

            if (horaEntradaReal.isAfter(horaLimite)) {
                int minutosTardanza = (int) Duration.between(
                        obtenerHoraEntradaProgramada(empleado), horaEntradaReal).toMinutes();
                registro.setMinutosTardanza(minutosTardanza);
                registro.setEstado("TARDANZA");
                registro.setObservacion("Registro manual - Entrada con tardanza. Motivo: " + motivo);
            } else {
                registro.setMinutosTardanza(0);
                registro.setEstado("PUNTUAL");
                registro.setObservacion("Registro manual - Entrada puntual. Motivo: " + motivo);
            }
        } else {
            registro.setHoraSalida(request.getFechaHora().toLocalTime());
            registro.setTipoUltimoRegistro("SALIDA");

            if (registro.getHoraEntrada() != null) {
                BigDecimal horas = calcularHorasTrabajadas(registro.getHoraEntrada(), registro.getHoraSalida());
                int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), registro.getHoraSalida(),
                        obtenerJornadaLaboralMinutos(empleado));
                registro.setHorasTrabajadas(horas);
                registro.setMinutosExtra(minutosExtra);
            }

            registro.setObservacion("Registro manual - Salida. Motivo: " + motivo);
        }

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        Historial log = new Historial(idUsuarioRrhh, "REGISTRO_MANUAL_ASISTENCIA", ip);
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo("Empleado: " + empleado.getIdEmpleado()
                + " | Tipo: " + (esEntrada ? "ENTRADA" : "SALIDA")
                + " | Motivo: " + motivo
                + " | Fecha: " + fecha);
        historialRepository.save(log);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Registro manual de asistencia almacenado correctamente.",
                esEntrada ? "ENTRADA" : "SALIDA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    public List<RegistroAsistenciaModel> listarPorEmpleado(Integer idEmpleado) {
        List<RegistroAsistenciaModel> todos = registroAsistenciaRepository.findByEmpleado_IdEmpleadoOrderByFechaDesc(idEmpleado);
        return todos.stream().filter(r -> "ORIGINAL".equals(r.getTipoRegistro()) || r.getTipoRegistro() == null).toList();
    }

    public Optional<RegistroAsistenciaModel> buscarHoy(Integer idEmpleado) {
        Optional<RegistroAsistenciaModel> corr = registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, LocalDate.now(), "CORRECCION");
        if (corr.isPresent()) return corr;
        return registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, LocalDate.now(), "ORIGINAL");
    }

    private void registrarLogFallo(Long idEmpleado, String motivo, String ip) {
        Historial log = new Historial(idEmpleado != null ? idEmpleado : 0L, "INTENTO_FALLIDO_ASISTENCIA", ip);
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo(motivo);
        historialRepository.save(log);
    }

    private HorarioModel obtenerHorarioAsignado(EmpleadoModel empleado) {
        List<AsignacionHorarioModel> asignaciones = em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true",
                AsignacionHorarioModel.class)
                .setParameter("idEmpleado", empleado.getIdEmpleado())
                .getResultList();

        if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
            return asignaciones.get(0).getHorario();
        }

        return null;
    }

    private LocalTime obtenerHoraEntradaProgramada(EmpleadoModel empleado) {
        HorarioModel horario = obtenerHorarioAsignado(empleado);
        if (horario != null && horario.getHoraEntrada() != null) {
            return horario.getHoraEntrada();
        }
        return LocalTime.of(8, 0);
    }

    private int obtenerTolerancia(EmpleadoModel empleado) {
        HorarioModel horario = obtenerHorarioAsignado(empleado);
        if (horario != null && horario.getTolerancia() != null) {
            return horario.getTolerancia();
        }
        return 10;
    }

    private int obtenerJornadaLaboralMinutos(EmpleadoModel empleado) {
        return 8 * 60;
    }

    private MarcarAsistenciaResponseDTO registrarEntrada(EmpleadoModel empleado, LocalDate fechaActual, LocalTime horaActual) {

        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(fechaActual);
        registro.setHoraEntrada(horaActual);
        registro.setTipoUltimoRegistro("ENTRADA");

        LocalTime horaEntradaProgramada = obtenerHoraEntradaProgramada(empleado);
        int tolerancia = obtenerTolerancia(empleado);
        LocalTime horaLimite = horaEntradaProgramada.plusMinutes(tolerancia);
        int minutosTardanza = 0;

        if (horaActual.isAfter(horaLimite)) {
            minutosTardanza = (int) Duration.between(horaEntradaProgramada, horaActual).toMinutes();
        }

        if (minutosTardanza > 0) {
            registro.setMinutosTardanza(minutosTardanza);
            registro.setEstado("TARDANZA");
            registro.setObservacion("Entrada registrada con tardanza. Pendiente de justificacion.");
        } else {
            registro.setMinutosTardanza(0);
            registro.setEstado("PUNTUAL");
            registro.setObservacion("Entrada registrada correctamente.");
        }

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Entrada registrada correctamente.",
                "ENTRADA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    private MarcarAsistenciaResponseDTO registrarSalida(RegistroAsistenciaModel registro, LocalTime horaSalida) {

        registro.setHoraSalida(horaSalida);
        registro.setTipoUltimoRegistro("SALIDA");

        BigDecimal horasTrabajadas = calcularHorasTrabajadas(registro.getHoraEntrada(), horaSalida);
        int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), horaSalida, 8 * 60);

        registro.setHorasTrabajadas(horasTrabajadas);
        registro.setMinutosExtra(minutosExtra);
        registro.setObservacion("Salida registrada correctamente.");
        registro.setActualizadoEl(LocalDateTime.now());

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Salida registrada correctamente. El registro de asistencia ha sido almacenado correctamente en el sistema.",
                "SALIDA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    private BigDecimal calcularHorasTrabajadas(LocalTime horaEntrada, LocalTime horaSalida) {
        long minutos = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutos < 0) {
            minutos = 0;
        }

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int calcularMinutosExtra(LocalTime horaEntrada, LocalTime horaSalida, int jornadaMinutos) {
        long minutosTrabajados = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutosTrabajados > jornadaMinutos) {
            return (int) (minutosTrabajados - jornadaMinutos);
        }

        return 0;
    }
}
