import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import "../styles/Dashboard.css";
import {
  getHorarios, crearHorario, actualizarHorario, eliminarHorario,
  asignarHorario, getEmpleadosPorHorario, listarEmpleados,
} from "../services/api";

const FORM_VACIO = {
  nombreTurno: "",
  horaEntrada: "",
  horaSalida:  "",
  tolerancia:  10,
};

export default function GestionHorarios() {
  const [horarios,  setHorarios]  = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [mensaje,   setMensaje]   = useState("");
  const [mensajeOk, setMensajeOk] = useState(true);

  const [modalForm, setModalForm] = useState({ open: false, modo: "crear", data: null });
  const [form,      setForm]      = useState(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [modalElim, setModalElim] = useState({ open: false, horario: null });

  // ── ASIGNAR EMPLEADOS ──
  const [modalAsignar,     setModalAsignar]     = useState({ open: false, horario: null });
  const [empleadosHorario, setEmpleadosHorario] = useState([]);
  const [todosEmpleados,   setTodosEmpleados]   = useState([]);
  const [selectedEmps,     setSelectedEmps]     = useState([]);
  const [buscarEmp,        setBuscarEmp]        = useState("");
  const [expandirLista,    setExpandirLista]    = useState(true);
  const [asignando,        setAsignando]        = useState(false);

  useEffect(() => { cargar(); }, []);

  async function cargar() {
    setLoading(true);
    try {
      const data = await getHorarios();
      setHorarios(data || []);
    } catch {
      setMensaje("Error al cargar los horarios");
      setMensajeOk(false);
    } finally {
      setLoading(false);
    }
  }

  const abrirCrear = () => {
    setForm(FORM_VACIO);
    setModalForm({ open: true, modo: "crear", data: null });
  };

  const abrirEditar = (h) => {
    setForm({
      nombreTurno: h.nombreTurno ?? "",
      horaEntrada: h.horaEntrada?.slice(0, 5) ?? "",
      horaSalida:  h.horaSalida?.slice(0, 5) ?? "",
      tolerancia:  h.tolerancia ?? 10,
    });
    setModalForm({ open: true, modo: "editar", data: h });
  };

  const cerrarModal = () => setModalForm(m => ({ ...m, open: false }));

  const handleGuardar = async (e) => {
    e.preventDefault();
    setGuardando(true);
    const dto = {
      nombreTurno: form.nombreTurno,
      horaEntrada: form.horaEntrada,
      horaSalida:  form.horaSalida,
      tolerancia:  Number(form.tolerancia),
    };
    try {
      if (modalForm.modo === "crear") {
        await crearHorario(dto);
        setMensaje("Horario creado correctamente");
      } else {
        await actualizarHorario(modalForm.data.idHorario, dto);
        setMensaje("Horario actualizado correctamente");
      }
      setMensajeOk(true);
      cerrarModal();
      cargar();
    } catch {
      setMensaje("Error al guardar el horario");
      setMensajeOk(false);
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async () => {
    try {
      await eliminarHorario(modalElim.horario.idHorario);
      setMensaje("Horario desactivado correctamente");
      setMensajeOk(true);
      setModalElim({ open: false, horario: null });
      cargar();
    } catch {
      setMensaje("Error al desactivar el horario");
      setMensajeOk(false);
      setModalElim({ open: false, horario: null });
    }
  };

  const abrirAsignar = async (h) => {
    setModalAsignar({ open: true, horario: h });
    setSelectedEmps([]);
    setBuscarEmp("");
    setExpandirLista(true);
    try {
      const [asignados, todos] = await Promise.all([
        getEmpleadosPorHorario(h.idHorario),
        listarEmpleados(0, 200),
      ]);
      setEmpleadosHorario(asignados || []);
      const lista = todos?.content ?? (Array.isArray(todos) ? todos : []);
      setTodosEmpleados(lista.filter(e => e.estado === "ACTIVO" || !e.estado));
    } catch {
      setEmpleadosHorario([]);
      setTodosEmpleados([]);
    }
  };

  const cerrarAsignar = () => setModalAsignar({ open: false, horario: null });

  const toggleEmp = (id) =>
    setSelectedEmps(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);

  const handleAsignarMultiple = async () => {
    if (!selectedEmps.length) return;
    setAsignando(true);
    try {
      await Promise.all(
        selectedEmps.map(idEmpleado =>
          asignarHorario({
            idEmpleado,
            idHorario: modalAsignar.horario.idHorario,
            fechaDesde: new Date().toISOString().slice(0, 10),
            esTemporal: false,
          })
        )
      );
      setMensaje(`${selectedEmps.length} empleado(s) asignado(s) al turno "${modalAsignar.horario.nombreTurno}"`);
      setMensajeOk(true);
      // Refrescar lista de asignados
      const asignados = await getEmpleadosPorHorario(modalAsignar.horario.idHorario);
      setEmpleadosHorario(asignados || []);
      setSelectedEmps([]);
    } catch {
      setMensaje("Error al asignar empleados");
      setMensajeOk(false);
    } finally {
      setAsignando(false);
    }
  };

  const duracionHoras = (entrada, salida) => {
    if (!entrada || !salida) return null;
    const [h1, m1] = entrada.split(":").map(Number);
    const [h2, m2] = salida.split(":").map(Number);
    let mins = (h2 * 60 + m2) - (h1 * 60 + m1);
    if (mins <= 0) mins += 24 * 60;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return m > 0 ? `${h}h ${m}min` : `${h}h`;
  };

  const empleadosDisponibles = todosEmpleados.filter(e => {
    const yaAsignado = empleadosHorario.some(a => a.idEmpleado === e.idEmpleado);
    if (yaAsignado) return false;
    const nombre = `${e.nombres ?? ""} ${e.apellidos ?? ""}`.toLowerCase();
    return nombre.includes(buscarEmp.toLowerCase());
  });

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content horarios-home">
        <header className="main-header header-rrhh horarios-header">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Gestión de Empleados / Horarios</span>
            <h1>Gestión de Horarios</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Defina los turnos disponibles: nombre, hora de entrada y hora de salida.
              Las fechas de vigencia se gestionan al asignar el turno a un empleado.
            </p>
          </div>
          <button className="btn btn-success" onClick={abrirCrear}>＋ Nuevo Turno</button>
        </header>

        {mensaje && (
          <div className={`alert ${mensajeOk ? "alert-success" : "alert-warning"}`}
            style={{ cursor: "pointer" }} onClick={() => setMensaje("")}>
            {mensajeOk ? "✅" : "⚠️"} {mensaje}
          </div>
        )}

        {/* ── TARJETAS RESUMEN ── */}
        {!loading && horarios.length > 0 && (
          <div className="horarios-summary-grid">
            {horarios.map(h => (
              <div key={h.idHorario} style={{
                background: "#fff", border: "1px solid var(--border)", borderRadius: 12,
                padding: "16px 20px", minWidth: 180, flex: "1 1 180px", maxWidth: 240,
                boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
              }}>
                <div style={{ fontSize: 22, marginBottom: 6 }} aria-hidden="true" />
                <div style={{ fontWeight: 700, fontSize: 14, color: "var(--text-1)", marginBottom: 4 }}>
                  {h.nombreTurno}
                </div>
                <div style={{ fontSize: 13, color: "var(--primary)", fontWeight: 600, marginBottom: 2 }}>
                  {h.horaEntrada?.slice(0,5)} – {h.horaSalida?.slice(0,5)}
                </div>
                <div style={{ fontSize: 11, color: "var(--text-3)" }}>
                  Duración: {duracionHoras(h.horaEntrada?.slice(0,5), h.horaSalida?.slice(0,5)) ?? "—"}
                </div>
                <div style={{ fontSize: 11, color: "var(--text-3)" }}>
                  Tolerancia: {h.tolerancia ?? 10} min
                </div>
              </div>
            ))}
          </div>
        )}

        {/* ── TABLA ── */}
        <div className="section-card horarios-list-card" style={{ padding: 0, overflow: "hidden" }}>
          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? (
              <Spinner />
            ) : horarios.length === 0 ? (
              <div className="empty-state" style={{ padding: "48px 0" }}>
                <div className="empty-state-icon">🕐</div>
                <p>No hay turnos registrados. Crea el primero.</p>
                <button className="btn btn-success" style={{ marginTop: 16 }} onClick={abrirCrear}>
                  ＋ Crear turno
                </button>
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Nombre del turno</th>
                    <th style={{ textAlign: "center" }}>Hora entrada</th>
                    <th style={{ textAlign: "center" }}>Hora salida</th>
                    <th style={{ textAlign: "center" }}>Duración</th>
                    <th style={{ textAlign: "center" }}>Tolerancia</th>
                    <th style={{ textAlign: "center" }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {horarios.map(h => {
                    const dur = duracionHoras(h.horaEntrada?.slice(0,5), h.horaSalida?.slice(0,5));
                    return (
                      <tr key={h.idHorario}>
                        <td style={{ fontWeight: 600 }}>{h.nombreTurno}</td>
                        <td style={{ textAlign: "center" }}>
                          <span style={{
                            background: "#eff6ff", color: "#2563eb",
                            borderRadius: 6, padding: "3px 12px", fontSize: 13, fontWeight: 600,
                          }}>
                            {h.horaEntrada?.slice(0,5) ?? "—"}
                          </span>
                        </td>
                        <td style={{ textAlign: "center" }}>
                          <span style={{
                            background: "#f0fdf4", color: "#16a34a",
                            borderRadius: 6, padding: "3px 12px", fontSize: 13, fontWeight: 600,
                          }}>
                            {h.horaSalida?.slice(0,5) ?? "—"}
                          </span>
                        </td>
                        <td style={{ textAlign: "center", fontSize: 13, color: "var(--text-2)" }}>
                          {dur ?? "—"}
                        </td>
                        <td style={{ textAlign: "center", fontSize: 13, color: "var(--text-2)" }}>
                          {h.tolerancia ?? 10} min
                        </td>
                        <td style={{ textAlign: "center" }}>
                          <div style={{ display: "inline-flex", gap: 6 }}>
                            <button
                              className="btn btn-sm"
                              style={{ background: "#eff6ff", color: "#2563eb", border: "1px solid #bfdbfe", fontWeight: 700, fontSize: 16, lineHeight: 1, padding: "4px 10px" }}
                              title="Asignar empleados"
                              onClick={() => abrirAsignar(h)}
                            >
                              +
                            </button>
                            <button className="btn btn-secondary btn-sm" onClick={() => abrirEditar(h)}>
                              ✏️ Editar
                            </button>
                            <button
                              className="btn btn-sm"
                              style={{ background: "#fee2e2", color: "#dc2626", border: "1px solid #fca5a5", fontWeight: 700, fontSize: 14 }}
                              onClick={() => setModalElim({ open: true, horario: h })}
                              title="Desactivar turno"
                            >
                              ✕
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
          {!loading && horarios.length > 0 && (
            <div className="pagination-bar">
              <div className="pagination-info">{horarios.length} turno(s) registrado(s)</div>
            </div>
          )}
        </div>
      </main>

      {/* ══ MODAL: CREAR / EDITAR ══ */}
      {modalForm.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>{modalForm.modo === "crear" ? "➕ Nuevo Turno" : "✏️ Editar Turno"}</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  {modalForm.modo === "crear"
                    ? "Define el nombre y el horario del turno."
                    : `Editando: ${modalForm.data?.nombreTurno}`}
                </p>
              </div>
              <button className="modal-close" onClick={cerrarModal}>×</button>
            </div>

            <div className="modal-body">
              {modalForm.modo === "crear" && (
                <div className="alert alert-info" style={{ marginBottom: 16 }}>
                  Las fechas de vigencia (desde / hasta) se configuran al asignar
                  el turno a un empleado desde <strong>Gestión de Empleados</strong>.
                </div>
              )}
              <form id="form-horario-gh" onSubmit={handleGuardar}>
                <div className="form-grid">
                  <div className="form-group form-full">
                    <label>Nombre del turno *</label>
                    <input
                      value={form.nombreTurno}
                      onChange={e => setForm(f => ({ ...f, nombreTurno: e.target.value }))}
                      placeholder="Ej: Turno Mañana, Turno Tarde, Turno Noche..."
                      required
                      autoFocus
                    />
                  </div>
                  <div className="form-group">
                    <label>Hora de entrada *</label>
                    <input
                      type="time"
                      value={form.horaEntrada}
                      onChange={e => setForm(f => ({ ...f, horaEntrada: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Hora de salida *</label>
                    <input
                      type="time"
                      value={form.horaSalida}
                      onChange={e => setForm(f => ({ ...f, horaSalida: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Tolerancia por llegada tarde (min)</label>
                    <input
                      type="number" min="0" max="60"
                      value={form.tolerancia}
                      onChange={e => setForm(f => ({ ...f, tolerancia: e.target.value }))}
                    />
                  </div>
                  {form.horaEntrada && form.horaSalida && (
                    <div className="form-group" style={{ display: "flex", alignItems: "center" }}>
                      <div style={{
                        background: "#eff6ff", border: "1px solid #bfdbfe",
                        borderRadius: 8, padding: "10px 14px", fontSize: 13,
                        color: "#1e40af", width: "100%",
                      }}>
                        ⏱ Duración del turno:{" "}
                        <strong>
                          {duracionHoras(form.horaEntrada, form.horaSalida)}
                        </strong>
                      </div>
                    </div>
                  )}
                </div>
              </form>
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={cerrarModal} disabled={guardando}>
                Cancelar
              </button>
              <button className="btn btn-success" type="submit" form="form-horario-gh" disabled={guardando}>
                {guardando
                  ? <><span className="btn-spinner" />Guardando...</>
                  : modalForm.modo === "crear" ? "✅ Crear Turno" : "💾 Guardar Cambios"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: CONFIRMAR DESACTIVAR ══ */}
      {modalElim.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>Desactivar Turno</h3></div>
              <button className="modal-close" onClick={() => setModalElim({ open: false, horario: null })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">
                ⚠️ El turno <strong>{modalElim.horario?.nombreTurno}</strong> quedará inactivo
                y no podrá ser asignado a nuevos empleados. Los empleados con este turno activo
                conservarán su asignación hasta que sea cambiada manualmente.
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalElim({ open: false, horario: null })}>
                Cancelar
              </button>
              <button className="btn btn-danger" onClick={handleEliminar}>
                Confirmar desactivar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: ASIGNAR EMPLEADOS ══ */}
      {modalAsignar.open && (
        <div className="modal-overlay">
          <div className="modal-box" style={{ maxWidth: 560 }}>
            <div className="modal-header">
              <div>
                <h3>Asignar empleados al turno</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  {modalAsignar.horario?.nombreTurno}
                  {" · "}
                  {modalAsignar.horario?.horaEntrada?.slice(0,5)} – {modalAsignar.horario?.horaSalida?.slice(0,5)}
                </p>
              </div>
              <button className="modal-close" onClick={cerrarAsignar}>×</button>
            </div>

            <div className="modal-body" style={{ display: "flex", flexDirection: "column", gap: 20 }}>

              {/* Lista desplegable de empleados actuales */}
              <div>
                <button
                  type="button"
                  onClick={() => setExpandirLista(v => !v)}
                  style={{
                    background: "none", border: "none", cursor: "pointer",
                    display: "flex", alignItems: "center", gap: 8,
                    fontWeight: 600, fontSize: 14, color: "var(--text-1)", padding: 0,
                  }}
                >
                  <span style={{ fontSize: 11 }}>{expandirLista ? "▼" : "▶"}</span>
                  Empleados en este turno ({empleadosHorario.length})
                </button>
                {expandirLista && (
                  <div style={{
                    marginTop: 8, border: "1px solid var(--border)", borderRadius: 8,
                    overflow: "hidden", maxHeight: 200, overflowY: "auto",
                  }}>
                    {empleadosHorario.length === 0 ? (
                      <div style={{ padding: "12px 16px", color: "var(--text-3)", fontSize: 13 }}>
                        Sin empleados asignados actualmente
                      </div>
                    ) : (
                      empleadosHorario.map(e => (
                        <div key={e.idEmpleado} style={{
                          padding: "8px 16px", borderBottom: "1px solid var(--border)",
                          fontSize: 13, display: "flex", justifyContent: "space-between", alignItems: "center",
                        }}>
                          <span style={{ fontWeight: 500 }}>{e.apellidos}, {e.nombres}</span>
                          {e.fechaDesde && (
                            <span style={{ fontSize: 11, color: "var(--text-3)" }}>desde {e.fechaDesde}</span>
                          )}
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>

              {/* Asignar nuevos empleados */}
              <div>
                <label style={{ fontWeight: 600, fontSize: 14, display: "block", marginBottom: 8 }}>
                  Asignar empleados
                </label>
                <input
                  placeholder="Buscar por nombre o apellido..."
                  value={buscarEmp}
                  onChange={e => setBuscarEmp(e.target.value)}
                  style={{ width: "100%", marginBottom: 8 }}
                />
                <div style={{
                  border: "1px solid var(--border)", borderRadius: 8,
                  maxHeight: 240, overflowY: "auto",
                }}>
                  {empleadosDisponibles.length === 0 ? (
                    <div style={{ padding: "12px 16px", color: "var(--text-3)", fontSize: 13 }}>
                      {buscarEmp ? "Sin resultados" : "Todos los empleados activos ya están asignados a este turno"}
                    </div>
                  ) : (
                    empleadosDisponibles.map(e => (
                      <label key={e.idEmpleado} style={{
                        display: "flex", alignItems: "center", gap: 10,
                        padding: "9px 14px", cursor: "pointer",
                        borderBottom: "1px solid var(--border)", fontSize: 13,
                        background: selectedEmps.includes(e.idEmpleado) ? "#eff6ff" : "transparent",
                      }}>
                        <input
                          type="checkbox"
                          checked={selectedEmps.includes(e.idEmpleado)}
                          onChange={() => toggleEmp(e.idEmpleado)}
                          style={{ accentColor: "#2563eb" }}
                        />
                        <span style={{ fontWeight: 500 }}>{e.apellidos}, {e.nombres}</span>
                        {e.cargo && (
                          <span style={{ fontSize: 11, color: "var(--text-3)", marginLeft: "auto" }}>{e.cargo}</span>
                        )}
                      </label>
                    ))
                  )}
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={cerrarAsignar}>
                Cerrar
              </button>
              <button
                className="btn btn-success"
                onClick={handleAsignarMultiple}
                disabled={selectedEmps.length === 0 || asignando}
              >
                {asignando
                  ? "Asignando..."
                  : `Asignar${selectedEmps.length ? ` (${selectedEmps.length})` : ""}`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
