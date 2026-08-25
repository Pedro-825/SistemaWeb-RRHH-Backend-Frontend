import { useState, useEffect, useCallback } from "react";
import { useLocation } from "react-router-dom";

const BASE = import.meta.env.VITE_API_URL || '';
const resolveUrl = (url) => url?.startsWith('/') ? `${BASE}${url}` : (url || '');
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import ApiAlert from "../components/ApiAlert";
import "../styles/Dashboard.css";
import {
  listarJustificacionesPendientes,
  listarCorreccionesRegistradas,
  obtenerPlanRecuperacion,
  listarJustificacionesEmpleado,
  listarMisJustificaciones,
  listarAsistenciaEmpleado,
  listarMiAsistencia,
  getAsistenciaHoy,
  getMiAsistenciaHoy,
  getHorarioEmpleado,
  getMiHorario,
  registrarManual,
  registrarJustificacion,
  uploadEvidencia,
  revisarJustificacion,
  listarEmpleados,
  getIdEmpleado,
  getMe,
  getUser,
  saveSession,
} from "../services/api";

function getRolFromPath(pathname) {
  if (pathname.startsWith("/empleado")) return "EMPLEADO";
  if (pathname.startsWith("/gerencia")) return "GERENCIA";
  return "RRHH";
}

function Reloj() {
  const [hora, setHora] = useState(new Date());
  useEffect(() => {
    const t = setInterval(() => setHora(new Date()), 1000);
    return () => clearInterval(t);
  }, []);
  return (
    <div className="asist-clock-display">
      {hora.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false })}
    </div>
  );
}

function esRegistroJustificable(r) {
  const estado = String(r?.estado ?? "").toUpperCase();
  return Boolean(r?.idRegistro) && (estado === "TARDANZA" || estado === "INASISTENCIA" || Number(r?.minutosTardanza ?? 0) > 0);
}

function tipoIncidencia(registro) {
  return String(registro?.tipoIncidencia ?? registro?.estado ?? "").toUpperCase() === "INASISTENCIA" ? "Inasistencia" : "Tardanza";
}

function estaDentroDelPlazo(fecha) {
  if (!fecha) return false;
  const incidencia = new Date(`${fecha}T00:00:00`);
  if (Number.isNaN(incidencia.getTime())) return false;
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const diffDias = Math.floor((hoy - incidencia) / 86400000);
  return diffDias >= 0 && diffDias <= 1;
}

function tieneJustificacionActiva(registro, justificaciones) {
  return justificaciones.some(j =>
    Number(j.idRegistroAsistencia) === Number(registro.idRegistro) &&
    String(j.estado ?? "PENDIENTE").toUpperCase() !== "RECHAZADA"
  );
}

function registrosPendientesDeJustificar(historial, justificaciones) {
  return historial.filter(r =>
    esRegistroJustificable(r) &&
    estaDentroDelPlazo(r.fecha) &&
    !tieneJustificacionActiva(r, justificaciones)
  );
}

function fechaHoyPeru() {
  const partes = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Lima",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date());
  const get = (type) => partes.find(p => p.type === type)?.value;
  return `${get("year")}-${get("month")}-${get("day")}`;
}

function registroDeHoyDesdeHistorial(registros) {
  const hoy = fechaHoyPeru();
  return (Array.isArray(registros) ? registros : []).find(r => r?.fecha === hoy) ?? null;
}

const ASISTENCIA_CACHE_TTL_MS = 60000;

function leerCacheAsistencia(idEmpleado) {
  if (!idEmpleado) return null;
  try {
    const raw = sessionStorage.getItem(`empleado_asistencia_${idEmpleado}`);
    if (!raw) return null;
    const data = JSON.parse(raw);
    return data && Date.now() - data.ts < ASISTENCIA_CACHE_TTL_MS ? data : null;
  } catch {
    return null;
  }
}

function guardarCacheAsistencia(idEmpleado, patch) {
  if (!idEmpleado) return;
  try {
    const previo = JSON.parse(sessionStorage.getItem(`empleado_asistencia_${idEmpleado}`) || "{}");
    sessionStorage.setItem(`empleado_asistencia_${idEmpleado}`, JSON.stringify({ ...previo, ...patch, ts: Date.now() }));
  } catch { /* silencioso */ }
}

/* ----------------------------------------------------------
   LIGHTBOX — foto a pantalla completa
---------------------------------------------------------- */
function FotoExpandida({ src, onClose }) {
  useEffect(() => {
    const onKey = (e) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <div
      style={{
        position: "fixed", inset: 0, background: "rgba(0,0,0,0.72)",
        zIndex: 100000, display: "flex", alignItems: "center", justifyContent: "center",
        padding: 24,
      }}
      onClick={onClose}
    >
      <div
        onClick={e => e.stopPropagation()}
        style={{
          background: "#1e1e2e", borderRadius: 14, padding: 12,
          boxShadow: "0 20px 60px rgba(0,0,0,0.5)",
          maxWidth: 620, width: "100%", position: "relative",
        }}
      >
        <button
          onClick={onClose}
          style={{
            position: "absolute", top: 8, right: 10, background: "rgba(255,255,255,0.12)",
            border: "none", color: "#fff", fontSize: 20, cursor: "pointer",
            borderRadius: "50%", width: 32, height: 32, lineHeight: "32px", textAlign: "center",
          }}
          title="Cerrar (Esc)"
        >×</button>
        <img
          src={src}
          alt="Evidencia ampliada"
          style={{ width: "100%", maxHeight: 480, objectFit: "contain", borderRadius: 8, display: "block" }}
        />
      </div>
    </div>
  );
}

/* ----------------------------------------------------------
   MODAL DETALLE JUSTIFICACIÓN (Empleado / Gerencia)
---------------------------------------------------------- */
function DetalleJustificacionModal({ justificacion: j, onClose }) {
  const [fotoExpandida, setFotoExpandida] = useState(null);
  const evidenciaUrl = resolveUrl(j.evidenciaUrl ?? j.evidencia ?? "");
  const esImagen = /\.(jpg|jpeg|png|gif|webp|bmp)(\?|$)/i.test(evidenciaUrl);

  return (
    <>
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-box" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>

          <div className="modal-header">
            <div>
              <h3>Detalle de Justificación</h3>
              <p style={{ margin: 0, fontSize: 12, color: "var(--text-3)" }}>
                {j.fechaJustificacion?.substring(0, 10) ?? j.fechaAsistencia ?? "—"}
              </p>
            </div>
            <button className="modal-close" onClick={onClose}>×</button>
          </div>

          <div className="modal-body">
            {/* Estado */}
            <div style={{ marginBottom: 14, display: "flex", alignItems: "center", gap: 10 }}>
              <span style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase" }}>Estado</span>
              <span className={`badge ${
                j.estado === "APROBADA"  ? "badge-success" :
                j.estado === "RECHAZADA" ? "badge-danger"  :
                "badge-warning"
              }`}>{j.estado ?? "PENDIENTE"}</span>
            </div>

            {/* Motivo */}
            <div style={{ marginBottom: 14 }}>
              <div style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 6 }}>
                Motivo
              </div>
              <div style={{
                background: "var(--bg-2, #f8fafc)", border: "1px solid var(--border)",
                borderRadius: 8, padding: "12px 14px", fontSize: 13,
                color: "var(--text-1)", whiteSpace: "pre-wrap", lineHeight: 1.6, minHeight: 48,
              }}>
                {j.motivo || <span style={{ color: "var(--text-3)" }}>Sin motivo registrado.</span>}
              </div>
            </div>

            {/* Observación RRHH */}
            {j.comentarioRevision && (
              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 6 }}>
                  Observación de RRHH
                </div>
                <div style={{
                  background: "#fefce8", border: "1px solid #fde047",
                  borderRadius: 8, padding: "10px 14px", fontSize: 13, color: "#713f12",
                }}>
                  {j.comentarioRevision}
                </div>
              </div>
            )}

            {/* Evidencia */}
            <div>
              <div style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 8 }}>
                Evidencia adjunta
              </div>
              {evidenciaUrl ? (
                esImagen ? (
                  <div className="evidencia-foto-container" onClick={() => setFotoExpandida(evidenciaUrl)} title="Click para ampliar">
                    <img
                      src={evidenciaUrl}
                      alt="Evidencia"
                      className="evidencia-foto-img"
                    />
                    <div className="evidencia-foto-overlay">Click para ampliar</div>
                  </div>
                ) : (
                  <a href={evidenciaUrl} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm">
                    Ver evidencia adjunta
                  </a>
                )
              ) : (
                <div style={{
                  border: "2px dashed var(--border)", borderRadius: 8, padding: "24px",
                  textAlign: "center", color: "var(--text-3)", fontSize: 13,
                }}>
                  <div style={{ fontSize: 32, marginBottom: 6 }}>Sin archivo</div>
                  Sin evidencia adjunta
                </div>
              )}
            </div>
          </div>

          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onClose}>Cerrar</button>
          </div>
        </div>
      </div>

      {fotoExpandida && <FotoExpandida src={fotoExpandida} onClose={() => setFotoExpandida(null)} />}
    </>
  );
}

/* ----------------------------------------------------------
   VISTA EMPLEADO
---------------------------------------------------------- */
function VistaEmpleado() {
  const [idEmpleadoSesion, setIdEmpleadoSesion] = useState(getIdEmpleado());
  const idEmpleado = idEmpleadoSesion;

  const [justificaciones, setJustificaciones] = useState([]);
  const [asistenciaHoy,   setAsistenciaHoy]   = useState(null);
  const [historial,       setHistorial]        = useState([]);
  const [horario,         setHorario]          = useState({ horaEntrada: "08:00", horaSalida: "17:00" });
  const [loading, setLoading]   = useState(false);
  const [mensaje, setMensaje]   = useState("");
  const [error,   setError]     = useState("");
  const [tabActiva, setTabActiva] = useState("info");

  // Registro manual modal
  const [modalManual, setModalManual] = useState({ open: false, tipo: "ENTRADA" });
  const [manualMotivo, setManualMotivo] = useState("");
  const [saving, setSaving] = useState(false);

  // ECU-03 A4: justificación de tardanza
  const [modalJustificar, setModalJustificar] = useState({ open: false, justificacion: null });
  const [justifMotivo,       setJustifMotivo]       = useState("");
  const [justifFile,         setJustifFile]         = useState(null);
  const [justifEvidenciaUrl, setJustifEvidenciaUrl] = useState("");
  const [justifSaving,       setJustifSaving]       = useState(false);
  const [evidenciaNombre,    setEvidenciaNombre]    = useState("");

  // Detalle de justificación (ver evidencia)
  const [modalDetalle, setModalDetalle] = useState(null);

  const cerrarModalJustif = () => {
    setModalJustificar({ open: false, justificacion: null });
    setJustifMotivo("");
    setJustifFile(null);
    setJustifEvidenciaUrl("");
  };

  useEffect(() => {
    if (idEmpleadoSesion) return;
    let activo = true;
    getMe()
      .then(data => {
        if (!activo || !data?.idEmpleado) return;
        const actual = getUser();
        saveSession(null, data.rol, data.username, data.idEmpleado, actual.requiereCambioPassword ?? false);
        setIdEmpleadoSesion(data.idEmpleado);
      })
      .catch(() => {
        if (activo) setError("No se pudo identificar al empleado de la sesion.");
      });
    return () => { activo = false; };
  }, [idEmpleadoSesion]);

  useEffect(() => {
    const cached = leerCacheAsistencia(idEmpleado);
    if (cached) {
      if ("justificaciones" in cached) setJustificaciones(cached.justificaciones);
      if ("asistenciaHoy" in cached) setAsistenciaHoy(cached.asistenciaHoy);
      if ("historial" in cached) setHistorial(cached.historial);
      if ("horario" in cached) setHorario(cached.horario);
    }
    cargarJustificaciones();
    cargarAsistenciaHoy();
    cargarHistorial();
    cargarHorario();
    const refrescar = () => {
      const cache = leerCacheAsistencia(idEmpleado);
      if (cache && Date.now() - cache.ts < 30000) return;
      cargarAsistenciaHoy();
      cargarHistorial();
    };
    window.addEventListener("focus", refrescar);
    document.addEventListener("visibilitychange", refrescar);
    return () => {
      window.removeEventListener("focus", refrescar);
      document.removeEventListener("visibilitychange", refrescar);
    };
  }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  async function cargarHorario() {
    try {
      const data = await getMiHorario();
      if (data) {
        setHorario(data);
        guardarCacheAsistencia(idEmpleado, { horario: data });
      }
    } catch { /* silencioso */ }
  }

  async function cargarJustificaciones() {
    try {
      const data = await listarMisJustificaciones();
      const lista = Array.isArray(data) ? data : [];
      setJustificaciones(lista);
      guardarCacheAsistencia(idEmpleado, { justificaciones: lista });
    } catch { /* silencioso */ }
  }

  async function cargarAsistenciaHoy() {
    try {
      const data = await getMiAsistenciaHoy();
      if (data) {
        setAsistenciaHoy(data);
        guardarCacheAsistencia(idEmpleado, { asistenciaHoy: data });
        return;
      }
      const registros = await listarMiAsistencia();
      const hoy = registroDeHoyDesdeHistorial(registros);
      setAsistenciaHoy(hoy);
      guardarCacheAsistencia(idEmpleado, { asistenciaHoy: hoy });
    } catch { /* silencioso */ }
  }

  async function cargarHistorial() {
    if (historial.length === 0) setLoading(true);
    try {
      const data = await listarMiAsistencia();
      const lista = Array.isArray(data) ? data : [];
      setHistorial(lista);
      guardarCacheAsistencia(idEmpleado, { historial: lista });
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  // ECU-03 A4: empleado envía justificación de tardanza
  const handleEnviarJustificacion = async () => {
    if (!justifMotivo.trim()) return;
    setJustifSaving(true);
    setMensaje("");
    try {
      const j = modalJustificar.justificacion;
      let evidenciaUrl = justifEvidenciaUrl;
      if (justifFile && !evidenciaUrl) {
        const res = await uploadEvidencia(justifFile);
        evidenciaUrl = res.url ?? "";
      }
      await registrarJustificacion({
        idEmpleado,
        idRegistroAsistencia: j.idRegistroAsistencia,
        motivo: justifMotivo,
        evidenciaUrl,
      });
      setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
      cerrarModalJustif();
      cargarJustificaciones();
    } catch (err) {
      setError(err);
      cerrarModalJustif();
    } finally {
      setJustifSaving(false);
    }
  };

  const handleRegistrarManual = async () => {
    if (!manualMotivo.trim()) return;
    if (!idEmpleado) {
      setError("No se encontró su ID de empleado. Contacte a RRHH.");
      setModalManual({ open: false, tipo: "ENTRADA" });
      return;
    }
    setSaving(true);
    try {
      const now = new Date();
      const fechaHora = now.toISOString().substring(0, 19);
      await registrarManual({
        idEmpleado,
        tipo: modalManual.tipo,
        fechaHora,
        motivo: manualMotivo,
      });
      setMensaje(`${modalManual.tipo === "ENTRADA" ? "Entrada" : "Salida"} registrada correctamente.`);
      setModalManual({ open: false, tipo: "ENTRADA" });
      setManualMotivo("");
    } catch (err) {
      setError(err);
      setModalManual({ open: false, tipo: "ENTRADA" });
    } finally {
      setSaving(false);
    }
  };

  const fechaCorta = new Date().toLocaleDateString("es-PE", { day: "2-digit", month: "2-digit", year: "numeric" });
  const registrosJustificables = registrosPendientesDeJustificar(historial, justificaciones);
  const registroJustificableHoy = esRegistroJustificable(asistenciaHoy) &&
    estaDentroDelPlazo(asistenciaHoy?.fecha) &&
    !tieneJustificacionActiva(asistenciaHoy, justificaciones)
      ? asistenciaHoy
      : null;
  const registrosParaJustificar = registrosJustificables.length > 0
    ? registrosJustificables
    : (registroJustificableHoy ? [registroJustificableHoy] : []);
  const puedeJustificar = registrosParaJustificar.length > 0;

  const abrirJustificacionRapida = () => {
    if (!puedeJustificar) return;
    setTabActiva("nueva-justif");
    setTimeout(() => {
      document.getElementById("form-justificacion-tardanza")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 0);
  };

  const estadoBadge = (estado) => {
    const m = {
      PENDIENTE: "badge-warning",
      APROBADO:  "badge-success",
      RECHAZADO: "badge-danger",
    };
    return m[estado] ?? "badge-gray";
  };

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Asistencia / Control de Asistencia</span>
            <h1>Control de Asistencia</h1>
          </div>
          <div className="header-right">
            <span className="role-pill role-pill-empleado">Empleado</span>
          </div>
        </header>

        <ApiAlert type="success" message={mensaje} style={{ marginBottom: 16 }} />
        <ApiAlert type="error" message={error} style={{ marginBottom: 16 }} />

        {/* -- FILA SUPERIOR: Registro manual | Asistencia Hoy -- */}
        <div className="asist-grid">

          {/* IZQUIERDA: Reloj y estado */}
          <div className="section-card" style={{ textAlign: "center", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
            <div style={{ fontSize: 12, fontWeight: 600, color: "var(--text-3)", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 6 }}>
              Hora actual
            </div>
            <Reloj />

            <div style={{ margin: "14px 0" }}>
              {asistenciaHoy ? (
                <span className={`badge ${
                  asistenciaHoy.estado === "PUNTUAL"     ? "badge-success" :
                  asistenciaHoy.estado === "TARDANZA"    ? "badge-warning" :
                  asistenciaHoy.estado === "JUSTIFICADA" ? "badge-justificada" :
                  asistenciaHoy.estado === "INASISTENCIA" ? "badge-danger" :
                  "badge-gray"
                }`} style={{ fontSize: 13, padding: "6px 18px" }}>
                  {asistenciaHoy.estado}
                </span>
              ) : (
                <span className="badge badge-gray" style={{ fontSize: 13, padding: "6px 18px" }}>
                  Sin registro hoy
                </span>
        )}
            </div>


            <div className="asist-horario-tag">
              Horario establecido: {horario.horaEntrada} a.m. - {horario.horaSalida} p.m.
            </div>
          </div>

          {/* DERECHA: Mi Asistencia Hoy */}
          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>Mi Asistencia Hoy</h2>
              <span style={{ fontSize: "12px", color: "var(--text-3)" }}>Fecha: {fechaCorta}</span>
            </div>

            <div className="asist-today-grid">
              <div className="asist-today-item">
                <label>Entrada</label>
                <div className="asist-today-val">
                  {asistenciaHoy?.horaEntrada
                    ? asistenciaHoy.horaEntrada.substring(0, 5)
                    : "—:—"}
                </div>
                <span className={`badge ${asistenciaHoy?.horaEntrada ? "badge-success" : "badge-gray"}`}
                  style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaEntrada ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Salida</label>
                <div className="asist-today-val" style={{ color: asistenciaHoy?.horaSalida ? "var(--text)" : "var(--text-3)" }}>
                  {asistenciaHoy?.horaSalida
                    ? asistenciaHoy.horaSalida.substring(0, 5)
                    : "—:—"}
                </div>
                <span className={`badge ${asistenciaHoy?.horaSalida ? "badge-success" : "badge-gray"}`}
                  style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaSalida ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Horas trabajadas</label>
                <span className="badge badge-info" style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.horasTrabajadas != null
                    ? `${asistenciaHoy.horasTrabajadas} h`
                    : asistenciaHoy?.horaEntrada ? "En curso" : "—"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Estado del día</label>
                <span className={`badge ${
                  asistenciaHoy?.estado === "PUNTUAL"      ? "badge-success" :
                  asistenciaHoy?.estado === "JUSTIFICADA"  ? "badge-justificada" :
                  asistenciaHoy?.estado === "TARDANZA"     ? "badge-warning" :
                  asistenciaHoy?.estado === "INASISTENCIA" ? "badge-danger" : "badge-gray"
                }`} style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.estado ?? "Sin registro"}
                </span>
              </div>
            </div>

            <div className="alert alert-warning" style={{ marginTop: 8, marginBottom: 0 }}>
              <strong>Importante:</strong> Si llegas después del horario establecido, deberás justificar tu tardanza.
              {puedeJustificar && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={abrirJustificacionRapida}
                  style={{ marginLeft: 12 }}
                >
                  Justificar incidencia
                </button>
              )}
            </div>
          </div>
        </div>

        {/* -- TABS -- */}
        <div className="tab-bar">
          <button className={`tab ${tabActiva === "justificaciones" ? "active" : ""}`}
            onClick={() => { setTabActiva("justificaciones"); if (idEmpleado) cargarJustificaciones(); }}>
            Mis Justificaciones
          </button>
          <button className={`tab ${tabActiva === "nueva-justif" ? "active" : ""}`}
            onClick={() => { if (!puedeJustificar) return; setTabActiva("nueva-justif"); }}
            disabled={!puedeJustificar}
            title=""
            style={!puedeJustificar ? { opacity: 0.5, cursor: "not-allowed" } : {}}>
            Nueva Justificación
          </button>
          <button className={`tab ${tabActiva === "info" ? "active" : ""}`}
            onClick={() => setTabActiva("info")}>
            Información
          </button>
        </div>

        {/* -- JUSTIFICACIONES -- */}
        {tabActiva === "justificaciones" && (
          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>
                Mis Justificaciones (Inasistencias o Tardanzas)
              </h2>
              <span style={{ fontSize: "12px", color: "var(--text-2)" }}>
                Aquí puedes registrar y dar seguimiento a tus justificaciones.
              </span>
            </div>
            <div className="alert alert-warning">
              Solo tienes <strong>1 día</strong> desde la incidencia para registrar tu justificación.
            </div>
            <div className="table-wrapper">
              {loading ? (
                <Spinner />
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Tipo</th>
                      <th>Motivo</th>
                      <th>Evidencia</th>
                      <th>Estado</th>
                      <th>Observación</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr>
                        <td colSpan={7}>
                          <div className="empty-state">
                            <div className="empty-state-icon">Sin datos</div>
                            <p>No tienes justificaciones pendientes</p>
                          </div>
                        </td>
                      </tr>
                    ) : justificaciones.map((j) => (
                      <tr
                        key={j.idJustificacion ?? j.id}
                        onClick={() => setModalDetalle(j)}
                        style={{ cursor: "pointer" }}
                        title="Click para ver detalle y evidencia"
                      >
                        <td>{j.fechaJustificacion?.substring(0, 10) ?? "—"}</td>
                        <td>{tipoIncidencia(j)}</td>
                        <td>{j.motivo}</td>
                        <td>
                          {j.evidenciaUrl
                            ? <span style={{ color: "#2563eb", fontWeight: 600 }}>Ver foto</span>
                            : <span style={{ color: "var(--text-3)" }}>-</span>}
                        </td>
                        <td>
                          <span className={`badge ${estadoBadge(j.estado)}`}>
                            {j.estado ?? "Pendiente"}
                          </span>
                        </td>
                        <td>{j.comentarioRevision ?? <span style={{ color: "var(--text-3)" }}>-</span>}</td>
                        <td>
                          {(!j.estado || j.estado === "PENDIENTE") ? (
                            <button className="btn btn-warning btn-sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                setModalJustificar({ open: true, justificacion: j });
                                setJustifMotivo(j.motivo ?? "");
                              }}>
                              Editar
                            </button>
                          ) : (
                            <span className="badge badge-gray" style={{ fontSize: "11px" }}>
                              {j.estado === "APROBADA" ? "Aprobado" : "Rechazado"}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* -- NUEVA JUSTIFICACIÓN (ECU-03 A4) -- */}
        {tabActiva === "nueva-justif" && (
          puedeJustificar ? (
            <div className="section-card">
              <h2>Registrar Nueva Justificación</h2>
            <div className="alert alert-warning">
              Solo puedes justificar incidencias del <strong>día anterior o del día actual</strong>. Pasado 1 día, no será posible registrar la justificación.
            </div>
            <form id="form-justificacion-tardanza" onSubmit={async (e) => {
              e.preventDefault();
              const fd = new FormData(e.target);
              const idRegistroSeleccionado = fd.get("idRegistroAsistencia");
              const registro = registrosParaJustificar.find(r => String(r.idRegistro) === String(idRegistroSeleccionado));
              if (!registro) {
                setError("Selecciona una incidencia pendiente de justificar.");
                return;
              }
              setJustifSaving(true); setMensaje(""); setError("");
              try {
                const evidenciaFile = fd.get("evidencia");
                let evidenciaUrl = "";
                if (evidenciaFile && evidenciaFile.size > 0) {
                  const res = await uploadEvidencia(evidenciaFile);
                  evidenciaUrl = res.url ?? "";
                }
                await registrarJustificacion({
                  idRegistroAsistencia: registro.idRegistro,
                  idEmpleado,
                  motivo: fd.get("motivo"),
                  evidenciaUrl,
                });
                setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
                e.target.reset();
                setEvidenciaNombre("");
                setTabActiva("justificaciones");
                cargarJustificaciones();
              } catch (err) {
                setError(err);
              } finally { setJustifSaving(false); }
            }}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Incidencia a justificar *</label>
                  <select name="idRegistroAsistencia" required defaultValue={registrosParaJustificar[0]?.idRegistro ?? ""}>
                    {registrosParaJustificar.map(r => (
                      <option key={r.idRegistro} value={r.idRegistro}>
                        {r.fecha} - {tipoIncidencia(r)}{tipoIncidencia(r) === "Tardanza" ? ` (${r.minutosTardanza ?? 0} min)` : ""}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group form-full">
                  <label>Motivo de la justificación *</label>
                  <textarea name="motivo" rows={4}
                    placeholder="Describa detalladamente el motivo de su tardanza o inasistencia..."
                    required />
                </div>
                <div className="form-group form-full">
                  <label>Evidencia fotográfica (opcional)</label>
                  <div className="file-picker">
                    <label className="file-picker-btn">
                      Elegir archivo
                      <input
                        type="file"
                        name="evidencia"
                        accept="image/*"
                        onChange={e => setEvidenciaNombre(e.target.files?.[0]?.name ?? "")}
                      />
                    </label>
                    <span className={`file-picker-name ${evidenciaNombre ? "" : "is-empty"}`}>
                      {evidenciaNombre || "No se eligió ningún archivo"}
                    </span>
                  </div>
                  <p className="file-picker-help">
                    Puedes tomar una foto desde el celular o elegir una imagen desde tu equipo.
                  </p>
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={justifSaving}>
                  {justifSaving ? <><span className="btn-spinner" />Enviando...</> : "Enviar Justificación"}
                </button>
                <button type="reset" className="btn btn-secondary" onClick={() => setEvidenciaNombre("")}>Limpiar</button>
              </div>
            </form>
          </div>
        ) : (
          <div className="section-card">
            <div className="alert alert-info">
              La opción se habilita cuando tienes una tardanza o inasistencia de hoy o ayer pendiente de justificar.
            </div>
          </div>
        ))}
        {/* -- INFORMACIÓN -- */}
        {tabActiva === "info" && (
          <div className="section-card">
            <h2>¿Cómo funciona el control de asistencia?</h2>
            <div className="alert alert-info">
              <strong>Registro de entrada:</strong> Coloca tu huella dactilar en el dispositivo biométrico al llegar. El sistema registrará tu hora de entrada automáticamente.
            </div>
            <div className="alert alert-info">
              <strong>Registro de salida:</strong> Al terminar tu jornada, vuelve a colocar tu huella. El sistema calculará automáticamente las horas trabajadas.
            </div>
            <div className="alert alert-warning">
              <strong>Tardanzas:</strong> Si llegas después del horario establecido (más allá del margen de tolerancia configurado), se registrará una incidencia de tardanza. Dispondrás de 1 día para justificarla.
            </div>
            <div className="alert alert-warning">
              <strong>Correcciones:</strong> Si hay un error en tu registro de asistencia, usa el formulario de registro manual o solicita una corrección a RRHH.
            </div>
          </div>
        )}

        <div className="asistencia-section-divider" aria-hidden="true" />

        {/* -- HISTORIAL -- */}
        <div className="section-card">
          <div className="section-header">
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>Historial de Asistencia</h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarHistorial}>Actualizar</button>
          </div>
          <div className="table-wrapper">
            {loading ? <Spinner /> : (
              <table>
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Entrada</th>
                    <th>Salida</th>
                    <th>Horas</th>
                    <th>Tardanza</th>
                    <th>Estado</th>
                    <th>Tipo registro</th>
                  </tr>
                </thead>
                <tbody>
                  {historial.length === 0 ? (
                    <tr><td colSpan={7}>
                      <div className="empty-state">
                        <div className="empty-state-icon">Sin datos</div>
                        <p>No hay registros de asistencia aún</p>
                      </div>
                    </td></tr>
                  ) : historial.map(r => (
                    <tr key={r.idRegistro}>
                      <td>{r.fecha}</td>
                      <td>{r.horaEntrada ? r.horaEntrada.substring(0, 5) : "—"}</td>
                      <td>{r.horaSalida  ? r.horaSalida.substring(0, 5)  : "—"}</td>
                      <td>{r.horasTrabajadas != null ? `${r.horasTrabajadas} h` : "—"}</td>
                      <td>{r.minutosTardanza > 0 ? `${r.minutosTardanza} min` : "—"}</td>
                      <td>
                        <span className={`badge ${r.estado === "PUNTUAL" ? "badge-success" : r.estado === "JUSTIFICADA" ? "badge-justificada" : r.estado === "TARDANZA" ? "badge-warning" : r.estado === "INASISTENCIA" ? "badge-danger" : "badge-gray"}`}>
                          {r.estado ?? "—"}
                        </span>
                      </td>
                      <td style={{ fontSize: "12px", color: "var(--text-3)" }}>
                        {r.tipoUltimoRegistro ?? "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

      </main>

      {/* -- MODAL: JUSTIFICACIÓN DE TARDANZA (ECU-03 A4) -- */}
      {modalJustificar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>Registrar Justificación de Tardanza</h3>
                <p>Fecha: {modalJustificar.justificacion?.fechaJustificacion?.substring(0, 10) ?? "—"}</p>
              </div>
              <button className="modal-close" onClick={cerrarModalJustif}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Motivo de la tardanza *</label>
                <textarea
                  rows={4}
                  value={justifMotivo}
                  onChange={e => setJustifMotivo(e.target.value)}
                  placeholder="Describa el motivo de su tardanza (tráfico, emergencia, problema de transporte, etc.)..."
                  autoFocus
                />
              </div>
              <div className="form-group">
                <label>Evidencia (opcional)</label>
                {justifEvidenciaUrl ? (
                  <div className="galeria-evidencia-preview">
                    <img src={justifEvidenciaUrl} alt="Evidencia seleccionada" className="galeria-preview-img" />
                    <button className="btn btn-secondary btn-sm" style={{ marginTop: 6 }}
                      onClick={() => setJustifEvidenciaUrl("")}>
                      × Quitar foto
                    </button>
                  </div>
                ) : (
                  <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <input
                      type="file"
                      accept="image/*,application/pdf"
                      onChange={e => { setJustifFile(e.target.files[0] ?? null); setJustifEvidenciaUrl(""); }}
                      style={{ flex: 1, fontSize: 13 }}
                    />
                  </div>
                )}
                {justifFile && !justifEvidenciaUrl && (
                  <div style={{ fontSize: 11, color: "#16a34a", marginTop: 4 }}>
                    {justifFile.name}
                  </div>
                )}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={cerrarModalJustif}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleEnviarJustificacion}
                disabled={!justifMotivo.trim() || justifSaving}>
                {justifSaving ? "Enviando..." : "Enviar Justificación"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* -- MODAL REGISTRO MANUAL -- */}
      {modalManual.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>
                  {modalManual.tipo === "ENTRADA"
                    ? "Registrar Entrada (Asistencia Manual)"
                    : "Registrar Salida (Asistencia Manual)"}
                </h3>
                <p>Inicio de jornada — {new Date().toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit" })}</p>
              </div>
              <button className="modal-close" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 14 }}>
                Esta opción se usa solo cuando el dispositivo biométrico no esté disponible.
              </div>
              <div className="form-group">
                <label>Motivo del registro manual *</label>
                <textarea
                  rows={3}
                  value={manualMotivo}
                  onChange={e => setManualMotivo(e.target.value)}
                  placeholder="Ej: Dispositivo biométrico no disponible, problema técnico..."
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>Cancelar</button>
              <button
                className={modalManual.tipo === "ENTRADA" ? "btn btn-success" : "btn btn-warning"}
                onClick={handleRegistrarManual}
                disabled={!manualMotivo.trim() || saving}
              >
                {saving ? <><span className="btn-spinner" />Guardando...</> : (modalManual.tipo === "ENTRADA" ? "Confirmar Entrada" : "Confirmar Salida")}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* -- MODAL DETALLE JUSTIFICACIÓN -- */}
      {modalDetalle && (
        <DetalleJustificacionModal
          justificacion={modalDetalle}
          onClose={() => setModalDetalle(null)}
        />
      )}

    </div>
  );
}

/* ----------------------------------------------------------
   VISTA RRHH
---------------------------------------------------------- */
function VistaRRHH() {
  const [justificaciones, setJustificaciones] = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [mensaje,  setMensaje]  = useState("");
  const [error,    setError]    = useState("");
  const [correcciones, setCorrecciones] = useState([]);
  const [loadingCorrecciones, setLoadingCorrecciones] = useState(false);
  const [tabCorrecciones, setTabCorrecciones] = useState("TARDANZA");
  const [modalPlan, setModalPlan] = useState({ open: false, idJustificacion: null, nombreEmpleado: "", tipo: "", dias: [], loading: false, fetchedAtMs: 0 });
  const [tickPlan, setTickPlan] = useState(Date.now());
  const [modalRechazar,  setModalRechazar]  = useState({ open: false, id: null, comentario: "" });
  const [modalDecision,  setModalDecision]  = useState({ open: false, justificacion: null });
  const [justificacionSeleccionada, setJustificacionSeleccionada] = useState(null);
  const [fotoExpandida, setFotoExpandida] = useState(null);

  const [manualForm, setManualForm] = useState({
    idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "",
  });
  const [empleados,       setEmpleados]       = useState([]);
  const [busquedaNombre,  setBusquedaNombre]  = useState("");
  const [empleadoSelec,   setEmpleadoSelec]   = useState(null);
  const [correccionInfo,  setCorreccionInfo]  = useState({
    fechaCorreccion: "", horaCorregida: "", horaEntradaDisplay: "", minutosTardanza: 0,
    entradaProgramada: "08:00", salidaProgramada: "17:00", salidaPropuesta: "",
    horaEntradaReal: "", salidaReal: "",
    evidenciaUrl: "",
  });
  const resetCorreccion = () => setCorreccionInfo({
    fechaCorreccion: "", horaCorregida: "", horaEntradaDisplay: "", minutosTardanza: 0,
    entradaProgramada: "08:00", salidaProgramada: "17:00", salidaPropuesta: "",
    horaEntradaReal: "", salidaReal: "",
    evidenciaUrl: "",
  });

  useEffect(() => {
    cargarJustificaciones();
    cargarCorreccionesRegistradas();
    listarEmpleados(0, 100).then(d => setEmpleados(d.content ?? [])).catch(() => {});
  }, []);

  async function cargarCorreccionesRegistradas() {
    setLoadingCorrecciones(true);
    try {
      const data = await listarCorreccionesRegistradas();
      setCorrecciones(Array.isArray(data) ? data : []);
    } catch { /* silencioso, no bloquea el resto de la pantalla */ }
    finally {
      setLoadingCorrecciones(false);
    }
  }

  async function cargarPlanRecuperacion(idJustificacion) {
    try {
      const dias = await obtenerPlanRecuperacion(idJustificacion);
      setModalPlan(m => (m.idJustificacion === idJustificacion
        ? { ...m, dias: Array.isArray(dias) ? dias : [], loading: false, fetchedAtMs: Date.now() }
        : m));
    } catch {
      setModalPlan(m => (m.idJustificacion === idJustificacion ? { ...m, loading: false } : m));
    }
  }

  const abrirPlanRecuperacion = (c) => {
    setModalPlan({ open: true, idJustificacion: c.idJustificacion, nombreEmpleado: c.nombreEmpleado, tipo: c.tipo, dias: [], loading: true, fetchedAtMs: 0 });
    cargarPlanRecuperacion(c.idJustificacion);
  };

  // Mientras el modal esta abierto, el conteo avanza en tiempo real (cada
  // segundo) en el navegador a partir del ultimo dato traido del backend.
  // Al cerrarse, se deja de tickear (no tiene sentido calcular algo que no
  // se muestra); la proxima vez que se abra se vuelve a pedir al backend.
  useEffect(() => {
    if (!modalPlan.open) return;
    const t = setInterval(() => setTickPlan(Date.now()), 1000);
    return () => clearInterval(t);
  }, [modalPlan.open]);

  async function cargarJustificaciones() {
    setLoading(true);
    setError("");
    try {
      const data = await listarJustificacionesPendientes();
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  const seleccionarEmpleado = (emp) => {
    setEmpleadoSelec(emp);
    setBusquedaNombre(`${emp.nombres} ${emp.apellidos}`);
    setManualForm(f => ({ ...f, idEmpleado: emp.idEmpleado }));
  };

  const handleRegistrarManual = async (e) => {
    e.preventDefault();
    setMensaje("");
    setError("");
    if (!manualForm.idEmpleado) {
      setError("Seleccione un empleado.");
      return;
    }
    if (!correccionInfo.horaCorregida && !correccionInfo.salidaPropuesta) {
      setError("Seleccione una justificación de la tabla para completar el formulario.");
      return;
    }
    if (!correccionInfo.fechaCorreccion) {
      setError("Seleccione la fecha de corrección.");
      return;
    }
    try {
      if (correccionInfo.horaCorregida) {
        await registrarManual({
          idEmpleado: parseInt(manualForm.idEmpleado),
          tipo: "ENTRADA",
          fechaHora: correccionInfo.fechaCorreccion + "T" + correccionInfo.horaCorregida + ":00",
          motivo: manualForm.motivo || "Corrección de tardanza justificada",
          tipoRegistro: "CORRECCION",
          idJustificacion: justificacionSeleccionada,
        });
      }
      if (correccionInfo.salidaPropuesta) {
        await registrarManual({
          idEmpleado: parseInt(manualForm.idEmpleado),
          tipo: "SALIDA",
          fechaHora: correccionInfo.fechaCorreccion + "T" + correccionInfo.salidaPropuesta + ":00",
          motivo: manualForm.motivo || "Corrección de tardanza justificada",
          tipoRegistro: "CORRECCION",
          idJustificacion: justificacionSeleccionada,
        });
      }
      setMensaje("Registro manual guardado correctamente");
      setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" });
      // Quitar de la tabla la justificación ya corregida
      setJustificaciones(prev => prev.filter(jj =>
        (jj.id ?? jj.idJustificacion) !== justificacionSeleccionada
      ));
      setJustificacionSeleccionada(null);
      resetCorreccion();
      setBusquedaNombre("");
      setEmpleadoSelec(null);
    } catch (err) {
      setError(err);
    }
  };

  const handleDecidirAprobar = async () => {
    const j = modalDecision.justificacion;
    setModalDecision({ open: false, justificacion: null });
    setMensaje("");
    setError("");
    try {
      await revisarJustificacion(j.id ?? j.idJustificacion, "APROBADA", "");
      setMensaje("Justificación aprobada. Complete el Registro Manual y guárdelo.");
      // Mantener en tabla con estado APROBADA (verde) hasta que se guarde el registro manual
      setJustificaciones(prev => prev.map(jj =>
        (jj.id ?? jj.idJustificacion) === (j.id ?? j.idJustificacion)
          ? { ...jj, estado: "APROBADA" }
          : jj
      ));
      await handleCorregirTardanza(j);
      document.querySelector(".asistencia-manual")?.scrollIntoView({ behavior: "smooth" });
    } catch (err) {
      setError(err);
    }
  };

  const handleDecidirRechazar = () => {
    const j = modalDecision.justificacion;
    setModalDecision({ open: false, justificacion: null });
    setModalRechazar({ open: true, id: j.id ?? j.idJustificacion, comentario: "" });
  };

  const handleConfirmarRechazo = async () => {
    if (!modalRechazar.comentario.trim()) return;
    try {
      await revisarJustificacion(modalRechazar.id, "RECHAZADA", modalRechazar.comentario);
      setMensaje("Justificación rechazada correctamente");
      setModalRechazar({ open: false, id: null, comentario: "" });
      cargarJustificaciones();
    } catch (err) {
      setError(err);
      setModalRechazar({ open: false, id: null, comentario: "" });
    }
  };

  const handleCorregirTardanza = async (j) => {
    setJustificacionSeleccionada(j.id ?? j.idJustificacion);
    const emp = empleados.find(e => e.idEmpleado == j.idEmpleado);
    if (emp) seleccionarEmpleado(emp);

    let horEmp = { horaEntrada: "08:00", horaSalida: "17:00" };
    try {
      const h = await getHorarioEmpleado(j.idEmpleado);
      if (h?.horaEntrada && h?.horaSalida) horEmp = h;
    } catch { /* usa defaults */ }

    const entradaPartes = horEmp.horaEntrada.split(":");
    const entradaMins = parseInt(entradaPartes[0]) * 60 + parseInt(entradaPartes[1] ?? "0");
    const salidaPartes = horEmp.horaSalida.split(":");
    const salidaMinsBase = parseInt(salidaPartes[0]) * 60 + parseInt(salidaPartes[1] ?? "0");

    const parseFecha = (val) => {
      if (!val) return new Date().toISOString().slice(0, 10);
      // Array format [2026,6,26,...] from Jackson
      if (Array.isArray(val)) {
        const [y, mo, d] = val;
        return `${y}-${String(mo).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
      }
      const m = String(val).match(/^(\d{4})-(\d{2})-(\d{2})/);
      return m ? `${m[1]}-${m[2]}-${m[3]}` : new Date().toISOString().slice(0, 10);
    };
    const fechaStr = parseFecha(j.fechaAsistencia || j.fechaJustificacion || j.fechaRegistro);

    // Sin registro de asistencia vinculado (ej. justificación de prueba/histórica):
    // no hay ninguna hora real de llegada de la que partir, así que no se puede
    // calcular automáticamente. RRHH debe ingresar las horas manualmente.
    const sinRegistro = !j.idRegistroAsistencia || !j.horaEntrada;

    let horaCorregida = "";
    let horaEntradaDisplay = "";
    let horaEntradaReal = "";
    let minsTard = 0;
    let salidaReal = "";

    if (!sinRegistro) {
      const parts = String(j.horaEntrada).split(":");
      const hh = parseInt(parts[0] ?? entradaPartes[0]);
      const mm = parseInt(parts[1] ?? "0");
      horaEntradaReal = String(hh).padStart(2, "0") + ":" + String(mm).padStart(2, "0");
      horaEntradaDisplay = horaEntradaReal;
      horaCorregida = horaEntradaReal;
      minsTard = Math.max(0, hh * 60 + mm - entradaMins);
      const salidaRealMins = salidaMinsBase + minsTard;
      salidaReal = String(Math.floor(salidaRealMins / 60)).padStart(2, "0") + ":" + String(salidaRealMins % 60).padStart(2, "0");
    }

    setManualForm(f => ({
      ...f,
      idEmpleado: j.idEmpleado,
      motivo: j.motivo ? "Corrección de tardanza justificada: " + j.motivo : "",
    }));

    setCorreccionInfo({
      fechaCorreccion: fechaStr,
      entradaProgramada: horEmp.horaEntrada,
      horaCorregida,
      horaEntradaDisplay,
      horaEntradaReal,
      minutosTardanza: minsTard,
      salidaProgramada: horEmp.horaSalida,
      salidaPropuesta: salidaReal,
      salidaReal,
      sinRegistro,
      evidenciaUrl: j.evidenciaUrl || j.evidencia || "",
    });
  };

  const getNombreEmpleado = (idEmpleado) => {
    const emp = empleados.find(e => e.idEmpleado == idEmpleado);
    return emp ? `${emp.nombres} ${emp.apellidos}` : `#${idEmpleado}`;
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content asistencia-rrhh">
        <header className="main-header header-rrhh asistencia-header">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Control de Asistencia</span>
            <h1>Control de Asistencia</h1>
            <p>Gestión diaria de registros y control biométrico del personal.</p>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-03</span>
        </header>

        <ApiAlert type="success" message={mensaje} />
        <ApiAlert type="error" message={error} />

        <div className="biometric-banner asistencia-biometric">
          <div className="biometric-banner-icon">BIO</div>
          <div className="biometric-banner-text">
            <h3>Sistema Biométrico Activo</h3>
            <p>El registro de entrada/salida de empleados se realiza desde el dispositivo biométrico. Aquí gestionas correcciones y justificaciones.</p>
          </div>
        </div>

        <div className="asistencia-grid">
        {/* -- SECCIÓN 1: JUSTIFICACIONES PENDIENTES -- */}
        <div className="section-card asistencia-panel asistencia-justificaciones">
          <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>
              Justificaciones Pendientes
              {justificaciones.length > 0 && (
                <span style={{
                  marginLeft: 10, background: "#fef3c7", color: "#92400e",
                  fontSize: "12px", fontWeight: 700, padding: "2px 8px",
                  borderRadius: 20, verticalAlign: "middle",
                }}>
                  {justificaciones.length}
                </span>
              )}
            </h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarJustificaciones}>Actualizar</button>
          </div>
            <div className="alert alert-info">
              Revise cada justificación y decida si aprobarla o rechazarla.
            </div>
            <div className="table-wrapper">
              {loading ? <Spinner text="Cargando justificaciones..." /> : (
                <table>
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Empleado</th>
                      <th>Fecha</th>
                      <th>Debía llegar</th>
                      <th>Llegó a</th>
                      <th>Tardanza</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr><td colSpan={7}>
                        <div className="empty-state">
                          <div className="empty-state-icon">Sin datos</div>
                          <p>No hay justificaciones pendientes de revisión</p>
                        </div>
                      </td></tr>
                    ) : justificaciones.map((j, idx) => {
                      const aprobada = j.estado === "APROBADA";
                      const seleccionada = justificacionSeleccionada === (j.id ?? j.idJustificacion);
                      return (
                      <tr
                        key={j.id ?? j.idJustificacion}
                        onClick={async () => {
                          if (!aprobada) { setModalDecision({ open: true, justificacion: j }); return; }
                          setMensaje(""); setError("");
                          try {
                            await handleCorregirTardanza(j);
                            document.querySelector(".asistencia-manual")?.scrollIntoView({ behavior: "smooth" });
                          } catch (err) {
                            setError(err);
                          }
                        }}
                        style={{
                          cursor: "pointer",
                          background: aprobada
                            ? "#dcfce7"
                            : seleccionada
                            ? "var(--accent-light, #eff6ff)"
                            : undefined,
                          borderLeft: aprobada ? "4px solid #16a34a" : undefined,
                        }}
                      >
                        <td>{idx + 1}</td>
                        <td>{getNombreEmpleado(j.idEmpleado)}</td>
                        <td>{j.fechaAsistencia ?? j.fechaRegistro ?? "—"}</td>
                        <td style={{ fontWeight: 600, color: "var(--text-2)" }}>
                          {j.horaEntradaEsperada ?? "—"}
                        </td>
                        <td style={{ fontWeight: 600, color: j.horaEntrada ? "#dc2626" : "var(--text-3)" }}>
                          {j.horaEntrada ?? "—"}
                        </td>
                        <td>
                          {j.minutosTardanza != null && j.minutosTardanza > 0 ? (
                            <span className="badge badge-warning" style={{ fontSize: 11 }}>
                              {Math.floor(j.minutosTardanza / 60) > 0
                                ? `${Math.floor(j.minutosTardanza / 60)}h ${j.minutosTardanza % 60}min`
                                : `${j.minutosTardanza} min`}
                            </span>
                          ) : "—"}
                        </td>
                        <td>
                          <span className={`badge ${aprobada ? "badge-success" : "badge-warning"}`}>
                            {aprobada ? "APROBADA · corregir" : (j.estado ?? "Pendiente")}
                          </span>
                        </td>
                      </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
        </div>

        {/* -- SECCIÓN 2: REGISTRO MANUAL -- */}
        <div className="section-card asistencia-panel asistencia-manual">
          <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>Registro Manual de Asistencia</h2>
            <span style={{ fontSize: "12px", color: "var(--text-3)" }}>Corrección · requiere solicitud previa del empleado</span>
          </div>
            <form onSubmit={handleRegistrarManual}>
              {!empleadoSelec && (
                <div className="alert alert-info" style={{ marginBottom: 12 }}>
                  Haga clic en una justificación de la tabla para llenar el formulario automáticamente.
                </div>
              )}
              <div className="form-grid">
                <div className="form-group" style={{ position: "relative" }}>
                  <label>Empleado</label>
                  <input
                    type="text"
                    value={busquedaNombre}
                    readOnly
                    placeholder="Seleccione una justificación..."
                    style={{ background: "var(--bg-2, #f8fafc)", cursor: "not-allowed" }}
                  />
                  <input type="hidden" value={manualForm.idEmpleado} />
                  {empleadoSelec && (
                    <div className="emp-selec-tag">
                      ✓ {empleadoSelec.nombres} {empleadoSelec.apellidos}
                    </div>
                  )}
                </div>
                <div className="form-group">
                  <label>Fecha de Corrección</label>
                  <input type="date" value={correccionInfo.fechaCorreccion}
                    readOnly
                    style={{ background: "var(--bg-2, #f8fafc)", cursor: "not-allowed" }} />
                </div>
                <div className="form-group">
                  <label>Vieja Entrada</label>
                  <input
                    type="time"
                    value={correccionInfo.horaEntradaDisplay || ""}
                    readOnly={!correccionInfo.sinRegistro}
                    placeholder="--:--"
                    onChange={e => {
                      const val = e.target.value;
                      setCorreccionInfo(c => {
                        const [ph, pm] = c.entradaProgramada.split(":").map(Number);
                        const [hh, mm] = val ? val.split(":").map(Number) : [ph, pm];
                        const minsTard = val ? Math.max(0, (hh * 60 + mm) - (ph * 60 + pm)) : 0;
                        return { ...c, horaEntradaDisplay: val, horaCorregida: val, horaEntradaReal: val, minutosTardanza: minsTard };
                      });
                    }}
                    style={correccionInfo.sinRegistro ? {} : { background: "var(--bg-2, #f8fafc)", cursor: "not-allowed" }}
                  />
                </div>
                <div className="form-group">
                  <label>Nueva Salida</label>
                  <input
                    type="time"
                    value={correccionInfo.salidaPropuesta || ""}
                    readOnly={!correccionInfo.sinRegistro}
                    onChange={e => setCorreccionInfo(c => ({ ...c, salidaPropuesta: e.target.value }))}
                    style={correccionInfo.sinRegistro ? {} : { background: "var(--bg-2, #f8fafc)", cursor: "not-allowed" }}
                  />
                </div>
              </div>
              {correccionInfo.sinRegistro && (
                <div className="alert alert-warning" style={{ marginTop: 12 }}>
                  Esta justificación no tiene un registro de asistencia biométrico asociado.
                  Ingrese manualmente la hora de entrada real y la hora de salida.
                </div>
              )}
              <div className="form-actions">
                <button type="submit" className="btn btn-success">Guardar Registro Manual</button>
                <button type="button" className="btn btn-secondary"
                  onClick={() => {
                    setManualForm({ idEmpleado: "", tipo: "ENTRADA", fechaHora: "", motivo: "" });
                    resetCorreccion();
                    setBusquedaNombre("");
                    setEmpleadoSelec(null);
                    setJustificacionSeleccionada(null);
                  }}>
                  Limpiar
                </button>
              </div>
              {correccionInfo.horaCorregida && (
                <div className="alert alert-info" style={{ marginTop: 12 }}>
                  <strong>Corrección de tardanza calculada:</strong><br />
                  Entrada programada: {correccionInfo.entradaProgramada} Vieja entrada: {correccionInfo.horaEntradaReal || correccionInfo.horaCorregida}
                  &nbsp;({Math.floor(correccionInfo.minutosTardanza / 60)}h {correccionInfo.minutosTardanza % 60}min de tardanza)<br />
                  Salida programada: {correccionInfo.salidaProgramada} Propuesta: {correccionInfo.salidaReal || correccionInfo.salidaPropuesta}
                  &nbsp;(recuperación de {Math.floor(correccionInfo.minutosTardanza / 60)}h {correccionInfo.minutosTardanza % 60}min)
                </div>
              )}
            </form>
          </div>
        </div>

        {/* -- SECCIÓN 3: CORRECCIONES REGISTRADAS -- */}
        <div className="section-card asistencia-panel asistencia-correcciones">
          <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>
              Correcciones Registradas
              {correcciones.length > 0 && (
                <span style={{
                  marginLeft: 10, background: "#dcfce7", color: "#15803d",
                  fontSize: "12px", fontWeight: 700, padding: "2px 8px",
                  borderRadius: 20, verticalAlign: "middle",
                }}>
                  {correcciones.length}
                </span>
              )}
            </h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarCorreccionesRegistradas}>Actualizar</button>
          </div>
          <div className="alert alert-info">
            Justificaciones aprobadas cuya corrección manual ya fue guardada. Haz click en una fila para ver su plan de recuperación de horas.
          </div>
          <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
            <button
              className={tabCorrecciones === "TARDANZA" ? "btn btn-primary btn-sm" : "btn btn-secondary btn-sm"}
              onClick={() => setTabCorrecciones("TARDANZA")}>
              Tardanzas ({correcciones.filter(c => c.tipo !== "INASISTENCIA").length})
            </button>
            <button
              className={tabCorrecciones === "INASISTENCIA" ? "btn btn-primary btn-sm" : "btn btn-secondary btn-sm"}
              onClick={() => setTabCorrecciones("INASISTENCIA")}>
              Inasistencias ({correcciones.filter(c => c.tipo === "INASISTENCIA").length})
            </button>
          </div>
          <div className="table-wrapper">
            {loadingCorrecciones ? <Spinner text="Cargando correcciones..." /> : (
              <table>
                <thead>
                  <tr>
                    <th>Empleado</th>
                    <th>Fecha</th>
                    <th>Entrada original</th>
                    <th>Entrada corregida</th>
                    <th>Salida original</th>
                    <th>Salida corregida</th>
                    <th>Motivo</th>
                  </tr>
                </thead>
                <tbody>
                  {correcciones.filter(c => (tabCorrecciones === "INASISTENCIA" ? c.tipo === "INASISTENCIA" : c.tipo !== "INASISTENCIA")).length === 0 ? (
                    <tr><td colSpan={7}>
                      <div className="empty-state">
                        <div className="empty-state-icon">Sin datos</div>
                        <p>Todavía no hay correcciones registradas</p>
                      </div>
                    </td></tr>
                  ) : correcciones
                      .filter(c => (tabCorrecciones === "INASISTENCIA" ? c.tipo === "INASISTENCIA" : c.tipo !== "INASISTENCIA"))
                      .map((c) => (
                    <tr key={c.idJustificacion} style={{ cursor: "pointer" }} onClick={() => abrirPlanRecuperacion(c)} title="Ver plan de recuperación">
                      <td><strong>{c.nombreEmpleado}</strong></td>
                      <td>{c.fecha}</td>
                      <td style={{ color: "#dc2626" }}>{c.horaEntradaOriginal ? c.horaEntradaOriginal.substring(0, 5) : "—"}</td>
                      <td style={{ color: "#16a34a" }}>{c.horaEntradaCorregida ? c.horaEntradaCorregida.substring(0, 5) : "—"}</td>
                      <td style={{ color: "#dc2626" }}>{c.horaSalidaOriginal ? c.horaSalidaOriginal.substring(0, 5) : "—"}</td>
                      <td style={{ color: "#16a34a" }}>{c.horaSalidaCorregida ? c.horaSalidaCorregida.substring(0, 5) : "—"}</td>
                      <td>{c.motivo}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

      </main>

      {/* -- MODAL: PLAN DE RECUPERACIÓN -- */}
      {modalPlan.open && (
        <div className="modal-overlay" onClick={() => setModalPlan(m => ({ ...m, open: false }))}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h3>Plan de recuperación de horas</h3>
                <p>{modalPlan.nombreEmpleado} — {modalPlan.tipo === "INASISTENCIA" ? "Inasistencia" : "Tardanza"} justificada</p>
              </div>
              <button className="modal-close" onClick={() => setModalPlan(m => ({ ...m, open: false }))}>×</button>
            </div>
            <div className="modal-body">
              {modalPlan.loading ? <Spinner text="Cargando plan..." /> : modalPlan.dias.length === 0 ? (
                <p>No se generó un plan de recuperación para esta corrección (no había horas que recuperar).</p>
              ) : (
                <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
                  {modalPlan.dias.map((d, i) => {
                    const segundosTranscurridos = Math.max(0, Math.floor((tickPlan - modalPlan.fetchedAtMs) / 1000));
                    const segundosRestantes = Math.max(0, d.minutosRestantes * 60 - segundosTranscurridos);
                    const completadoLive = d.completado || segundosRestantes <= 0;
                    const hh = String(Math.floor(segundosRestantes / 3600)).padStart(2, "0");
                    const mm = String(Math.floor((segundosRestantes % 3600) / 60)).padStart(2, "0");
                    const horasTotales = Math.floor(d.horasTotalesDia / 60);
                    const minTotales = d.horasTotalesDia % 60;
                    return (
                      <div key={i} style={{
                        border: "1px solid #e2e8f0", borderRadius: 10, padding: 14,
                        minWidth: 140, flex: "1 1 140px", textAlign: "center",
                      }}>
                        <strong style={{ display: "block", marginBottom: 2 }}>Día {i + 1}</strong>
                        <span style={{ fontSize: 12, color: "#64748b" }}>{d.fecha}</span>

                        <div style={{
                          fontSize: 32, fontWeight: 800, fontVariantNumeric: "tabular-nums",
                          margin: "10px 0", color: completadoLive ? "#16a34a" : "#2563eb",
                        }}>
                          {completadoLive ? "✓" : `${hh}:${mm}`}
                        </div>

                        {completadoLive ? (
                          <span style={{ color: "#16a34a", fontWeight: 700, fontSize: 13 }}>Completado</span>
                        ) : (
                          <span style={{ color: "#92400e", fontWeight: 600, fontSize: 12 }}>restante</span>
                        )}

                        <div style={{ fontSize: 12, color: "#475569", marginTop: 10, textAlign: "left" }}>
                          Entrada: {String(d.horaEntrada).substring(0, 5)}<br />
                          Salida normal: {String(d.horaSalidaOriginal).substring(0, 5)}<br />
                          Salida c/recuperación: <strong>{String(d.horaSalidaModificada).substring(0, 5)}</strong><br />
                          Total del día: {horasTotales}h {minTotales}min (+{d.minutosRecuperacion} min)
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalPlan(m => ({ ...m, open: false }))}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

      {/* -- MODAL: DECISIÓN -- */}
      {modalDecision.open && modalDecision.justificacion && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>Decisión de Justificación</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-2)" }}>
                  {getNombreEmpleado(modalDecision.justificacion.idEmpleado)}
                  {modalDecision.justificacion.fechaAsistencia
                    ? ` — ${modalDecision.justificacion.fechaAsistencia}`
                    : ""}
                </p>
              </div>
              <button className="modal-close" onClick={() => setModalDecision({ open: false, justificacion: null })}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontWeight: 600, marginBottom: 12 }}>¿Desea aprobar o rechazar esta justificación?</p>

              {/* Comparación de horas */}
              {(modalDecision.justificacion.horaEntradaEsperada || modalDecision.justificacion.horaEntrada) && (
                <div style={{
                  display: "flex", gap: 12, marginBottom: 14, alignItems: "center",
                  background: "var(--bg-2, #f8fafc)", borderRadius: 8, padding: "12px 16px",
                  border: "1px solid var(--border)"
                }}>
                  <div style={{ textAlign: "center", flex: 1 }}>
                    <div style={{ fontSize: 11, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 2 }}>Debía llegar</div>
                    <div style={{ fontSize: 22, fontWeight: 700, color: "#16a34a" }}>
                      {modalDecision.justificacion.horaEntradaEsperada ?? "—"}
                    </div>
                  </div>
                  <div style={{ fontSize: 20, color: "var(--text-3)" }}>→</div>
                  <div style={{ textAlign: "center", flex: 1 }}>
                    <div style={{ fontSize: 11, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 2 }}>Llegó a</div>
                    <div style={{ fontSize: 22, fontWeight: 700, color: "#dc2626" }}>
                      {modalDecision.justificacion.horaEntrada ?? "—"}
                    </div>
                  </div>
                  {modalDecision.justificacion.minutosTardanza != null && modalDecision.justificacion.minutosTardanza > 0 && (
                    <>
                      <div style={{ fontSize: 20, color: "var(--text-3)" }}>=</div>
                      <div style={{ textAlign: "center", flex: 1 }}>
                        <div style={{ fontSize: 11, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 2 }}>Tardanza</div>
                        <div style={{ fontSize: 18, fontWeight: 700, color: "#d97706" }}>
                          {Math.floor(modalDecision.justificacion.minutosTardanza / 60) > 0
                            ? `${Math.floor(modalDecision.justificacion.minutosTardanza / 60)}h ${modalDecision.justificacion.minutosTardanza % 60}min`
                            : `${modalDecision.justificacion.minutosTardanza} min`}
                        </div>
                      </div>
                    </>
                  )}
                </div>
              )}

              {/* Motivo */}
              <div style={{ marginBottom: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 6 }}>
                  Motivo del empleado
                </div>
                <div style={{
                  background: "var(--bg-2, #f8fafc)", border: "1px solid var(--border)",
                  borderRadius: 8, padding: "12px 14px", fontSize: 13,
                  color: "var(--text-1)", whiteSpace: "pre-wrap", lineHeight: 1.6,
                  minHeight: 48,
                }}>
                  {modalDecision.justificacion.motivo || <span style={{ color: "var(--text-3)" }}>Sin motivo registrado.</span>}
                </div>
              </div>

              {/* Evidencia */}
              <div style={{ marginBottom: 14 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: "var(--text-3)", textTransform: "uppercase", marginBottom: 6 }}>
                  Evidencia adjunta
                </div>
                {(modalDecision.justificacion.evidenciaUrl || modalDecision.justificacion.evidencia) ? (() => {
                  const url = resolveUrl(modalDecision.justificacion.evidenciaUrl ?? modalDecision.justificacion.evidencia);
                  const esImg = /\.(jpg|jpeg|png|gif|webp|bmp)(\?|$)/i.test(url);
                  return esImg ? (
                    <div className="evidencia-foto-container" onClick={() => setFotoExpandida(url)} title="Click para ampliar">
                      <img src={url} alt="Evidencia" className="evidencia-foto-img" style={{ maxHeight: 280 }} />
                      <div className="evidencia-foto-overlay">Click para ampliar</div>
                    </div>
                  ) : (
                    <a href={url} target="_blank" rel="noopener noreferrer" className="btn btn-secondary btn-sm">
                      Ver evidencia adjunta
                    </a>
                  );
                })() : (
                  <div style={{
                    border: "2px dashed var(--border)", borderRadius: 8, padding: "24px",
                    textAlign: "center", color: "var(--text-3)", fontSize: 13,
                  }}>
                    <div style={{ fontSize: 32, marginBottom: 6 }}>Sin archivo</div>
                    Sin evidencia adjunta
                  </div>
                )}
              </div>

              <div className="alert alert-warning" style={{ fontSize: 12, marginBottom: 0 }}>
                Si aprueba: los minutos de tardanza se anulan y<strong>no se descontarán en nómina</strong>.
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDecision({ open: false, justificacion: null })}>Cancelar</button>
              <button className="btn btn-danger" onClick={handleDecidirRechazar}>Rechazar</button>
              <button className="btn btn-success" onClick={handleDecidirAprobar}>Aprobar</button>
            </div>
          </div>
        </div>
      )}

      {/* -- MODAL: RECHAZAR JUSTIFICACIÓN -- */}
      {modalRechazar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>Rechazar Justificación</h3><p>Justificación #{modalRechazar.id}</p></div>
              <button className="modal-close" onClick={() => setModalRechazar({ open: false, id: null, comentario: "" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 12 }}>El motivo será registrado en el sistema.</div>
              <div className="form-group">
                <label>Motivo del rechazo *</label>
                <textarea
                  rows={3}
                  value={modalRechazar.comentario}
                  onChange={e => setModalRechazar({ ...modalRechazar, comentario: e.target.value })}
                  placeholder="Explique el motivo del rechazo..."
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalRechazar({ open: false, id: null, comentario: "" })}>Cancelar</button>
              <button className="btn btn-danger" onClick={handleConfirmarRechazo} disabled={!modalRechazar.comentario.trim()}>
                Confirmar Rechazo
              </button>
            </div>
          </div>
        </div>
      )}

      {fotoExpandida && <FotoExpandida src={fotoExpandida} onClose={() => setFotoExpandida(null)} />}
    </div>
  );
}

/* ----------------------------------------------------------
   VISTA GERENCIA (réplica de VistaEmpleado con tema gerencia)
---------------------------------------------------------- */
function VistaGerencia() {
  const idEmpleado = getIdEmpleado();

  const [justificaciones, setJustificaciones] = useState([]);
  const [asistenciaHoy,   setAsistenciaHoy]   = useState(null);
  const [historial,       setHistorial]        = useState([]);
  const [horario,         setHorario]          = useState({ horaEntrada: "08:00", horaSalida: "17:00" });
  const [loading,  setLoading]  = useState(false);
  const [mensaje,  setMensaje]  = useState("");
  const [error,    setError]    = useState("");
  const [tabActiva, setTabActiva] = useState("info");

  const [modalManual,  setModalManual]  = useState({ open: false, tipo: "ENTRADA" });
  const [manualMotivo, setManualMotivo] = useState("");
  const [saving,       setSaving]       = useState(false);

  const [modalJustificar, setModalJustificar] = useState({ open: false, justificacion: null });
  const [justifMotivo,       setJustifMotivo]       = useState("");
  const [justifFile,         setJustifFile]         = useState(null);
  const [justifEvidenciaUrl, setJustifEvidenciaUrl] = useState("");
  const [justifSaving,       setJustifSaving]       = useState(false);

  // Detalle de justificación (ver evidencia)
  const [modalDetalle, setModalDetalle] = useState(null);

  const cerrarModalJustif = () => {
    setModalJustificar({ open: false, justificacion: null });
    setJustifMotivo("");
    setJustifFile(null);
    setJustifEvidenciaUrl("");
  };

  useEffect(() => {
    if (!idEmpleado) return;
    cargarJustificaciones();
    cargarAsistenciaHoy();
    cargarHistorial();
    cargarHorario();
  }, [idEmpleado]); // eslint-disable-line react-hooks/exhaustive-deps

  async function cargarHorario() {
    try {
      const data = await getHorarioEmpleado(idEmpleado);
      if (data) setHorario(data);
    } catch { /* silencioso */ }
  }

  async function cargarJustificaciones() {
    try {
      const data = await listarJustificacionesEmpleado(idEmpleado);
      setJustificaciones(Array.isArray(data) ? data : []);
    } catch { /* silencioso */ }
  }

  async function cargarAsistenciaHoy() {
    try {
      const data = await getAsistenciaHoy(idEmpleado);
      setAsistenciaHoy(data);
    } catch { /* silencioso */ }
  }

  async function cargarHistorial() {
    setLoading(true);
    try {
      const data = await listarAsistenciaEmpleado(idEmpleado);
      setHistorial(Array.isArray(data) ? data : []);
    } catch (err) { setError(err); }
    finally  { setLoading(false); }
  }

  const handleEnviarJustificacion = async () => {
    if (!justifMotivo.trim()) return;
    setJustifSaving(true);
    setMensaje("");
    try {
      const j = modalJustificar.justificacion;
      let evidenciaUrl = justifEvidenciaUrl;
      if (justifFile && !evidenciaUrl) {
        const res = await uploadEvidencia(justifFile);
        evidenciaUrl = res.url ?? "";
      }
      await registrarJustificacion({ idEmpleado, idRegistroAsistencia: j.idRegistroAsistencia, motivo: justifMotivo, evidenciaUrl });
      setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
      cerrarModalJustif();
      cargarJustificaciones();
    } catch (err) {
      setError(err);
      cerrarModalJustif();
    } finally { setJustifSaving(false); }
  };

  const handleRegistrarManual = async () => {
    if (!manualMotivo.trim()) return;
    if (!idEmpleado) { setError("No se encontró su ID de empleado. Contacte a RRHH."); setModalManual({ open: false, tipo: "ENTRADA" }); return; }
    setSaving(true);
    try {
      const now = new Date();
      const fechaHora = now.toISOString().substring(0, 19);
      await registrarManual({ idEmpleado, tipo: modalManual.tipo, fechaHora, motivo: manualMotivo });
      setMensaje(`${modalManual.tipo === "ENTRADA" ? "Entrada" : "Salida"} registrada correctamente.`);
      setModalManual({ open: false, tipo: "ENTRADA" });
      setManualMotivo("");
    } catch (err) {
      setError(err);
      setModalManual({ open: false, tipo: "ENTRADA" });
    } finally { setSaving(false); }
  };

  const fechaCorta = new Date().toLocaleDateString("es-PE", { day: "2-digit", month: "2-digit", year: "numeric" });
  const registrosJustificables = registrosPendientesDeJustificar(historial, justificaciones);
  const registroJustificableHoy = esRegistroJustificable(asistenciaHoy) &&
    estaDentroDelPlazo(asistenciaHoy?.fecha) &&
    !tieneJustificacionActiva(asistenciaHoy, justificaciones)
      ? asistenciaHoy
      : null;
  const registrosParaJustificar = registrosJustificables.length > 0
    ? registrosJustificables
    : (registroJustificableHoy ? [registroJustificableHoy] : []);
  const puedeJustificar = registrosParaJustificar.length > 0;

  const estadoBadge = (estado) => ({ PENDIENTE: "badge-warning", APROBADO: "badge-success", RECHAZADO: "badge-danger" }[estado] ?? "badge-gray");

  return (
    <div className="dashboard">
      <Sidebar rol="GERENCIA" />

      <main className="main-content">
        <header className="main-header header-gerencia">
          <div className="header-left">
            <span className="page-breadcrumb">Gerencia / Mi Asistencia</span>
            <h1>Control de Asistencia</h1>
          </div>
          <span className="role-pill role-pill-gerencia">Gestión Ejecutiva</span>
        </header>

        <ApiAlert type="success" message={mensaje} style={{ marginBottom: 16 }} />
        <ApiAlert type="error" message={error} style={{ marginBottom: 16 }} />

        <div className="asist-grid">
          <div className="section-card" style={{ textAlign: "center", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
            <div style={{ fontSize: 12, fontWeight: 600, color: "var(--text-3)", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 6 }}>
              Hora actual
            </div>
            <Reloj />
            <div style={{ margin: "14px 0" }}>
              {asistenciaHoy ? (
                <span className={`badge ${
                  asistenciaHoy.estado === "PUNTUAL"     ? "badge-success" :
                  asistenciaHoy.estado === "TARDANZA"    ? "badge-warning" :
                  asistenciaHoy.estado === "JUSTIFICADA" ? "badge-justificada" :
                  asistenciaHoy.estado === "INASISTENCIA" ? "badge-danger" : "badge-gray"
                }`} style={{ fontSize: 13, padding: "6px 18px" }}>
                  {asistenciaHoy.estado}
                </span>
              ) : (
                <span className="badge badge-gray" style={{ fontSize: 13, padding: "6px 18px" }}>Sin registro hoy</span>
              )}
            </div>
            <div className="asist-horario-tag">Horario establecido: {horario.horaEntrada} a.m. - {horario.horaSalida} p.m.</div>
          </div>

          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>Mi Asistencia Hoy</h2>
              <span style={{ fontSize: "12px", color: "var(--text-3)" }}>Fecha: {fechaCorta}</span>
            </div>
            <div className="asist-today-grid">
              <div className="asist-today-item">
                <label>Entrada</label>
                <div className="asist-today-val">{asistenciaHoy?.horaEntrada ? asistenciaHoy.horaEntrada.substring(0, 5) : "—:—"}</div>
                <span className={`badge ${asistenciaHoy?.horaEntrada ? "badge-success" : "badge-gray"}`} style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaEntrada ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Salida</label>
                <div className="asist-today-val" style={{ color: asistenciaHoy?.horaSalida ? "var(--text)" : "var(--text-3)" }}>
                  {asistenciaHoy?.horaSalida ? asistenciaHoy.horaSalida.substring(0, 5) : "—:—"}
                </div>
                <span className={`badge ${asistenciaHoy?.horaSalida ? "badge-success" : "badge-gray"}`} style={{ marginTop: 4, fontSize: "11px" }}>
                  {asistenciaHoy?.horaSalida ? "Registrada" : "Pendiente"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Horas trabajadas</label>
                <span className="badge badge-info" style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.horasTrabajadas != null ? `${asistenciaHoy.horasTrabajadas} h` : asistenciaHoy?.horaEntrada ? "En curso" : "—"}
                </span>
              </div>
              <div className="asist-today-item">
                <label>Estado del día</label>
                <span className={`badge ${
                  asistenciaHoy?.estado === "PUNTUAL"     ? "badge-success" :
                  asistenciaHoy?.estado === "JUSTIFICADA" ? "badge-justificada" :
                  asistenciaHoy?.estado === "TARDANZA"    ? "badge-warning" :
                  asistenciaHoy?.estado === "INASISTENCIA" ? "badge-danger" : "badge-gray"
                }`} style={{ fontSize: "11px" }}>
                  {asistenciaHoy?.estado ?? "Sin registro"}
                </span>
              </div>
            </div>
            <div className="alert alert-warning" style={{ marginTop: 8, marginBottom: 0 }}>
              <strong>Importante:</strong> Si llegas después del horario establecido, deberás justificar tu tardanza.
            </div>
          </div>
        </div>

        <div className="tab-bar">
          <button className={`tab ${tabActiva === "justificaciones" ? "active" : ""}`}
            onClick={() => { setTabActiva("justificaciones"); if (idEmpleado) cargarJustificaciones(); }}>
            Mis Justificaciones
          </button>
          <button className={`tab ${tabActiva === "nueva-justif" ? "active" : ""}`}
            onClick={() => { if (!puedeJustificar) return; setTabActiva("nueva-justif"); }}
            disabled={!puedeJustificar}
            title=""
            style={!puedeJustificar ? { opacity: 0.5, cursor: "not-allowed" } : {}}>
            Nueva Justificación
          </button>
          <button className={`tab ${tabActiva === "info" ? "active" : ""}`}
            onClick={() => setTabActiva("info")}>
            Información
          </button>
        </div>

        {tabActiva === "justificaciones" && (
          <div className="section-card">
            <div className="section-header">
              <h2 style={{ margin: 0, border: "none", padding: 0 }}>Mis Justificaciones</h2>
              <span style={{ fontSize: "12px", color: "var(--text-2)" }}>Aquí puedes dar seguimiento a tus justificaciones.</span>
            </div>
            <div className="alert alert-warning">
              Solo tienes <strong>1 día</strong> desde la incidencia para registrar tu justificación.
            </div>
            <div className="table-wrapper">
              {loading ? <Spinner text="Cargando justificaciones..." /> : (
                <table>
                  <thead>
                    <tr><th>Fecha</th><th>Tipo</th><th>Motivo</th><th>Evidencia</th><th>Estado</th><th>Observación</th><th>Acciones</th></tr>
                  </thead>
                  <tbody>
                    {justificaciones.length === 0 ? (
                      <tr><td colSpan={7}><div className="empty-state"><div className="empty-state-icon">Sin datos</div><p>No tienes justificaciones pendientes</p></div></td></tr>
                    ) : justificaciones.map(j => (
                      <tr
                        key={j.idJustificacion ?? j.id}
                        onClick={() => setModalDetalle(j)}
                        style={{ cursor: "pointer" }}
                        title="Click para ver detalle y evidencia"
                      >
                        <td>{j.fechaJustificacion?.substring(0, 10) ?? "—"}</td>
                        <td>{tipoIncidencia(j)}</td>
                        <td>{j.motivo}</td>
                        <td>{j.evidenciaUrl
                          ? <span style={{ color: "#2563eb", fontWeight: 600 }}>Ver foto</span>
                          : <span style={{ color: "var(--text-3)" }}>-</span>}
                        </td>
                        <td><span className={`badge ${estadoBadge(j.estado)}`}>{j.estado ?? "Pendiente"}</span></td>
                        <td>{j.comentarioRevision ?? <span style={{ color: "var(--text-3)" }}>-</span>}</td>
                        <td>
                          {(!j.estado || j.estado === "PENDIENTE") ? (
                            <button className="btn btn-warning btn-sm"
                              onClick={(e) => { e.stopPropagation(); setModalJustificar({ open: true, justificacion: j }); setJustifMotivo(j.motivo ?? ""); }}>
                              Editar
                            </button>
                          ) : (
                            <span className="badge badge-gray" style={{ fontSize: "11px" }}>
                              {j.estado === "APROBADA" ? "Aprobado" : "Rechazado"}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {tabActiva === "nueva-justif" && (
          puedeJustificar ? (
            <div className="section-card">
              <h2>Registrar Nueva Justificación</h2>
              <div className="alert alert-warning">
                Solo puedes justificar incidencias del <strong>día anterior o del día actual</strong>.
              </div>
              <form onSubmit={async (e) => {
                e.preventDefault();
                const fd = new FormData(e.target);
                const idRegistroSeleccionado = fd.get("idRegistroAsistencia");
                const registro = registrosParaJustificar.find(r => String(r.idRegistro) === String(idRegistroSeleccionado));
                if (!registro) { setError("Selecciona una incidencia pendiente de justificar."); return; }
                setJustifSaving(true); setMensaje(""); setError("");
                try {
                  const evidenciaFile = fd.get("evidencia");
                  let evidenciaUrl = "";
                  if (evidenciaFile && evidenciaFile.size > 0) {
                    const res = await uploadEvidencia(evidenciaFile);
                    evidenciaUrl = res.url ?? "";
                  }
                  await registrarJustificacion({ idRegistroAsistencia: registro.idRegistro, idEmpleado, motivo: fd.get("motivo"), evidenciaUrl });
                  setMensaje("Justificación enviada correctamente. RRHH la revisará en breve.");
                  e.target.reset();
                  setTabActiva("justificaciones");
                  cargarJustificaciones();
                } catch (err) { setError(err); }
                finally  { setJustifSaving(false); }
              }}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Incidencia a justificar *</label>
                    <select name="idRegistroAsistencia" required defaultValue={registrosParaJustificar[0]?.idRegistro ?? ""}>
                      {registrosParaJustificar.map(r => (
                        <option key={r.idRegistro} value={r.idRegistro}>
                          {r.fecha} - {tipoIncidencia(r)}{tipoIncidencia(r) === "Tardanza" ? ` (${r.minutosTardanza ?? 0} min)` : ""}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group form-full">
                    <label>Motivo de la justificación *</label>
                    <textarea name="motivo" rows={4} placeholder="Describa detalladamente el motivo de su tardanza o inasistencia..." required />
                  </div>
                  <div className="form-group form-full">
                    <label>Evidencia fotográfica (opcional)</label>
                    <input type="file" name="evidencia" accept="image/*" />
                    <p style={{ margin: "6px 0 0", fontSize: 12, color: "var(--text-3)" }}>
                      Puedes tomar una foto desde el celular o elegir una imagen desde tu equipo.
                    </p>
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={justifSaving}>{justifSaving ? "Enviando..." : "Enviar Justificación"}</button>
                  <button type="reset" className="btn btn-secondary">Limpiar</button>
                </div>
              </form>
            </div>
          ) : (
            <div className="section-card">
              <div className="alert alert-info">La opción se habilita cuando tienes una tardanza o inasistencia de hoy o ayer pendiente de justificar.</div>
            </div>
          )
        )}

        {tabActiva === "info" && (
          <div className="section-card">
            <h2>¿Cómo funciona el control de asistencia?</h2>
            <div className="alert alert-info"><strong>Registro de entrada:</strong> Coloca tu huella dactilar en el dispositivo biométrico al llegar.</div>
            <div className="alert alert-info"><strong>Registro de salida:</strong> Al terminar tu jornada, vuelve a colocar tu huella.</div>
            <div className="alert alert-warning"><strong>Tardanzas:</strong> Si llegas después del horario establecido, se registrará una incidencia. Dispondrás de 1 día para justificarla.</div>
            <div className="alert alert-warning"><strong>Correcciones:</strong> Si hay un error en tu registro, solicita una corrección a RRHH.</div>
          </div>
        )}

        <div className="asistencia-section-divider" aria-hidden="true" />

        <div className="section-card">
          <div className="section-header">
            <h2 style={{ margin: 0, border: "none", padding: 0 }}>Historial de Asistencia</h2>
            <button className="btn btn-secondary btn-sm" onClick={cargarHistorial}>Actualizar</button>
          </div>
          <div className="table-wrapper">
            {loading ? <Spinner /> : (
              <table>
                <thead>
                  <tr><th>Fecha</th><th>Entrada</th><th>Salida</th><th>Horas</th><th>Tardanza</th><th>Estado</th><th>Tipo registro</th></tr>
                </thead>
                <tbody>
                  {historial.length === 0 ? (
                    <tr><td colSpan={7}><div className="empty-state"><div className="empty-state-icon">Sin datos</div><p>No hay registros de asistencia aún</p></div></td></tr>
                  ) : historial.map(r => (
                    <tr key={r.idRegistro}>
                      <td>{r.fecha}</td>
                      <td>{r.horaEntrada ? r.horaEntrada.substring(0, 5) : "—"}</td>
                      <td>{r.horaSalida  ? r.horaSalida.substring(0, 5)  : "—"}</td>
                      <td>{r.horasTrabajadas != null ? `${r.horasTrabajadas} h` : "—"}</td>
                      <td>{r.minutosTardanza > 0 ? `${r.minutosTardanza} min` : "—"}</td>
                      <td>
                        <span className={`badge ${r.estado === "PUNTUAL" ? "badge-success" : r.estado === "JUSTIFICADA" ? "badge-justificada" : r.estado === "TARDANZA" ? "badge-warning" : r.estado === "INASISTENCIA" ? "badge-danger" : "badge-gray"}`}>
                          {r.estado ?? "—"}
                        </span>
                      </td>
                      <td style={{ fontSize: "12px", color: "var(--text-3)" }}>{r.tipoUltimoRegistro ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </main>

      {modalJustificar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>Registrar Justificación de Tardanza</h3>
                <p>Fecha: {modalJustificar.justificacion?.fechaJustificacion?.substring(0, 10) ?? "—"}</p>
              </div>
              <button className="modal-close" onClick={cerrarModalJustif}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label>Motivo de la tardanza *</label>
                <textarea rows={4} value={justifMotivo} onChange={e => setJustifMotivo(e.target.value)}
                  placeholder="Describa el motivo de su tardanza..." autoFocus />
              </div>
              <div className="form-group">
                <label>Evidencia (opcional)</label>
                {justifEvidenciaUrl ? (
                  <div className="galeria-evidencia-preview">
                    <img src={justifEvidenciaUrl} alt="Evidencia seleccionada" className="galeria-preview-img" />
                    <button className="btn btn-secondary btn-sm" style={{ marginTop: 6 }}
                      onClick={() => setJustifEvidenciaUrl("")}>
                      × Quitar foto
                    </button>
                  </div>
                ) : (
                  <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    <input
                      type="file"
                      accept="image/*,application/pdf"
                      onChange={e => { setJustifFile(e.target.files[0] ?? null); setJustifEvidenciaUrl(""); }}
                      style={{ flex: 1, fontSize: 13 }}
                    />
                  </div>
                )}
                {justifFile && !justifEvidenciaUrl && (
                  <div style={{ fontSize: 11, color: "#16a34a", marginTop: 4 }}>
                    {justifFile.name}
                  </div>
                )}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={cerrarModalJustif}>Cancelar</button>
              <button className="btn btn-primary" onClick={handleEnviarJustificacion} disabled={!justifMotivo.trim() || justifSaving}>
                {justifSaving ? "Enviando..." : "Enviar Justificación"}
              </button>
            </div>
          </div>
        </div>
      )}

      {modalManual.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>{modalManual.tipo === "ENTRADA" ? "Registrar Entrada (Manual)" : "Registrar Salida (Manual)"}</h3>
                <p>{new Date().toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit" })}</p>
              </div>
              <button className="modal-close" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: 14 }}>Esta opción se usa solo cuando el dispositivo biométrico no esté disponible.</div>
              <div className="form-group">
                <label>Motivo del registro manual *</label>
                <textarea rows={3} value={manualMotivo} onChange={e => setManualMotivo(e.target.value)}
                  placeholder="Ej: Dispositivo biométrico no disponible..." autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalManual({ open: false, tipo: "ENTRADA" })}>Cancelar</button>
              <button className={modalManual.tipo === "ENTRADA" ? "btn btn-success" : "btn btn-warning"}
                onClick={handleRegistrarManual} disabled={!manualMotivo.trim() || saving}>
                {saving ? <><span className="btn-spinner" />Guardando...</> : (modalManual.tipo === "ENTRADA" ? "Confirmar Entrada" : "Confirmar Salida")}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* -- MODAL GALERÍA -- */}
      {/* -- MODAL DETALLE JUSTIFICACIÓN -- */}
      {modalDetalle && (
        <DetalleJustificacionModal
          justificacion={modalDetalle}
          onClose={() => setModalDetalle(null)}
        />
      )}
    </div>
  );
}

/* ----------------------------------------------------------
   ROUTER PRINCIPAL
---------------------------------------------------------- */
export default function ControlAsistencia() {
  const location = useLocation();
  const rol = getRolFromPath(location.pathname);
  if (rol === "EMPLEADO") return <VistaEmpleado />;
  if (rol === "GERENCIA") return <VistaGerencia />;
  return <VistaRRHH />;
}
