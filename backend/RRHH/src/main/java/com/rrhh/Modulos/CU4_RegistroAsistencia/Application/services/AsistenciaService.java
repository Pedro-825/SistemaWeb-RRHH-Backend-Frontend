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
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AsistenciaService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private final JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final IHistorialRepository historialRepository;
    private final PlanRecuperacionService planRecuperacionService;

    @PersistenceContext
    private EntityManager em;

    public AsistenciaService(JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository,
                             JpaEmpleadoRepository empleadoRepository,
                             IHistorialRepository historialRepository,
                             PlanRecuperacionService planRecuperacionService) {
        this.registroAsistenciaRepository = registroAsistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.historialRepository = historialRepository;
        this.planRecuperacionService = planRecuperacionService;
    }

    public MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request, String ip) {
        return marcarAsistencia(request, ip, true);
    }

    public MarcarAsistenciaResponseDTO marcarAsistenciaMovil(MarcarAsistenciaRequestDTO request, String ip) {
        return marcarAsistencia(request, ip, false);
    }

    private MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request, String ip,
                                                         boolean validarHuellaTemplate) {

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

        if (validarHuellaTemplate) {
            String huellaTemplate = request.getHuellaTemplate();
            if (huellaTemplate == null || huellaTemplate.isBlank()
                    || empleado.getHuellaTemplate() == null
                    || !huellaTemplate.equals(empleado.getHuellaTemplate())) {
                registrarLogFallo(empleado.getIdEmpleado(), "HUELLA_NO_COINCIDE", ip);
                return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. La huella no coincide.");
            }
        }

        ZonedDateTime ahora = ZonedDateTime.now(ZONA_PERU);
        LocalDate fechaActual = ahora.toLocalDate();
        LocalTime horaActual = ahora.toLocalTime();

        // Turno nocturno abierto desde ayer (entrada marcada, sin salida aun): esta
        // marcacion se interpreta como su salida, sin importar que ya sea "otro dia".
        Optional<RegistroAsistenciaModel> registroAbiertoAyer =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                                request.getIdEmpleado(), fechaActual.minusDays(1), "ORIGINAL")
                        .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() == null);

        if (registroAbiertoAyer.isPresent()) {
            return registrarSalida(registroAbiertoAyer.get(), horaActual, empleado);
        }

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                        request.getIdEmpleado(), fechaActual, "ORIGINAL");

        RegistroAsistenciaModel registro;
        if (registroOptional.isEmpty()) {
            registro = new RegistroAsistenciaModel();
            registro.setEmpleado(empleado);
            registro.setFecha(fechaActual);
            registro.setTipoRegistro("ORIGINAL");
        } else {
            registro = registroOptional.get();
        }

        // El dia ya quedo marcado como inasistencia (paso la hora de salida
        // programada sin marcar entrada): no se permite auto-marcar, debe
        // justificarse desde la web. El bloqueo del APK es solo de UI, esta es
        // la validacion real.
        if ("INASISTENCIA".equals(registro.getEstado())) {
            registrarLogFallo(empleado.getIdEmpleado(), "MARCADO_BLOQUEADO_INASISTENCIA", ip);
            return new MarcarAsistenciaResponseDTO(false,
                    "Tu asistencia de hoy quedo marcada como inasistencia. Ingresa a la plataforma web para justificarla.");
        }

        if (registro.getHoraEntrada() == null) {
            return registrarEntrada(registro, empleado, horaActual);
        }

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() == null) {
            return registrarSalida(registro, horaActual, empleado);
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

    @Transactional
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
        LocalDate hoy = hoyPeru();
        boolean esEntrada = "ENTRADA".equalsIgnoreCase(request.getTipo());
        boolean esCorreccion = "CORRECCION".equalsIgnoreCase(request.getTipoRegistro());

        // Las correcciones históricas (RRHH) no tienen restricción de fecha.
        // Solo los registros normales se limitan a ±1 día.
        if (!esCorreccion && (fecha.isBefore(hoy.minusDays(1)) || fecha.isAfter(hoy.plusDays(1)))) {
            return new MarcarAsistenciaResponseDTO(false,
                    "El registro manual solo permite un desfase maximo de 1 dia.");
        }

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

            if (request.getIdJustificacion() != null) {
                JustificacionTardanzaModel just = em.find(JustificacionTardanzaModel.class, request.getIdJustificacion());
                if (just != null) {
                    just.setCorreccionRegistrada(true);
                    em.merge(just);

                    if (just.getIdRegistroAsistencia() != null) {
                        RegistroAsistenciaModel registroOriginal =
                                em.find(RegistroAsistenciaModel.class, just.getIdRegistroAsistencia());
                        if (registroOriginal != null) {
                            HorarioModel horarioVigente = resolverHorarioParaCalculo(registroOriginal, empleado);
                            planRecuperacionService.generarPlan(just, registroOriginal, horarioVigente);
                        }
                    }
                }
            }

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

            HorarioModel horarioVigente = obtenerHorarioAsignado(empleado);
            registro.setHorario(horarioVigente);

            LocalTime horaLimite = obtenerHoraEntradaProgramada(horarioVigente).plusMinutes(obtenerTolerancia(horarioVigente));
            LocalTime horaEntradaReal = registro.getHoraEntrada();

            if (horaEntradaReal.isAfter(horaLimite)) {
                int minutosTardanza = (int) Duration.between(
                        obtenerHoraEntradaProgramada(horarioVigente), horaEntradaReal).toMinutes();
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
                        obtenerJornadaLaboralMinutos(resolverHorarioParaCalculo(registro, empleado)));
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

    /** Justificaciones aprobadas que ya tienen su corrección manual guardada,
     * con el detalle de antes/después (registro ORIGINAL vs CORRECCION). */
    public List<Map<String, Object>> listarCorreccionesRegistradas() {
        List<JustificacionTardanzaModel> justificaciones = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j " +
                "WHERE j.estado = 'APROBADA' AND j.correccionRegistrada = true " +
                "ORDER BY j.fechaRevision DESC",
                JustificacionTardanzaModel.class)
                .getResultList();

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (JustificacionTardanzaModel j : justificaciones) {
            if (j.getIdRegistroAsistencia() == null) continue;

            RegistroAsistenciaModel original = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
            if (original == null) continue;

            Optional<RegistroAsistenciaModel> correccion = registroAsistenciaRepository
                    .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                            j.getIdEmpleado().intValue(), original.getFecha(), "CORRECCION");

            Optional<EmpleadoModel> empleado = empleadoRepository.findById(j.getIdEmpleado());

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idJustificacion", j.getIdJustificacion());
            m.put("idEmpleado", j.getIdEmpleado());
            m.put("nombreEmpleado", empleado.map(e -> e.getNombres() + " " + e.getApellidos()).orElse("Empleado #" + j.getIdEmpleado()));
            m.put("tipo", "INASISTENCIA".equals(original.getEstado()) ? "INASISTENCIA" : "TARDANZA");
            m.put("fecha", original.getFecha());
            m.put("horaEntradaOriginal", original.getHoraEntrada());
            m.put("horaSalidaOriginal", original.getHoraSalida());
            m.put("horaEntradaCorregida", correccion.map(RegistroAsistenciaModel::getHoraEntrada).orElse(null));
            m.put("horaSalidaCorregida", correccion.map(RegistroAsistenciaModel::getHoraSalida).orElse(null));
            m.put("motivo", j.getMotivo());
            m.put("fechaRevision", j.getFechaRevision());
            resultado.add(m);
        }

        return resultado;
    }

    public Optional<HorarioModel> getHorarioEmpleado(Integer idEmpleado) {
        Optional<EmpleadoModel> empleadoOpt = empleadoRepository.findById(idEmpleado.longValue());
        if (empleadoOpt.isEmpty()) return Optional.empty();
        return Optional.ofNullable(obtenerHorarioAsignado(empleadoOpt.get()));
    }

    public Optional<RegistroAsistenciaModel> buscarHoy(Integer idEmpleado) {
        Optional<RegistroAsistenciaModel> corr = registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, hoyPeru(), "CORRECCION");
        if (corr.isPresent()) return corr;
        return registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, hoyPeru(), "ORIGINAL");
    }

    /** Estado a mostrar en el APK: igual que buscarHoy(), pero si hay un turno
     * nocturno de ayer aun abierto (entrada marcada, sin salida), se reporta ese
     * registro en vez de "nada" -- para que el boton de Entrada no aparezca
     * habilitado cuando en realidad marcarAsistencia() lo va a tomar como Salida. */
    public Optional<RegistroAsistenciaModel> buscarEstadoParaMarcar(Integer idEmpleado) {
        Optional<RegistroAsistenciaModel> abiertoAyer = registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, hoyPeru().minusDays(1), "ORIGINAL")
                .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() == null);
        if (abiertoAyer.isPresent()) return abiertoAyer;
        return buscarHoy(idEmpleado);
    }

    private LocalDate hoyPeru() {
        return ZonedDateTime.now(ZONA_PERU).toLocalDate();
    }

    private LocalDateTime ahoraPeru() {
        return ZonedDateTime.now(ZONA_PERU).toLocalDateTime();
    }

    private void registrarLogFallo(Long idEmpleado, String motivo, String ip) {
        Historial log = new Historial(idEmpleado != null ? idEmpleado : 0L, "INTENTO_FALLIDO_ASISTENCIA", ip);
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo(motivo);
        historialRepository.save(log);
    }

    private HorarioModel obtenerHorarioAsignado(EmpleadoModel empleado) {
        List<AsignacionHorarioModel> asignaciones = em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true " +
                "ORDER BY a.fechaDesde DESC, a.idAsignacion DESC",
                AsignacionHorarioModel.class)
                .setParameter("idEmpleado", empleado.getIdEmpleado())
                .setMaxResults(1)
                .getResultList();

        if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
            return asignaciones.get(0).getHorario();
        }

        return null;
    }

    private LocalTime obtenerHoraEntradaProgramada(HorarioModel horario) {
        if (horario != null && horario.getHoraEntrada() != null) {
            return horario.getHoraEntrada();
        }
        return LocalTime.of(8, 0);
    }

    private int obtenerTolerancia(HorarioModel horario) {
        if (horario != null && horario.getTolerancia() != null) {
            return horario.getTolerancia();
        }
        return 10;
    }

    private int obtenerJornadaLaboralMinutos(HorarioModel horario) {
        if (horario != null && horario.getHoraEntrada() != null && horario.getHoraSalida() != null) {
            return (int) Duration.between(horario.getHoraEntrada(), horario.getHoraSalida()).toMinutes();
        }
        return 9 * 60; // fallback: 08:00–17:00
    }

    /** Horario a usar para un registro ya existente: el congelado al marcar entrada,
     * o el vigente ahora si el registro no tiene uno guardado (correcciones antiguas). */
    private HorarioModel resolverHorarioParaCalculo(RegistroAsistenciaModel registro, EmpleadoModel empleado) {
        return registro.getHorario() != null ? registro.getHorario() : obtenerHorarioAsignado(empleado);
    }

    private MarcarAsistenciaResponseDTO registrarEntrada(RegistroAsistenciaModel registro, EmpleadoModel empleado,
                                                         LocalTime horaActual) {
        registro.setHoraEntrada(horaActual);
        registro.setTipoUltimoRegistro("ENTRADA");
        registro.setTipoRegistro("ORIGINAL");
        registro.setActualizadoEl(ahoraPeru());

        HorarioModel horarioVigente = obtenerHorarioAsignado(empleado);
        registro.setHorario(horarioVigente);

        LocalTime horaEntradaProgramada = obtenerHoraEntradaProgramada(horarioVigente);
        int tolerancia = obtenerTolerancia(horarioVigente);
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

    private MarcarAsistenciaResponseDTO registrarSalida(RegistroAsistenciaModel registro, LocalTime horaSalida, EmpleadoModel empleado) {

        registro.setHoraSalida(horaSalida);
        registro.setTipoUltimoRegistro("SALIDA");

        BigDecimal horasTrabajadas = calcularHorasTrabajadas(registro.getHoraEntrada(), horaSalida);
        int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), horaSalida,
                obtenerJornadaLaboralMinutos(resolverHorarioParaCalculo(registro, empleado)));

        registro.setHorasTrabajadas(horasTrabajadas);
        registro.setMinutosExtra(minutosExtra);
        registro.setObservacion("Salida registrada correctamente.");
        registro.setActualizadoEl(ahoraPeru());

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

        // Turno que cruza la medianoche (ej. entrada 22:00, salida 06:00 del dia siguiente):
        // la resta de horas puras da negativa, se ajusta sumando 24h.
        if (minutos < 0) {
            minutos += 24 * 60;
        }

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int calcularMinutosExtra(LocalTime horaEntrada, LocalTime horaSalida, int jornadaMinutos) {
        long minutosTrabajados = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutosTrabajados < 0) {
            minutosTrabajados += 24 * 60;
        }

        if (minutosTrabajados > jornadaMinutos) {
            return (int) (minutosTrabajados - jornadaMinutos);
        }

        return 0;
    }
}
