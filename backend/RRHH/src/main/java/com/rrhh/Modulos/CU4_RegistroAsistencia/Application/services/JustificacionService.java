package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class JustificacionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public JustificacionTardanzaModel registrar(Integer idRegistroAsistencia, Long idEmpleado,
                                                 String motivo, String evidenciaUrl) {
        RegistroAsistenciaModel r = null;
        if (idRegistroAsistencia != null) {
            r = em.find(RegistroAsistenciaModel.class, idRegistroAsistencia);
            if (r == null) {
                throw new RuntimeException("Registro de asistencia no encontrado");
            }
            if (r.getEmpleado() == null || !idEmpleado.equals(r.getEmpleado().getIdEmpleado())) {
                throw new RuntimeException("El registro de asistencia no corresponde al empleado.");
            }
            String estado = r.getEstado() != null ? r.getEstado().toUpperCase() : "";
            boolean esJustificable = "TARDANZA".equals(estado)
                    || "INASISTENCIA".equals(estado)
                    || (r.getMinutosTardanza() != null && r.getMinutosTardanza() > 0);
            if (!esJustificable) {
                throw new RuntimeException("El registro seleccionado no tiene tardanza o inasistencia para justificar.");
            }
            Long existentes = em.createQuery(
                            "SELECT COUNT(j) FROM JustificacionTardanzaModel j WHERE j.idRegistroAsistencia = :idRegistro AND j.estado = 'PENDIENTE'",
                            Long.class)
                    .setParameter("idRegistro", idRegistroAsistencia)
                    .getSingleResult();
            if (existentes > 0) {
                throw new RuntimeException("Ya existe una justificación pendiente para este registro.");
            }
            LocalDate fechaRegistro = r.getFecha();
            if (LocalDate.now(ZoneId.of("America/Lima")).isAfter(fechaRegistro.plusDays(1))) {
                throw new RuntimeException("El plazo de justificacion vencio. Solo puedes justificar el mismo dia o el dia siguiente.");
            }
        }

        JustificacionTardanzaModel j = new JustificacionTardanzaModel();
        j.setIdRegistroAsistencia(idRegistroAsistencia);
        j.setIdEmpleado(idEmpleado);
        j.setMotivo(motivo);
        j.setEvidenciaUrl(evidenciaUrl);
        j.setEstado("PENDIENTE");
        j.setFechaJustificacion(LocalDateTime.now());

        if (r != null) {
            r.setObservacion("Justificacion pendiente de aprobacion");
            em.merge(r);
        }

        em.persist(j);
        return j;
    }

    @Transactional
    public JustificacionTardanzaModel revisar(Long idJustificacion, String decision,
                                                Long idRevisor, String comentario) {
        JustificacionTardanzaModel j = em.find(JustificacionTardanzaModel.class, idJustificacion);
        if (j == null) throw new RuntimeException("Justificacion no encontrada");

        if (!"PENDIENTE".equals(j.getEstado())) {
            throw new RuntimeException("La justificacion ya fue revisada");
        }

        j.setEstado(decision);
        j.setRevisadoPor(idRevisor);
        j.setFechaRevision(LocalDateTime.now());

        if (comentario != null && !comentario.isBlank()) {
            j.setComentarioRevision(comentario);
        }

        // Si se aprueba: anular la tardanza en el registro de asistencia
        // para que la nómina no descuente esos minutos
        if ("APROBADA".equals(decision) && j.getIdRegistroAsistencia() != null) {
            RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
            if (r != null) {
                r.setMinutosTardanza(0);
                r.setObservacion("Tardanza justificada y aprobada");
                em.merge(r);
            }
        }

        return em.merge(j);
    }

    public List<JustificacionTardanzaModel> listarPendientes() {
        List<JustificacionTardanzaModel> pendientes = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.estado = 'PENDIENTE'" +
                " OR (j.estado = 'APROBADA' AND (j.correccionRegistrada IS NULL OR j.correccionRegistrada = false))",
                JustificacionTardanzaModel.class).getResultList();
        pendientes.forEach(this::enriquecerConDatosAsistencia);
        return pendientes;
    }

    public List<JustificacionTardanzaModel> listarPorEmpleado(Long idEmpleado) {
        List<JustificacionTardanzaModel> lista = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.idEmpleado = :idEmpleado ORDER BY j.fechaJustificacion DESC",
                JustificacionTardanzaModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
        lista.forEach(this::enriquecerConDatosAsistencia);
        return lista;
    }

    /** Historial completo, cualquier estado (PENDIENTE/APROBADA/RECHAZADA), para
     * vistas de supervision de solo lectura (ej. Gerencia). */
    public List<JustificacionTardanzaModel> listarTodas() {
        List<JustificacionTardanzaModel> lista = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j ORDER BY j.fechaJustificacion DESC",
                JustificacionTardanzaModel.class).getResultList();
        lista.forEach(this::enriquecerConDatosAsistencia);
        return lista;
    }

    private void enriquecerConDatosAsistencia(JustificacionTardanzaModel j) {
        if (j.getIdEmpleado() != null) {
            EmpleadoModel empleado = em.find(EmpleadoModel.class, j.getIdEmpleado());
            if (empleado != null) {
                j.setNombreEmpleado(empleado.getNombres() + " " + empleado.getApellidos());
            }
        }

        if (j.getIdRegistroAsistencia() == null) return;

        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
        if (r == null) return;

        if (r.getHoraEntrada() != null) {
            j.setHoraEntrada(r.getHoraEntrada().toString().substring(0, 5));
        }
        if (r.getFecha() != null) {
            j.setFechaAsistencia(r.getFecha().toString());
        }
        j.setMinutosTardanza(r.getMinutosTardanza());
        String estadoRegistro = r.getEstado() != null ? r.getEstado().toUpperCase() : "";
        j.setTipoIncidencia("INASISTENCIA".equals(estadoRegistro) ? "INASISTENCIA" : "TARDANZA");

        // Determinar hora de entrada esperada con tres niveles de prioridad:
        // 1° el horario directamente vinculado al registro (el más preciso: congelado al momento del registro)
        // 2° la asignación de horario vigente en esa fecha (incluye históricas desactivadas)
        // 3° la asignación más reciente del empleado (para registros anteriores a cualquier asignación formal)
        if (r.getHorario() != null && r.getHorario().getHoraEntrada() != null) {
            j.setHoraEntradaEsperada(r.getHorario().getHoraEntrada().toString().substring(0, 5));
        } else if (j.getIdEmpleado() != null && r.getFecha() != null) {
            List<AsignacionHorarioModel> asignaciones = em.createQuery(
                    "SELECT a FROM AsignacionHorarioModel a " +
                    "WHERE a.empleado.idEmpleado = :idEmpleado " +
                    "AND a.fechaDesde <= :fecha " +
                    "AND (a.fechaHasta IS NULL OR a.fechaHasta >= :fecha) " +
                    "ORDER BY a.fechaDesde DESC",
                    AsignacionHorarioModel.class)
                    .setParameter("idEmpleado", j.getIdEmpleado())
                    .setParameter("fecha", r.getFecha())
                    .setMaxResults(1)
                    .getResultList();

            if (asignaciones.isEmpty()) {
                asignaciones = em.createQuery(
                        "SELECT a FROM AsignacionHorarioModel a " +
                        "WHERE a.empleado.idEmpleado = :idEmpleado " +
                        "ORDER BY a.creadoEl DESC",
                        AsignacionHorarioModel.class)
                        .setParameter("idEmpleado", j.getIdEmpleado())
                        .setMaxResults(1)
                        .getResultList();
            }

            if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
                HorarioModel horario = asignaciones.get(0).getHorario();
                if (horario.getHoraEntrada() != null) {
                    j.setHoraEntradaEsperada(horario.getHoraEntrada().toString().substring(0, 5));
                }
            }
        }
    }
}
