package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    @PersistenceContext
    private EntityManager em;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    public HorarioService(AuditoriaEmpleadoService auditoriaEmpleadoService) {
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    public List<HorarioModel> listar() {
        return em.createQuery("SELECT h FROM HorarioModel h WHERE h.activo = true ORDER BY h.idHorario", HorarioModel.class)
                .getResultList();
    }

    public HorarioModel buscarPorId(Integer id) {
        return em.find(HorarioModel.class, id);
    }

    @Transactional
    public HorarioModel guardar(Horario horario) {
        HorarioModel model;
        if (horario.getIdHorario() != null) {
            model = em.find(HorarioModel.class, horario.getIdHorario());
            if (model == null) throw new RuntimeException("Horario no encontrado");
        } else {
            model = new HorarioModel();
        }
        model.setNombreTurno(horario.getNombreTurno());
        model.setHoraEntrada(horario.getHoraEntrada());
        model.setHoraSalida(horario.getHoraSalida());
        model.setTolerancia(horario.getTolerancia() != null ? horario.getTolerancia() : 10);
        model.setUmbralExtra(horario.getUmbralExtra() != null ? horario.getUmbralExtra() : 60);
        model.setActivo(true);
        return em.merge(model);
    }

    @Transactional
    public void eliminar(Integer id) {
        HorarioModel model = em.find(HorarioModel.class, id);
        if (model != null) {
            model.setActivo(false);
            em.merge(model);
        }
    }

    @Transactional
    public Map<String, Object> asignarHorario(Long idEmpleado, Integer idHorario,
                                               LocalDate fechaDesde, LocalDate fechaHasta,
                                               Boolean esTemporal) {
        LocalDate inicioNueva = fechaDesde != null ? fechaDesde : LocalDate.now();

        String valorAnterior = buscarTurnoActivo(idEmpleado)
                .map(a -> a.getHorario().getNombreTurno() + " (" + a.getHorario().getHoraEntrada()
                        + "-" + a.getHorario().getHoraSalida() + ")")
                .orElse("Sin turno asignado");

        // Cerrar asignaciones activas anteriores con fechaHasta = día anterior al inicio de la nueva,
        // para que los rangos no se solapen y las justificaciones históricas encuentren el horario correcto.
        cerrarAsignacionesActivas(idEmpleado, inicioNueva.minusDays(1));

        EmpleadoModel empleadoModel = em.find(EmpleadoModel.class, idEmpleado);
        HorarioModel horarioModel   = em.find(HorarioModel.class, idHorario);

        if (empleadoModel == null) throw new RuntimeException("Empleado no encontrado");
        if (horarioModel  == null) throw new RuntimeException("Horario no encontrado");

        AsignacionHorarioModel asignacion = new AsignacionHorarioModel();
        asignacion.setEmpleado(empleadoModel);
        asignacion.setHorario(horarioModel);
        asignacion.setFechaDesde(inicioNueva);
        asignacion.setFechaHasta(fechaHasta);
        asignacion.setEsTemporal(esTemporal != null ? esTemporal : false);
        asignacion.setActivo(true);
        em.persist(asignacion);
        em.flush();

        String valorNuevo = horarioModel.getNombreTurno() + " (" + horarioModel.getHoraEntrada()
                + "-" + horarioModel.getHoraSalida() + "), vigente desde " + inicioNueva
                + (fechaHasta != null ? " hasta " + fechaHasta : "");

        auditoriaEmpleadoService.registrar(
                idEmpleado,
                "ASIGNACION_HORARIO",
                "ASIGNACION_HORARIO",
                valorAnterior,
                valorNuevo
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("idAsignacion", asignacion.getIdAsignacion());
        result.put("idEmpleado",   idEmpleado);
        result.put("idHorario",    idHorario);
        result.put("nombreTurno",  horarioModel.getNombreTurno());
        result.put("horaEntrada",  horarioModel.getHoraEntrada().toString());
        result.put("horaSalida",   horarioModel.getHoraSalida().toString());
        result.put("fechaDesde",   inicioNueva.toString());
        result.put("fechaHasta",   fechaHasta != null ? fechaHasta.toString() : null);
        result.put("esTemporal",   asignacion.getEsTemporal());
        return result;
    }

    private Optional<AsignacionHorarioModel> buscarTurnoActivo(Long idEmpleado) {
        return em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a " +
                "WHERE a.empleado.idEmpleado = :idEmp AND a.activo = true " +
                "ORDER BY a.fechaDesde DESC", AsignacionHorarioModel.class)
                .setParameter("idEmp", idEmpleado)
                .setMaxResults(1)
                .getResultList()
                .stream().findFirst();
    }

    @Transactional
    public void cerrarAsignacionesActivas(Long idEmpleado, LocalDate fechaCierre) {
        em.createQuery(
                "UPDATE AsignacionHorarioModel a SET a.activo = false, a.fechaHasta = :cierre " +
                "WHERE a.empleado.idEmpleado = :idEmp AND a.activo = true")
                .setParameter("idEmp", idEmpleado)
                .setParameter("cierre", fechaCierre)
                .executeUpdate();
    }

    /** Historial de asignaciones de un empleado (más reciente primero). */
    public List<Map<String, Object>> listarAsignacionesPorEmpleado(Long idEmpleado) {
        List<Object[]> rows = em.createQuery(
                "SELECT a.idAsignacion, a.horario.idHorario, a.horario.nombreTurno, " +
                "       a.horario.horaEntrada, a.horario.horaSalida, " +
                "       a.fechaDesde, a.fechaHasta, a.esTemporal, a.activo " +
                "FROM AsignacionHorarioModel a " +
                "WHERE a.empleado.idEmpleado = :idEmp " +
                "ORDER BY a.creadoEl DESC", Object[].class)
                .setParameter("idEmp", idEmpleado)
                .getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idAsignacion", r[0]);
            m.put("idHorario",    r[1]);
            m.put("nombreTurno",  r[2]);
            m.put("horaEntrada",  r[3] != null ? r[3].toString() : null);
            m.put("horaSalida",   r[4] != null ? r[4].toString() : null);
            m.put("fechaDesde",   r[5] != null ? r[5].toString() : null);
            m.put("fechaHasta",   r[6] != null ? r[6].toString() : null);
            m.put("esTemporal",   r[7]);
            m.put("activo",       r[8]);
            return m;
        }).collect(Collectors.toList());
    }

    /** Empleados actualmente asignados a un horario específico. */
    public List<Map<String, Object>> getEmpleadosPorHorario(Integer idHorario) {
        List<Object[]> rows = em.createQuery(
                "SELECT a.empleado.idEmpleado, a.empleado.nombres, a.empleado.apellidos, a.fechaDesde " +
                "FROM AsignacionHorarioModel a " +
                "WHERE a.horario.idHorario = :idHorario AND a.activo = true " +
                "ORDER BY a.empleado.apellidos", Object[].class)
                .setParameter("idHorario", idHorario)
                .getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idEmpleado", r[0]);
            m.put("nombres",    r[1]);
            m.put("apellidos",  r[2]);
            m.put("fechaDesde", r[3] != null ? r[3].toString() : null);
            return m;
        }).collect(Collectors.toList());
    }

    /** Resumen de asignaciones activas para todos los empleados (para poblar la tabla). */
    public List<Map<String, Object>> getAsignacionesActivasResumen() {
        List<Object[]> rows = em.createQuery(
                "SELECT a.empleado.idEmpleado, a.horario.idHorario, a.horario.nombreTurno, " +
                "       a.horario.horaEntrada, a.horario.horaSalida " +
                "FROM AsignacionHorarioModel a WHERE a.activo = true", Object[].class)
                .getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("idEmpleado",  r[0]);
            m.put("idHorario",   r[1]);
            m.put("nombreTurno", r[2]);
            m.put("horaEntrada", r[3] != null ? r[3].toString() : null);
            m.put("horaSalida",  r[4] != null ? r[4].toString() : null);
            return m;
        }).collect(Collectors.toList());
    }
}
