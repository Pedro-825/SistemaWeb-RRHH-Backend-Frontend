package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaPlanRecuperacionRepository;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.Shared.persistence.PlanRecuperacionModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula y guarda el plan de recuperacion de horas cuando RRHH registra la
 * correccion de una justificacion aprobada (tardanza o inasistencia).
 *
 * Tardanza: se recupera en un solo dia (el siguiente habil), sumando a la
 * salida todos los minutos de tardanza.
 *
 * Inasistencia: se recupera la jornada completa repartida en bloques de 2
 * horas por dia habil (domingo se salta), y el ultimo dia se ajusta: si las
 * horas enteras a recuperar son pares, el ultimo bloque tambien es de 2h (mas
 * los minutos sueltos); si son impares, el ultimo bloque es de 1h (mas los
 * minutos sueltos).
 */
@Service
public class PlanRecuperacionService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final int BLOQUE_MINUTOS = 120;

    private final JpaPlanRecuperacionRepository repository;

    public PlanRecuperacionService(JpaPlanRecuperacionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void generarPlan(JustificacionTardanzaModel justificacion, RegistroAsistenciaModel registroOriginal, HorarioModel horario) {
        if (registroOriginal == null || horario == null) return;
        if (repository.existsByIdJustificacion(justificacion.getIdJustificacion())) return;

        boolean esInasistencia = "INASISTENCIA".equals(registroOriginal.getEstado());
        LocalTime horaEntrada = horario.getHoraEntrada();
        LocalTime horaSalida = horario.getHoraSalida();

        long jornadaMinutos = Duration.between(horaEntrada, horaSalida).toMinutes();
        if (jornadaMinutos <= 0) jornadaMinutos += 24 * 60;

        int minutosARecuperar;
        if (esInasistencia) {
            minutosARecuperar = (int) jornadaMinutos;
        } else {
            if (registroOriginal.getHoraEntrada() == null) return;
            int entradaProgMin = horaEntrada.toSecondOfDay() / 60;
            int entradaRealMin = registroOriginal.getHoraEntrada().toSecondOfDay() / 60;
            minutosARecuperar = Math.max(0, entradaRealMin - entradaProgMin);
        }
        if (minutosARecuperar <= 0) return;

        List<PlanRecuperacionModel> dias = new ArrayList<>();
        LocalDate fechaCursor = siguienteDiaHabil(registroOriginal.getFecha().plusDays(1));

        if (!esInasistencia) {
            dias.add(construirDia(justificacion, registroOriginal, "TARDANZA",
                    fechaCursor, horaEntrada, horaSalida, minutosARecuperar));
        } else {
            int horasEnteras = minutosARecuperar / 60;
            int minutosSueltos = minutosARecuperar % 60;
            int cantidadDias = (horasEnteras % 2 == 0) ? horasEnteras / 2 : (horasEnteras + 1) / 2;
            int minutosUltimoDia = (horasEnteras % 2 == 0 ? 120 : 60) + minutosSueltos;

            for (int i = 1; i <= cantidadDias; i++) {
                int minutosDia = (i < cantidadDias) ? BLOQUE_MINUTOS : minutosUltimoDia;
                dias.add(construirDia(justificacion, registroOriginal, "INASISTENCIA",
                        fechaCursor, horaEntrada, horaSalida, minutosDia));
                fechaCursor = siguienteDiaHabil(fechaCursor.plusDays(1));
            }
        }

        repository.saveAll(dias);
    }

    private PlanRecuperacionModel construirDia(JustificacionTardanzaModel justificacion, RegistroAsistenciaModel registroOriginal,
                                                String tipo, LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, int minutosRecuperacion) {
        PlanRecuperacionModel dia = new PlanRecuperacionModel();
        dia.setIdJustificacion(justificacion.getIdJustificacion());
        dia.setIdEmpleado(registroOriginal.getEmpleado().getIdEmpleado());
        dia.setTipo(tipo);
        dia.setFecha(fecha);
        dia.setHoraEntrada(horaEntrada);
        dia.setHoraSalidaOriginal(horaSalida);
        dia.setMinutosRecuperacion(minutosRecuperacion);
        dia.setHoraSalidaModificada(horaSalida.plusMinutes(minutosRecuperacion));
        return dia;
    }

    private LocalDate siguienteDiaHabil(LocalDate fecha) {
        while (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fecha = fecha.plusDays(1);
        }
        return fecha;
    }

    public List<Map<String, Object>> obtenerPlan(Integer idJustificacion) {
        LocalDateTime ahora = LocalDateTime.now(ZONA_PERU);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (PlanRecuperacionModel dia : repository.findByIdJustificacionOrderByFechaAsc(idJustificacion)) {
            LocalDateTime finModificado = LocalDateTime.of(dia.getFecha(), dia.getHoraSalidaModificada());
            LocalDateTime inicioJornada = LocalDateTime.of(dia.getFecha(), dia.getHoraEntrada());

            boolean completado = ahora.isAfter(finModificado);
            long minutosTotales = Duration.between(dia.getHoraEntrada(), dia.getHoraSalidaModificada()).toMinutes();
            long minutosRestantes;
            if (ahora.isBefore(inicioJornada)) {
                minutosRestantes = minutosTotales;
            } else if (completado) {
                minutosRestantes = 0;
            } else {
                minutosRestantes = Duration.between(ahora, finModificado).toMinutes();
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fecha", dia.getFecha());
            m.put("horaEntrada", dia.getHoraEntrada());
            m.put("horaSalidaOriginal", dia.getHoraSalidaOriginal());
            m.put("minutosRecuperacion", dia.getMinutosRecuperacion());
            m.put("horaSalidaModificada", dia.getHoraSalidaModificada());
            m.put("horasTotalesDia", minutosTotales);
            m.put("minutosRestantes", minutosRestantes);
            m.put("completado", completado);
            resultado.add(m);
        }
        return resultado;
    }
}
