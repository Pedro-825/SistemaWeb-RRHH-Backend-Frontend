import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import ApiAlert from "../components/ApiAlert";
import "../styles/Dashboard.css";
import {
  registrarSolicitud,
  getSolicitudesPorEmpleado,
  getSolicitudesPorEstado,
  revisarSolicitudRRHH,
  decidirSolicitudGerencia,
  getIdEmpleado,
} from "../services/api";

const STEP_COLORS = ["#ef4444", "#f97316", "#eab308", "#22c55e"];

const ESTADO_FLOW_STEP = {
  PENDIENTE:   0,
  OBSERVADO:   0,
  PROCESADO:   2,
  APROBADA:    3,
  RECHAZADA:   3,
};

function FlowBar({ estadoSolicitud }) {
  const steps = ["EMPLEADO", "RRHH", "GERENCIA", "RESULTADO"];
  const activeUpTo = estadoSolicitud != null ? (ESTADO_FLOW_STEP[estadoSolicitud] ?? -1) : -1;
  const hasSelection = estadoSolicitud != null;

  return (
    <div className="sol-flow-bar">
      {steps.map((s, i) => {
        const baseColor = STEP_COLORS[i];
        const color = (hasSelection && i === 3 && estadoSolicitud === "RECHAZADA") ? "#ef4444" : baseColor;
        const lit = hasSelection && i <= activeUpTo;
        return (
          <div key={s} className="sol-flow-step">
            <div
              className="sol-flow-dot"
              style={{
                background: lit ? color : "#f8fafc",
                color:      lit ? "#fff" : color,
                border:     `2px solid ${color}`,
                fontWeight: 700,
                opacity:    hasSelection && !lit ? 0.35 : 1,
                transition: "all 0.3s",
              }}
            >
              {i + 1}
            </div>
            <span style={{
              color:      lit ? color : "var(--text-3)",
              fontWeight: hasSelection && i === activeUpTo ? 700 : 600,
              opacity:    hasSelection && !lit ? 0.35 : 1,
              transition: "all 0.3s",
            }}>
              {s}
            </span>
            {i < steps.length - 1 && (
              <div className="sol-flow-line" style={{
                background: hasSelection && i < activeUpTo ? "#22c55e" : "var(--border)",
                transition: "background 0.3s",
              }} />
            )}
          </div>
        );
      })}
    </div>
  );
}

const ESTADO_TAB_CONFIG = {
  PENDIENTE: { label: "Pendiente",   icon: "⏳", color: "#d97706", bg: "#fef3c7", activeBg: "#f59e0b" },
  PROCESADO: { label: "En Revisión", icon: "🔍", color: "#2563eb", bg: "#dbeafe", activeBg: "#3b82f6" },
  APROBADA:  { label: "Aprobado",    icon: "✅", color: "#16a34a", bg: "#dcfce7", activeBg: "#22c55e" },
  RECHAZADA: { label: "Rechazado",   icon: "❌", color: "#dc2626", bg: "#fee2e2", activeBg: "#ef4444" },
};

const TIPO_LABELS = {
  VACACIONES:       "Vacaciones",
  PERMISO_PERSONAL: "Permiso Personal",
  PERMISO_MEDICO:   "Permiso Médico",
  OTROS:            "Otros",
};

const estadoBadge = (estado) => {
  const m = {
    PENDIENTE:   "badge-sol-pendiente",
    OBSERVADO:   "badge-sol-pendiente",
    PROCESADO:   "badge-sol-gerencia",
    APROBADA:    "badge-sol-aprobada",
    RECHAZADA:   "badge-sol-rechazada",
  };
  return m[estado] ?? "badge-gray";
};

/* ══════════════════════════════════════════════════════════
   VISTA EMPLEADO
══════════════════════════════════════════════════════════ */
function VistaEmpleado() {
  const idEmpleado = getIdEmpleado();
  const [solicitudes, setSolicitudes] = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [mensaje,  setMensaje]  = useState("");
  const [error,    setError]    = useState("");
  const [enviando, setEnviando] = useState(false);
  const [tabActiva, setTabActiva] = useState("lista");
  const [solicitudActiva, setSolicitudActiva] = useState(null);
  const [form, setForm] = useState({
    tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "",
  });

  useEffect(() => { if (idEmpleado) cargarMisSolicitudes(); }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  async function cargarMisSolicitudes() {
    setLoading(true);
    setError("");
    try {
      const data = await getSolicitudesPorEmpleado(idEmpleado);
      const lista = Array.isArray(data) ? data : [];
      setSolicitudes(lista);
      const idsVistas = lista
        .filter(s => s.estado === "APROBADA" || s.estado === "RECHAZADA")
        .map(s => s.idSolicitud)
        .filter(id => id != null);
      if (idsVistas.length > 0)
        localStorage.setItem(`notif_vistas_${idEmpleado}`, JSON.stringify(idsVistas));
    } catch { setError("No se pudieron cargar tus solicitudes."); }
    finally  { setLoading(false); }
  }

  const handleEnviar = async (e) => {
    e.preventDefault();
    if (!idEmpleado) { setError("No se pudo obtener tu ID de empleado. Contacta a RRHH."); return; }
    if (form.fechaInicio && form.fechaFin && form.fechaInicio > form.fechaFin) {
      setError("La fecha de inicio no puede ser mayor que la fecha fin.");
      return;
    }
    setEnviando(true); setMensaje(""); setError("");
    try {
      await registrarSolicitud({ tipoSolicitud: form.tipo, fechaInicio: form.fechaInicio, fechaFin: form.fechaFin, motivo: form.descripcion });
      setMensaje("Solicitud enviada correctamente. RRHH la revisará pronto.");
      setForm({ tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "" });
      setTabActiva("lista");
      cargarMisSolicitudes();
    } catch (e) { setError(e || "Error al enviar la solicitud. Intente de nuevo."); }
    finally  { setEnviando(false); }
  };

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />
      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Mis Solicitudes</span>
            <h1>Solicitud de Permisos y Licencias</h1>
          </div>
          <span className="role-pill role-pill-empleado">Empleado</span>
        </header>

        <ApiAlert type="success" message={mensaje} />
        <ApiAlert type="error" message={error} />

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "lista" ? "active" : ""}`}
            onClick={() => { setTabActiva("lista"); cargarMisSolicitudes(); }}>
            📋 Mis Solicitudes
          </button>
          <button className={`tab ${tabActiva === "nueva" ? "active" : ""}`}
            onClick={() => setTabActiva("nueva")}>
            ➕ Nueva Solicitud
          </button>
        </div>

        {tabActiva === "lista" && (
          <div className="section-card">
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 12, flexWrap: "wrap", gap: 8 }}>
              <h2 style={{ margin: 0 }}>Mis Solicitudes</h2>
            </div>
            <div className="table-wrapper">
              {loading ? <Spinner /> : (
                <table>
                  <thead>
                    <tr><th>#</th><th>Tipo</th><th>Desde</th><th>Hasta</th><th>Estado</th><th>Rev. RRHH</th><th>Decisión Gerencia</th></tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={7}><div className="empty-state"><div className="empty-state-icon">📋</div><p>No tienes solicitudes registradas</p></div></td></tr>
                    ) : solicitudes.map((s, idx) => {
                      const id = s.idSolicitud;
                      const isSelected = solicitudActiva?.idSolicitud === id;
                      return (
                        <tr key={id}
                          onClick={() => setSolicitudActiva(isSelected ? null : s)}
                          style={{ cursor: "pointer", background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{idx + 1}</td>
                          <td>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td>{s.fechaInicio ?? "—"}</td>
                          <td>{s.fechaFin ?? "—"}</td>
                          <td><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                          <td style={{ fontSize: 12, color: "var(--text-2)", maxWidth: 180, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                            {s.observacionRrhh ?? s.comentarioRRHH ?? <span style={{ color: "var(--text-3)" }}>—</span>}
                          </td>
                          <td style={{ fontSize: 12, color: s.respuesta ? "#16a34a" : "var(--text-3)", maxWidth: 180, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                            {s.respuesta ?? "—"}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {tabActiva === "nueva" && (
          <div className="sol-emp-layout">
            <div className="section-card">
              <h2>➕ Nueva Solicitud de Permiso</h2>
              <div className="alert alert-info" style={{ marginBottom: 16 }}>
                Las solicitudes pasan por revisión de RRHH y luego Gerencia (según el tipo).
              </div>
              <form onSubmit={handleEnviar}>
                <div className="form-grid solicitud-form-grid">
                  <div className="form-group">
                    <label>Tipo de Solicitud *</label>
                    <select value={form.tipo} onChange={e => setForm({ ...form, tipo: e.target.value })}>
                      {Object.entries(TIPO_LABELS).map(([v, l]) => <option key={v} value={v}>{l}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Fecha de Inicio *</label>
                    <input type="date" value={form.fechaInicio}
                      onChange={e => setForm({ ...form, fechaInicio: e.target.value })} required />
                  </div>
                  <div className="form-group">
                    <label>Fecha de Fin *</label>
                    <input type="date" value={form.fechaFin}
                      onChange={e => setForm({ ...form, fechaFin: e.target.value })} required />
                  </div>
                  <div className="form-group form-full">
                    <label>Motivo / Descripción *</label>
                    <textarea rows={4} value={form.descripcion}
                      onChange={e => setForm({ ...form, descripcion: e.target.value })}
                      placeholder="Describa el motivo de su solicitud de permiso..." required />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={enviando}>
                    {enviando ? <><span className="btn-spinner" />Enviando...</> : "📤 Enviar Solicitud"}
                  </button>
                  <button type="button" className="btn btn-secondary"
                    onClick={() => setForm({ tipo: "VACACIONES", fechaInicio: "", fechaFin: "", descripcion: "" })}>
                    Limpiar
                  </button>
                </div>
              </form>
            </div>

            <div className="notif-panel">
              <div className="section-card">
                <h2>⚡ ¿Cómo funciona?</h2>
                <ol style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "var(--text-2)", lineHeight: "2" }}>
                  <li>Completa el formulario con los datos de tu permiso.</li>
                  <li>RRHH revisa y valida la información.</li>
                  <li>Si requiere aprobación de Gerencia, pasa al siguiente nivel.</li>
                  <li>Recibes la notificación con la decisión final.</li>
                </ol>
                <div className="alert alert-warning" style={{ marginTop: 12 }}>
                  <strong>Nota:</strong> Las vacaciones deben solicitarse con al menos 5 días de anticipación.
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   VISTA RRHH
══════════════════════════════════════════════════════════ */
function VistaRRHH() {
  const [todasSolicitudes, setTodasSolicitudes] = useState([]);
  const [loading,          setLoading]          = useState(false);
  const [mensaje,          setMensaje]          = useState("");
  const [error,            setError]            = useState("");
  const [filtroEstado,     setFiltroEstado]     = useState("PENDIENTE");
  const [solicitudActiva,  setSolicitudActiva]  = useState(null);
  const [modalRevision,    setModalRevision]    = useState({ open: false, solicitud: null, decision: "", comentario: "" });
  const [procesando,       setProcesando]       = useState(false);

  // Derivado: solo las del filtro activo (para la tabla)
  const solicitudes = todasSolicitudes.filter(s => s.estado === filtroEstado);

  useEffect(() => { cargarSolicitudes(); }, []);

  async function cargarSolicitudes() {
    setLoading(true); setError("");
    try {
      const [pend, enRev, apro, rech] = await Promise.all([
        getSolicitudesPorEstado("PENDIENTE"),
        getSolicitudesPorEstado("PROCESADO"),
        getSolicitudesPorEstado("APROBADA"),
        getSolicitudesPorEstado("RECHAZADA"),
      ]);
      setTodasSolicitudes([
        ...(Array.isArray(pend)  ? pend  : []),
        ...(Array.isArray(enRev) ? enRev : []),
        ...(Array.isArray(apro)  ? apro  : []),
        ...(Array.isArray(rech)  ? rech  : []),
      ]);
    } catch { setError("No se pudieron cargar las solicitudes"); }
    finally  { setLoading(false); }
  }

  const contarPorEstado = (e) => todasSolicitudes.filter(s => s.estado === e).length;

  const handleRevisar = async () => {
    const esDevolver = modalRevision.decision === "DEVOLVER";
    if (!modalRevision.decision) return;
    if (esDevolver && !modalRevision.comentario.trim()) return;
    setProcesando(true);
    try {
      await revisarSolicitudRRHH(modalRevision.solicitud.idSolicitud, {
        decision: modalRevision.decision, comentario: modalRevision.comentario,
      });
      setMensaje(`Solicitud ${modalRevision.decision === "APROBAR" ? "enviada a Gerencia" : "devuelta al empleado"} correctamente`);
      setModalRevision({ open: false, solicitud: null, decision: "", comentario: "" });
      await cargarSolicitudes();
    } catch {
      setError("Error al procesar la revisión");
      setModalRevision({ open: false, solicitud: null, decision: "", comentario: "" });
    } finally { setProcesando(false); }
  };

  const stats = [
    { label: "Pendientes",  value: contarPorEstado("PENDIENTE"),  color: "#f59e0b" },
    { label: "En revisión", value: contarPorEstado("PROCESADO"),  color: "#3b82f6" },
    { label: "Aprobadas",   value: contarPorEstado("APROBADA"),   color: "#10b981" },
    { label: "Rechazadas",  value: contarPorEstado("RECHAZADA"),  color: "#ef4444" },
  ];

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />
      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Solicitud de Permisos</span>
            <h1>Solicitud de Permisos y Licencias</h1>
          </div>
          <span className="role-pill role-pill-rrhh">RRHH</span>
        </header>

        <ApiAlert type="success" message={mensaje} />
        <ApiAlert type="error" message={error} />

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        <div className="emp-stats-grid" style={{ marginBottom: 24 }}>
          {stats.map(s => (
            <div key={s.label} className="rrhh-stat-card" style={{ borderLeft: `4px solid ${s.color}` }}>
              <div className="rrhh-stat-label">{s.label}</div>
              <div className="rrhh-stat-value" style={{ color: s.color }}>{s.value}</div>
            </div>
          ))}
        </div>

        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)" }}>
            <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
              {["PENDIENTE", "PROCESADO", "APROBADA", "RECHAZADA"].map(e => {
                const cfg = ESTADO_TAB_CONFIG[e];
                const isActive = filtroEstado === e;
                return (
                  <button key={e} onClick={() => setFiltroEstado(e)} style={{
                    padding: "7px 16px",
                    borderRadius: "20px",
                    border: `1.5px solid ${cfg.activeBg}`,
                    background: isActive ? cfg.activeBg : cfg.bg,
                    color: isActive ? "#fff" : cfg.color,
                    fontWeight: 600,
                    fontSize: 12,
                    cursor: "pointer",
                    fontFamily: "inherit",
                    transition: "all 0.18s",
                    display: "flex",
                    alignItems: "center",
                    gap: 5,
                  }}>
                    {cfg.icon} {cfg.label}
                  </button>
                );
              })}
              <button className="btn btn-secondary btn-sm" style={{ marginLeft: "auto" }}
                onClick={cargarSolicitudes}>🔄 Actualizar</button>
            </div>
          </div>
          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? <Spinner text="Cargando solicitudes..." /> : (
              <table>
                <thead>
                  <tr><th>#</th><th>Empleado</th><th>Tipo</th><th>Inicio</th><th>Fin</th><th>Motivo</th><th>Coment. RRHH</th><th>Decisión Gerencia</th><th>Estado</th>{filtroEstado === "PENDIENTE" && <th>Acciones</th>}</tr>
                </thead>
                <tbody>
                  {solicitudes.length === 0 ? (
                    <tr><td colSpan={filtroEstado === "PENDIENTE" ? 10 : 9}><div className="empty-state"><div className="empty-state-icon">✅</div><p>No hay solicitudes en estado {filtroEstado}</p></div></td></tr>
                  ) : solicitudes.map((s, idx) => {
                    const id = s.idSolicitud;
                    const isSelected = solicitudActiva?.idSolicitud === id;
                    return (
                      <tr key={id}
                        onClick={() => setSolicitudActiva(isSelected ? null : s)}
                        style={{ cursor: "pointer", background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                        <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{idx + 1}</td>
                        <td>{s.nombreEmpleado ?? s.idEmpleado}</td>
                        <td>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                        <td>{s.fechaInicio ?? "—"}</td>
                        <td>{s.fechaFin ?? "—"}</td>
                        <td style={{ maxWidth: "150px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{s.motivo}</td>
                        <td style={{ fontSize: 12, color: "var(--text-2)", maxWidth: 140, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                          {s.observacionRrhh ?? <span style={{ color: "var(--text-3)" }}>—</span>}
                        </td>
                        <td style={{ fontSize: 12, color: s.respuesta ? "#16a34a" : "var(--text-3)", maxWidth: 140, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                          {s.respuesta ?? "—"}
                        </td>
                        <td><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                        {filtroEstado === "PENDIENTE" && (
                          <td>
                            <button className="btn btn-primary btn-sm"
                              onClick={e => { e.stopPropagation(); setModalRevision({ open: true, solicitud: s, decision: "", comentario: "" }); }}>
                              Revisar
                            </button>
                          </td>
                        )}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </main>

      {modalRevision.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>📋 Revisar Solicitud #{modalRevision.solicitud?.idSolicitud}</h3>
                <p>{TIPO_LABELS[modalRevision.solicitud?.tipoSolicitud] ?? modalRevision.solicitud?.tipoSolicitud} — {modalRevision.solicitud?.nombreEmpleado ?? modalRevision.solicitud?.idEmpleado}</p>
              </div>
              <button className="modal-close" onClick={() => setModalRevision({ ...modalRevision, open: false })}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Decisión *</label>
                <select value={modalRevision.decision}
                  onChange={e => setModalRevision({ ...modalRevision, decision: e.target.value })}>
                  <option value="">Seleccione</option>
                  <option value="APROBAR">✅ Aprobar y pasar a Gerencia</option>
                  <option value="DEVOLVER">↩ Devolver al empleado</option>
                </select>
              </div>
              <div className="form-group">
                <label>
                  Comentario {modalRevision.decision === "DEVOLVER" ? "*" : "(opcional)"}
                </label>
                <textarea rows={3} value={modalRevision.comentario}
                  onChange={e => setModalRevision({ ...modalRevision, comentario: e.target.value })}
                  placeholder={modalRevision.decision === "DEVOLVER"
                    ? "Indique el motivo por el que devuelve la solicitud..."
                    : "Observación para Gerencia (opcional)..."} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalRevision({ ...modalRevision, open: false })}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleRevisar}
                disabled={!modalRevision.decision || (modalRevision.decision === "DEVOLVER" && !modalRevision.comentario.trim()) || procesando}>
                {procesando ? "Procesando..." : "Confirmar Revisión"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   VISTA GERENCIA
══════════════════════════════════════════════════════════ */
function VistaGerencia() {
  const [solicitudes,    setSolicitudes]    = useState([]);
  const [solAprobadas,   setSolAprobadas]   = useState([]);
  const [solRechazadas,  setSolRechazadas]  = useState([]);
  const [loading,        setLoading]        = useState(false);
  const [mensaje,        setMensaje]        = useState("");
  const [error,          setError]          = useState("");
  const [solicitudActiva,  setSolicitudActiva]  = useState(null);
  const [modalDecision,    setModalDecision]    = useState({ open: false, solicitud: null, decision: "", comentario: "" });
  const [procesando,       setProcesando]       = useState(false);

  useEffect(() => { cargarSolicitudes(); }, []);

  async function cargarSolicitudes() {
    setLoading(true); setError("");
    try {
      const [enRev, aprob, recha] = await Promise.all([
        getSolicitudesPorEstado("PROCESADO"),
        getSolicitudesPorEstado("APROBADA"),
        getSolicitudesPorEstado("RECHAZADA"),
      ]);
      setSolicitudes(Array.isArray(enRev) ? enRev : []);
      setSolAprobadas(Array.isArray(aprob) ? aprob : []);
      setSolRechazadas(Array.isArray(recha) ? recha : []);
    } catch { setError("No se pudieron cargar las solicitudes"); }
    finally  { setLoading(false); }
  }

  const handleDecidir = async () => {
    const esRechazo = modalDecision.decision === "RECHAZAR";
    if (!modalDecision.decision) return;
    if (esRechazo && !modalDecision.comentario.trim()) return;
    setProcesando(true);
    try {
      await decidirSolicitudGerencia(
        modalDecision.solicitud.idSolicitud,
        { decision: modalDecision.decision, comentario: modalDecision.comentario }
      );
      setMensaje(`Solicitud ${modalDecision.decision === "APROBAR" ? "aprobada" : "rechazada"} correctamente`);
      setModalDecision({ open: false, solicitud: null, decision: "", comentario: "" });
      await cargarSolicitudes();
    } catch {
      setError("Error al procesar la decisión");
      setModalDecision({ open: false, solicitud: null, decision: "", comentario: "" });
    } finally { setProcesando(false); }
  };

  return (
    <div className="dashboard">
      <Sidebar rol="GERENCIA" />

      <main className="main-content">
        <header className="main-header header-gerencia">
          <div className="header-left">
            <span className="page-breadcrumb">Gerencia / Solicitudes de Permisos</span>
            <h1>Decisión Final — Solicitudes de Permisos</h1>
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <button className="btn btn-secondary" onClick={cargarSolicitudes}>🔄 Actualizar</button>
            <span className="role-pill role-pill-gerencia">Gestión Ejecutiva</span>
          </div>
        </header>

        <ApiAlert type="success" message={mensaje} style={{ marginTop: 16 }} />
        <ApiAlert type="error" message={error} style={{ marginTop: 16 }} />

        <FlowBar estadoSolicitud={solicitudActiva?.estado} />

        {/* ── Tabla: En Revisión (decisión pendiente) ── */}
        <div style={{ marginTop: 16 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#f97316", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#f97316", display: "inline-block" }} />
            Esperando Decisión ({solicitudes.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? <Spinner /> : (
                <table className="table-gerencia-permisos">
                  <thead>
                    <tr><th>#</th><th>Empleado</th><th>Tipo</th><th>Inicio</th><th>Fin</th><th>Motivo</th><th>Rev. RRHH</th><th>Estado</th><th>Decisión</th></tr>
                  </thead>
                  <tbody>
                    {solicitudes.length === 0 ? (
                      <tr><td colSpan={9}><div className="empty-state"><div className="empty-state-icon">✅</div><p>No hay solicitudes pendientes de decisión</p></div></td></tr>
                    ) : solicitudes.map((s, idx) => {
                      const id = s.idSolicitud;
                      const isSelected = solicitudActiva?.idSolicitud === id;
                      return (
                        <tr key={id}
                          onClick={() => setSolicitudActiva(isSelected ? null : s)}
                          style={{ cursor: "pointer", background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{idx + 1}</td>
                          <td>{s.nombreEmpleado ?? s.idEmpleado}</td>
                          <td>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td>{s.fechaInicio ?? "—"}</td>
                          <td>{s.fechaFin ?? "—"}</td>
                          <td>{s.motivo}</td>
                          <td>{s.observacionRrhh ?? "—"}</td>
                          <td><span className={`badge ${estadoBadge(s.estado)}`}>{s.estado}</span></td>
                          <td>
                            <div style={{ display: "flex", gap: "4px" }}>
                              <button className="btn btn-success btn-sm"
                                onClick={e => { e.stopPropagation(); setModalDecision({ open: true, solicitud: s, decision: "APROBAR", comentario: "" }); }}>
                                ✅ Aprobar
                              </button>
                              <button className="btn btn-danger btn-sm"
                                onClick={e => { e.stopPropagation(); setModalDecision({ open: true, solicitud: s, decision: "RECHAZAR", comentario: "" }); }}>
                                ❌ Rechazar
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* ── Tabla: Aprobadas ── */}
        <div style={{ marginTop: 24 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#22c55e", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#22c55e", display: "inline-block" }} />
            Solicitudes Aprobadas ({solAprobadas.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? (
                <Spinner text="Cargando solicitudes aprobadas..." />
              ) : solAprobadas.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <p>No hay solicitudes aprobadas todavía</p>
                </div>
              ) : (
                <table className="table-gerencia-permisos">
                  <thead>
                    <tr><th>#</th><th>Empleado</th><th>Tipo</th><th>Inicio</th><th>Fin</th><th>Días</th><th>Respuesta Gerencia</th></tr>
                  </thead>
                  <tbody>
                    {solAprobadas.map((s, idx) => {
                      const id = s.idSolicitud;
                      const isSelected = solicitudActiva?.idSolicitud === id;
                      return (
                        <tr key={id}
                          onClick={() => setSolicitudActiva(isSelected ? null : s)}
                          style={{ cursor: "pointer", background: isSelected ? "#eff6ff" : undefined, outline: isSelected ? "2px solid #3b82f6" : undefined }}>
                          <td style={{ color: "var(--text-3)", fontSize: 13 }}>{idx + 1}</td>
                          <td>{s.nombreEmpleado ?? s.idEmpleado}</td>
                          <td>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td>{s.fechaInicio ?? "—"}</td>
                          <td>{s.fechaFin ?? "—"}</td>
                          <td>{s.diasSolicitados ?? "—"}</td>
                          <td style={{ color: "#16a34a" }}>{s.respuesta ?? "—"}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* ── Tabla: Rechazadas ── */}
        <div style={{ marginTop: 24 }}>
          <h3 style={{ margin: "0 0 10px", fontSize: 15, color: "#ef4444", display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ width: 10, height: 10, borderRadius: "50%", background: "#ef4444", display: "inline-block" }} />
            Solicitudes Rechazadas ({solRechazadas.length})
          </h3>
          <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
            <div className="table-wrapper" style={{ marginBottom: 0 }}>
              {loading ? (
                <Spinner text="Cargando solicitudes rechazadas..." />
              ) : solRechazadas.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <p>No hay solicitudes rechazadas</p>
                </div>
              ) : (
                <table className="table-gerencia-permisos">
                  <thead>
                    <tr><th>#</th><th>Empleado</th><th>Tipo</th><th>Inicio</th><th>Fin</th><th>Días</th><th>Motivo del Rechazo</th></tr>
                  </thead>
                  <tbody>
                    {solRechazadas.map((s, idx) => {
                      const id = s.idSolicitud;
                      const isSelected = solicitudActiva?.idSolicitud === id;
                      return (
                        <tr key={id}
                          onClick={() => setSolicitudActiva(isSelected ? null : s)}
                          style={{ cursor: "pointer", background: isSelected ? "#fff1f2" : undefined, outline: isSelected ? "2px solid #ef4444" : undefined }}>
                          <td style={{ color: "var(--text-3)", fontSize: 13 }}>{idx + 1}</td>
                          <td>{s.nombreEmpleado ?? s.idEmpleado}</td>
                          <td>{TIPO_LABELS[s.tipoSolicitud] ?? s.tipoSolicitud}</td>
                          <td>{s.fechaInicio ?? "—"}</td>
                          <td>{s.fechaFin ?? "—"}</td>
                          <td>{s.diasSolicitados ?? "—"}</td>
                          <td style={{ color: "#dc2626" }}>{s.respuesta ?? "—"}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </main>

      {modalDecision.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>{modalDecision.decision === "APROBAR" ? "✅ Aprobar" : "❌ Rechazar"} Solicitud</h3>
                <p>#{modalDecision.solicitud?.idSolicitud} — {modalDecision.solicitud?.nombreEmpleado ?? modalDecision.solicitud?.idEmpleado}</p>
              </div>
              <button className="modal-close" onClick={() => setModalDecision({ ...modalDecision, open: false })}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>
                  {modalDecision.decision === "APROBAR"
                    ? "Comentario para el empleado (opcional)"
                    : "Motivo del rechazo *"}
                </label>
                <textarea rows={3} value={modalDecision.comentario}
                  onChange={e => setModalDecision({ ...modalDecision, comentario: e.target.value })}
                  placeholder={modalDecision.decision === "APROBAR"
                    ? "Mensaje adicional para el empleado (opcional)..."
                    : "Indique el motivo del rechazo..."} autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDecision({ ...modalDecision, open: false })}>Cancelar</button>
              <button
                className={modalDecision.decision === "APROBAR" ? "btn btn-success" : "btn btn-danger"}
                onClick={handleDecidir}
                disabled={(modalDecision.decision === "RECHAZAR" && !modalDecision.comentario.trim()) || procesando}>
                {procesando ? "Procesando..." : (modalDecision.decision === "APROBAR" ? "✅ Confirmar Aprobación" : "❌ Confirmar Rechazo")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════
   ROUTER
══════════════════════════════════════════════════════════ */
export default function SolicitudPermisos() {
  const location = useLocation();
  const path = location.pathname;
  if (path.startsWith("/gerencia")) return <VistaGerencia />;
  if (path.startsWith("/rrhh"))     return <VistaRRHH />;
  return <VistaEmpleado />;
}
