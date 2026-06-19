package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class JustificacionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public JustificacionTardanzaModel registrar(Integer idRegistroAsistencia, Long idEmpleado,
                                                 String motivo, String evidenciaUrl) {
        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, idRegistroAsistencia);
        if (r == null) {
            throw new RuntimeException("Registro de asistencia no encontrado");
        }

        LocalDate fechaRegistro = r.getFecha();
        if (LocalDate.now().isAfter(fechaRegistro.plusDays(1))) {
            throw new RuntimeException("El plazo de justificacion vencio. Solo puedes justificar el mismo dia o el dia siguiente.");
        }

        JustificacionTardanzaModel j = new JustificacionTardanzaModel();
        j.setIdRegistroAsistencia(idRegistroAsistencia);
        j.setIdEmpleado(idEmpleado);
        j.setMotivo(motivo);
        j.setEvidenciaUrl(evidenciaUrl);
        j.setEstado("PENDIENTE");
        j.setFechaJustificacion(LocalDateTime.now());

        r.setObservacion("Justificacion pendiente de aprobacion");
        em.merge(r);

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

        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
        if (r != null && "APROBADA".equals(decision)) {
            int tardanzaOriginal = r.getMinutosTardanza();
            LocalTime entrada = r.getHoraEntrada() != null ? r.getHoraEntrada() : LocalTime.of(8, 0);
            LocalTime salida = r.getHoraSalida() != null
                    ? r.getHoraSalida().plusMinutes(tardanzaOriginal)
                    : entrada.plusHours(8);

            RegistroAsistenciaModel correccion = new RegistroAsistenciaModel();
            correccion.setEmpleado(r.getEmpleado());
            correccion.setFecha(r.getFecha());
            correccion.setHoraEntrada(entrada);
            correccion.setHoraSalida(salida);
            correccion.setHorasTrabajadas(new java.math.BigDecimal("8.00"));
            correccion.setMinutosTardanza(0);
            correccion.setEstado("JUSTIFICADA");
            correccion.setTipoRegistro("CORRECCION");
            correccion.setObservacion("Tardanza original: " + tardanzaOriginal + " min. Horas recuperadas.");
            em.persist(correccion);
        }

        return em.merge(j);
    }

    private HorarioModel getHorarioAsignado(Long idEmpleado) {
        List<AsignacionHorarioModel> asignaciones = em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true",
                AsignacionHorarioModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
        if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
            return asignaciones.get(0).getHorario();
        }
        return null;
    }

    public List<JustificacionTardanzaModel> listarPendientes() {
        List<JustificacionTardanzaModel> pendientes = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.estado = 'PENDIENTE'",
                JustificacionTardanzaModel.class).getResultList();
        for (JustificacionTardanzaModel j : pendientes) {
            if (j.getIdRegistroAsistencia() != null) {
                RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
                if (r != null) {
                    if (r.getHoraEntrada() != null) {
                        j.setHoraEntrada(r.getHoraEntrada().toString());
                    }
                    if (r.getFecha() != null) {
                        j.setFechaAsistencia(r.getFecha().toString());
                    }
                }
            }
        }
        return pendientes;
    }

    public List<JustificacionTardanzaModel> listarPorEmpleado(Long idEmpleado) {
        return em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.idEmpleado = :idEmpleado ORDER BY j.fechaJustificacion DESC",
                JustificacionTardanzaModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
    }
}
