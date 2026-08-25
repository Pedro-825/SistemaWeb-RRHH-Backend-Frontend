import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import ApiAlert from "../components/ApiAlert";
import "../styles/Dashboard.css";
import { listarTodasJustificaciones } from "../services/api";

const estadoBadge = (estado) => {
  if (estado === "APROBADA")  return "badge-sol-aprobada";
  if (estado === "RECHAZADA") return "badge-sol-rechazada";
  return "badge-sol-pendiente";
};

export default function GerenciaJustificaciones() {
  const [justificaciones, setJustificaciones] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState("");

  useEffect(() => { cargar(); }, []);

  async function cargar() {
    setLoading(true); setError("");
    try {
      const data = await listarTodasJustificaciones();
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err || "No se pudieron cargar las justificaciones de tardanza.");
    } finally {
      setLoading(false);
    }
  }

  const pendientes = justificaciones.filter(j => (j.estado ?? "PENDIENTE") === "PENDIENTE").length;
  const aprobadas  = justificaciones.filter(j => j.estado === "APROBADA").length;
  const rechazadas = justificaciones.filter(j => j.estado === "RECHAZADA").length;

  return (
    <div className="dashboard">
      <Sidebar rol="GERENCIA" />

      <main className="main-content">
        <header className="main-header" style={{ borderBottom: "3px solid #7c3aed" }}>
          <div className="header-left">
            <span className="page-breadcrumb">Gerencia / Justificaciones de Tardanza</span>
            <h1>Supervisión de Justificaciones de Tardanza</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Vista de solo lectura — el sistema y RRHH gestionan las aprobaciones automáticamente.
            </p>
          </div>
          <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
            <button className="btn btn-secondary btn-sm" onClick={cargar} disabled={loading}>
              🔄 Actualizar
            </button>
            <span className="role-pill role-pill-gerencia">Gerencia</span>
          </div>
        </header>

        <ApiAlert type="error" message={error} />

        {/* Estadísticas */}
        <div className="emp-stats-grid" style={{ marginBottom: 24 }}>
          {[
            { label: "Total Registros",       value: justificaciones.length, color: "#7c3aed" },
            { label: "Pendientes de Revisión", value: pendientes,            color: "#f59e0b" },
            { label: "Aprobadas",             value: aprobadas,              color: "#10b981" },
            { label: "Rechazadas",            value: rechazadas,             color: "#ef4444" },
          ].map(s => (
            <div key={s.label} className="rrhh-stat-card" style={{ borderLeft: `4px solid ${s.color}` }}>
              <div className="rrhh-stat-label">{s.label}</div>
              <div className="rrhh-stat-value" style={{ color: s.color }}>{s.value}</div>
            </div>
          ))}
        </div>

        {/* Tabla */}
        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <h2 style={{ margin: 0 }}>
              Justificaciones de Tardanza
              <span className="badge badge-gray" style={{ marginLeft: 8, fontSize: 12 }}>
                {justificaciones.length} registros
              </span>
            </h2>
            <span style={{ fontSize: 12, color: "var(--text-3)", display: "flex", alignItems: "center", gap: 6 }}>
              <span style={{ display: "inline-block", width: 8, height: 8, borderRadius: "50%", background: "#7c3aed" }} />
              Sistema automatizado — procesado por RRHH
            </span>
          </div>
          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? (
              <Spinner />
            ) : (
              <table className="table-cards">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Empleado</th>
                    <th>Fecha Tardanza</th>
                    <th>Motivo</th>
                    <th>Estado</th>
                    <th>Fecha Registro</th>
                    <th>Comentario RRHH</th>
                  </tr>
                </thead>
                <tbody>
                  {justificaciones.length === 0 ? (
                    <tr>
                      <td colSpan={7}>
                        <div className="empty-state">
                          <div className="empty-state-icon">✅</div>
                          <p>No hay justificaciones de tardanza registradas en el sistema.</p>
                        </div>
                      </td>
                    </tr>
                  ) : justificaciones.map((j, i) => (
                    <tr key={j.idJustificacion ?? j.id ?? i}>
                      <td style={{ color: "var(--text-3)", fontSize: 13 }}>
                        {j.idJustificacion ?? j.id ?? i + 1}
                      </td>
                      <td data-label="Empleado">
                        <strong>{j.nombreEmpleado ?? `Emp. #${j.idEmpleado}`}</strong>
                      </td>
                      <td data-label="Fecha">{j.fechaAsistencia ?? "—"}</td>
                      <td data-label="Motivo" style={{ maxWidth: 260, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {j.motivo ?? "—"}
                      </td>
                      <td data-label="Estado">
                        <span className={`badge ${estadoBadge(j.estado ?? "PENDIENTE")}`}>
                          {j.estado ?? "PENDIENTE"}
                        </span>
                      </td>
                      <td data-label="Registrado" style={{ color: "var(--text-3)", fontSize: 12 }}>
                        {j.fechaJustificacion ? String(j.fechaJustificacion).slice(0, 16).replace("T", " ") : "—"}
                      </td>
                      <td data-label="Comentario" style={{ fontSize: 12, color: "var(--text-2)" }}>
                        {j.comentarioRevision ?? <span style={{ color: "var(--text-3)" }}>—</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Nota informativa */}
        <div className="section-card" style={{ marginTop: 20, background: "#faf5ff", border: "1px solid #e9d5ff" }}>
          <h2 style={{ color: "#7c3aed" }}>ℹ️ Acerca de este módulo</h2>
          <p style={{ fontSize: 13, color: "var(--text-2)", margin: "8px 0 0", lineHeight: 1.8 }}>
            Este módulo muestra todas las justificaciones de tardanza registradas por los empleados.
            El proceso es <strong>automatizado</strong>: los empleados registran sus justificaciones,
            RRHH las revisa y aprueba o rechaza según los procedimientos internos.
            Gerencia tiene visibilidad de todo el proceso en tiempo real, sin necesidad de intervención manual.
          </p>
        </div>
      </main>
    </div>
  );
}
