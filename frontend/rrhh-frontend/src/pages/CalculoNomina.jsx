import { useState, useEffect, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import ApiAlert from "../components/ApiAlert";
import "../styles/Dashboard.css";
import {
  calcularNomina,
  getNominasPorPeriodo,
  enviarComprobante,
  enviarComprobantesPeriodo,
  descargarComprobante,
  ajustarNomina,
} from "../services/api";

const getBonif = (n) =>
  (Number(n.bonifFamiliar)      || 0) +
  (Number(n.bonifTurnoNocturno) || 0) +
  (Number(n.bonifGuardia)       || 0) +
  (Number(n.bonifRiesgo)        || 0);

const getDesc = (n) =>
  (Number(n.descuentoTardanzas) || 0) +
  (Number(n.descuentoLey)       || 0);

const MESES = [
  { v: "01", l: "Enero" }, { v: "02", l: "Febrero" }, { v: "03", l: "Marzo" },
  { v: "04", l: "Abril" }, { v: "05", l: "Mayo" },    { v: "06", l: "Junio" },
  { v: "07", l: "Julio" }, { v: "08", l: "Agosto" },  { v: "09", l: "Septiembre" },
  { v: "10", l: "Octubre" }, { v: "11", l: "Noviembre" }, { v: "12", l: "Diciembre" },
];

const anioActual = new Date().getFullYear();
const ANIOS = [anioActual - 1, anioActual, anioActual + 1];

export default function CalculoNomina() {
  const [mes,    setMes]    = useState(String(new Date().getMonth() + 1).padStart(2, "0"));
  const [anio,   setAnio]   = useState(String(anioActual));
  const [loading, setLoading] = useState(false);
  const [calculando, setCalculando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [periodoCalculado, setPeriodoCalculado] = useState(false);
  const [verificando, setVerificando] = useState(false);
  const [historial, setHistorial] = useState([]);
  const [mensaje,   setMensaje]   = useState("");
  const [error,     setError]     = useState("");
  const [enviando,  setEnviando]  = useState({});
  const [enviandoTodos, setEnviandoTodos] = useState(false);
  const [modalConfirmarEnvio, setModalConfirmarEnvio] = useState(false);
  const [resultadoEnvio, setResultadoEnvio] = useState(null);
  const [guardandoAjuste, setGuardandoAjuste] = useState(false);
  const [previewNomina, setPreviewNomina] = useState(null);
  const [modalEstructura, setModalEstructura] = useState(null);
  const [selectedEmpleado, setSelectedEmpleado] = useState(null);
  const [datosBase, setDatosBase] = useState({
    sueldoBase: 0, horasTrabajadas: 0, horasExtra: 0,
    horasNocturnas: 0, minutosLate: 0, tieneHijos: false,
    cantidadHijos: 0, tipoPension: "ONP", cantidadGuardias: 0,
  });

  const abrirEstructura = (n) => {
    setModalEstructura(n);
    setDatosBase({
      sueldoBase:       Number(n.sueldoBase ?? 0),
      horasTrabajadas:  Math.round(Number(n.totalHorasTrabajadas ?? 0) * 10) / 10,
      horasExtra:       Math.round(Number(n.totalHorasExtra ?? 0) * 10) / 10,
      horasNocturnas:   Math.round(Number(n.totalHorasNocturnas ?? 0) * 10) / 10,
      minutosLate:      n.totalMinutosTardanza ?? 0,
      tieneHijos:       n.tieneHijos ?? (Number(n.cantidadHijos) > 0),
      cantidadHijos:    n.cantidadHijos ?? 0,
      tipoPension:      n.tipoPensionAplicada ?? "ONP",
      cantidadGuardias: n.cantidadGuardias ?? 0,
    });
  };

  const periodo = `${anio}-${mes}`;
  const hoy = new Date();

  const esPeriodoFuturo = (mesSeleccionado = mes, anioSeleccionado = anio) =>
    Number(anioSeleccionado) > hoy.getFullYear() ||
    (Number(anioSeleccionado) === hoy.getFullYear() && Number(mesSeleccionado) > hoy.getMonth() + 1);

  // Mes actual seleccionado → cálculo ilimitado (provisional)
  const esMesActual = Number(mes) === hoy.getMonth() + 1 && Number(anio) === hoy.getFullYear();

  // Mes anterior seleccionado Y hoy es día 1 → ventana de cierre final
  const fechaPrevia = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
  const esMesPrevioY1ro =
    Number(mes) === fechaPrevia.getMonth() + 1 &&
    Number(anio) === fechaPrevia.getFullYear() &&
    hoy.getDate() === 1;

  // En ventana de recálculo: mes actual O cierre del mes anterior
  const enVentanaRecalculo = esMesActual || esMesPrevioY1ro;

  // "Calcular" bloqueado si: futuro, O ya calculado y fuera de ventana
  const calcularBloqueado = esPeriodoFuturo() || (periodoCalculado && !enVentanaRecalculo);

  // "Cargar" bloqueado si aún no hay nómina calculada
  const cargarBloqueado = !periodoCalculado;

  const cambiarPeriodo = (campo, valor) => {
    if (campo === "mes") setMes(valor);
    else setAnio(valor);
    setResultado(null);
    setPeriodoCalculado(false);
    setMensaje("");
    setError("");
    setSelectedEmpleado(null);
  };

  const verificarPeriodo = useCallback(async (m, a) => {
    if (esPeriodoFuturo(m, a)) return;
    setVerificando(true);
    try {
      const data = await getNominasPorPeriodo(m, a);
      if (Array.isArray(data) && data.length > 0) {
        const byEmployee = {};
        for (const n of data) {
          if (!byEmployee[n.idEmpleado] || n.idNomina > byEmployee[n.idEmpleado].idNomina)
            byEmployee[n.idEmpleado] = n;
        }
        const deduped = Object.values(byEmployee)
          .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity));
        setResultado(deduped);
        setPeriodoCalculado(true);
        setMensaje(`Período ${a}-${m} ya calculado — mostrando ${deduped.length} registros.`);
      } else {
        setPeriodoCalculado(false);
        setResultado(null);
      }
    } catch {
      setPeriodoCalculado(false);
    } finally {
      setVerificando(false);
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    verificarPeriodo(mes, anio);
  }, [mes, anio]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleCalcular = async () => {
    setCalculando(true);
    setError("");
    setMensaje("");
    try {
      const data = await calcularNomina(mes, anio);
      if (Array.isArray(data)) {
        setResultado([...data].sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity)));
        setHistorial(h => {
          const entry = { ...data, periodo, timestamp: new Date().toISOString() };
          return [entry, ...h.filter(x => x.periodo !== periodo)].slice(0, 10);
        });
        setMensaje("Nómina calculada correctamente (" + data.length + " empleados)");
      } else {
        throw new Error(data?.message || "Respuesta inválida");
      }
    } catch (err) {
      setError(err || "Error al calcular la nómina. Verifique el período.");
    } finally {
      setCalculando(false);
    }
  };

  const handleCargarPeriodo = async () => {
    setLoading(true);
    setError("");
    setMensaje("");
    try {
      const data = await getNominasPorPeriodo(mes, anio);
      if (Array.isArray(data) && data.length > 0) {
        const byEmployee = {};
        for (const n of data) {
          if (!byEmployee[n.idEmpleado] || n.idNomina > byEmployee[n.idEmpleado].idNomina) {
            byEmployee[n.idEmpleado] = n;
          }
        }
        const deduped = Object.values(byEmployee)
          .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity));
        setResultado(deduped);
        setMensaje(`Se cargaron ${deduped.length} registros del período ${periodo}`);
      } else {
        setError(`No hay nóminas registradas para ${periodo}. Calcule primero.`);
      }
    } catch (err) {
      setError(err || "Error al cargar el período");
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarComprobante = async (idNomina) => {
    setEnviando(v => ({ ...v, [idNomina]: true }));
    setMensaje("");
    setError("");
    try {
      await enviarComprobante(idNomina);
      setMensaje(`Comprobante enviado correctamente (Nómina #${idNomina})`);
    } catch (err) {
      setError(err || "Error al enviar el comprobante");
    } finally {
      setEnviando(v => ({ ...v, [idNomina]: false }));
    }
  };

  const handleEnviarTodos = async () => {
    setModalConfirmarEnvio(false);
    setEnviandoTodos(true);
    setMensaje("");
    setError("");
    setResultadoEnvio(null);
    try {
      const resp = await enviarComprobantesPeriodo(periodo);
      setResultadoEnvio(resp);
      if (resp?.success === false && Number(resp?.fallidos || 0) > 0) {
        setError(resp.message || "Algunos comprobantes no pudieron enviarse.");
      } else {
        setMensaje(resp?.message || `Comprobantes enviados para ${periodo}.`);
      }
    } catch (err) {
      setError(err || "Error al enviar los comprobantes del período.");
    } finally {
      setEnviandoTodos(false);
    }
  };

  const handleDescargar = async (idNomina) => {
    try {
      const blob = await descargarComprobante(idNomina);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `comprobante-nomina-${idNomina}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setError("Error al descargar el comprobante.");
    }
  };

  const reemplazarNomina = (actualizada) => {
    setResultado(prev => {
      if (!prev) return prev;
      const lista = Array.isArray(prev) ? prev : [prev];
      const nuevaLista = lista.map(n => n.idNomina === actualizada.idNomina ? actualizada : n);
      return Array.isArray(prev) ? nuevaLista : nuevaLista[0];
    });
    setModalEstructura(actualizada);
    setSelectedEmpleado(actualizada);
  };

  const handleGuardarAjuste = async (calculos) => {
    if (!modalEstructura?.idNomina) return;
    setGuardandoAjuste(true);
    setMensaje("");
    setError("");
    try {
      const actualizada = await ajustarNomina(modalEstructura.idNomina, {
        sueldoBase: datosBase.sueldoBase,
        totalHorasTrabajadas: datosBase.horasTrabajadas,
        totalHorasExtra: datosBase.horasExtra,
        totalHorasNocturnas: datosBase.horasNocturnas,
        totalMinutosTardanza: datosBase.minutosLate,
        tieneHijos: datosBase.tieneHijos,
        cantidadHijos: datosBase.cantidadHijos,
        cantidadGuardias: datosBase.cantidadGuardias,
        tipoPensionAplicada: datosBase.tipoPension,
      });
      reemplazarNomina(actualizada);
      setDatosBase(p => ({
        ...p,
        sueldoBase: Number(actualizada.sueldoBase ?? p.sueldoBase),
        horasTrabajadas: Number(actualizada.totalHorasTrabajadas ?? p.horasTrabajadas),
        horasExtra: Number(actualizada.totalHorasExtra ?? p.horasExtra),
        horasNocturnas: Number(actualizada.totalHorasNocturnas ?? p.horasNocturnas),
        minutosLate: actualizada.totalMinutosTardanza ?? p.minutosLate,
        tieneHijos: actualizada.tieneHijos ?? p.tieneHijos,
        cantidadHijos: actualizada.cantidadHijos ?? p.cantidadHijos,
        cantidadGuardias: actualizada.cantidadGuardias ?? p.cantidadGuardias,
        tipoPension: actualizada.tipoPensionAplicada ?? p.tipoPension,
      }));
      setMensaje(`Nómina #${actualizada.idNomina} ajustada correctamente. Neto: ${fmtS(calculos.neto)}`);
    } catch {
      setError("Error al guardar el ajuste de nómina.");
    } finally {
      setGuardandoAjuste(false);
    }
  };

  const fmtS = (v) => v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const resultList = resultado
    ? (Array.isArray(resultado) ? [...resultado] : [resultado])
        .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity))
    : [];

  const totalNeto  = resultList.reduce((sum, n) => sum + Number(n.sueldoNeto ?? 0), 0);
  const totalBruto = resultList.reduce((sum, n) => sum + Number(n.sueldoBruto ?? n.sueldoBase ?? 0), 0);
  const cargandoResultados = loading || verificando || calculando;

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Cálculo de Nómina</span>
            <h1>Cálculo de Nómina</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Gestiona los pagos mensuales y descuentos de los empleados.
            </p>
          </div>
        </header>

        <div className="nomina-layout">

          {/* ── COLUMNA IZQUIERDA ── */}
          <div className="nomina-left">

            {selectedEmpleado && (() => {
              const se = selectedEmpleado;
              return (
                <div className="section-card" style={{ padding: 0, overflow: "hidden", marginBottom: 12, cursor: "pointer", border: "2px solid #2563eb" }}
                  onClick={() => abrirEstructura(se)}>
                  <div style={{ padding: "14px 18px", background: "linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)", color: "#fff", display: "flex", alignItems: "center", gap: 12 }}>
                    <div style={{ width: 36, height: 36, borderRadius: 8, background: "rgba(255,255,255,0.18)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, flexShrink: 0 }}>
                      ✏️
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 13, fontWeight: 700 }}>{se.nombreEmpleado ?? se.empleadoNombre ?? `Empleado #${se.idEmpleado}`}</div>
                      <div style={{ fontSize: 11, opacity: 0.72, marginTop: 1 }}>Nómina #{se.idNomina} · Haz clic para modificar</div>
                    </div>
                    <button onClick={e => { e.stopPropagation(); setSelectedEmpleado(null); }}
                      style={{ background: "rgba(255,255,255,0.18)", border: "none", color: "#fff", width: 26, height: 26, borderRadius: 6, cursor: "pointer", fontSize: 14, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                      ×
                    </button>
                  </div>
                  <div style={{ padding: "12px 18px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 8, fontSize: 12, background: "#f8fafc" }}>
                    <div><span style={{ color: "var(--text-3)" }}>Sueldo base</span><br /><strong>{fmtS(se.sueldoContrato ?? se.sueldoBase)}</strong></div>
                    <div><span style={{ color: "var(--text-3)" }}>Sueldo bruto</span><br /><strong style={{ color: "#16a34a" }}>{fmtS(se.sueldoBruto)}</strong></div>
                    <div><span style={{ color: "var(--text-3)" }}>Sueldo neto</span><br /><strong style={{ color: "#2563eb" }}>{fmtS(se.sueldoNeto)}</strong></div>
                  </div>
                </div>
              );
            })()}

            <ApiAlert type="success" message={mensaje} />
            <ApiAlert type="error" message={error} />

            {resultadoEnvio && (
              <div className="section-card" style={{ padding: "14px 18px", marginBottom: 16 }}>
                <div style={{ display: "flex", gap: 16, fontSize: 13, marginBottom: resultadoEnvio.errores?.length ? 10 : 0 }}>
                  <span><strong>Total:</strong> {resultadoEnvio.total ?? "—"}</span>
                  <span style={{ color: "#16a34a" }}><strong>✓ Enviados:</strong> {resultadoEnvio.enviados ?? "—"}</span>
                  <span style={{ color: "#dc2626" }}><strong>✗ Fallidos:</strong> {resultadoEnvio.fallidos ?? 0}</span>
                </div>
                {resultadoEnvio.errores?.length > 0 && (
                  <table style={{ width: "100%", fontSize: 12, borderCollapse: "collapse" }}>
                    <thead>
                      <tr style={{ textAlign: "left", color: "var(--text-3)" }}>
                        <th style={{ padding: "4px 8px 4px 0" }}>Empleado</th>
                        <th style={{ padding: "4px 0" }}>Motivo del fallo</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultadoEnvio.errores.map((e, i) => (
                        <tr key={i} style={{ borderTop: "1px solid var(--border)" }}>
                          <td style={{ padding: "4px 8px 4px 0", fontWeight: 600 }}>{e.empleado}</td>
                          <td style={{ padding: "4px 0", color: "#dc2626" }}>{e.message}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            )}

            <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)", background: "linear-gradient(135deg, #f0fdf4 0%, #eff6ff 100%)", display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "#22c55e", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20, flexShrink: 0 }}>
                  📅
                </div>
                <div>
                  <h2 style={{ margin: 0, fontSize: 16 }}>Configuración del Período</h2>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-3)" }}>Seleccione el mes y año para calcular la nómina</p>
                </div>
              </div>

              <div style={{ padding: "16px 20px 0" }}>
                <div className="form-grid" style={{ gap: 12, marginBottom: 12 }}>
                  <div className="form-group">
                    <label>Mes</label>
                    <select value={mes} onChange={e => cambiarPeriodo("mes", e.target.value)}>
                      {MESES.map(m => <option key={m.v} value={m.v} disabled={esPeriodoFuturo(m.v, anio)}>{m.l}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Año</label>
                    <select value={anio} onChange={e => cambiarPeriodo("anio", e.target.value)}>
                      {ANIOS.map(a => <option key={a} value={a} disabled={esPeriodoFuturo(mes, a)}>{a}</option>)}
                    </select>
                  </div>
                </div>

                <div style={{ background: "linear-gradient(90deg, #eff6ff 0%, #f0fdf4 100%)", border: "1px solid #bfdbfe", borderRadius: 10, padding: "10px 14px", marginBottom: 14, display: "flex", alignItems: "center", gap: 10 }}>
                  <span style={{ fontSize: 20 }}>📆</span>
                  <div>
                    <div style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em" }}>Período seleccionado</div>
                    <div style={{ fontSize: 15, fontWeight: 700, color: "var(--primary)" }}>{MESES.find(m => m.v === mes)?.l} {anio}</div>
                  </div>
                </div>
              </div>

              {/* Aviso contextual según estado del período */}
              {periodoCalculado && !enVentanaRecalculo && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#fef9c3", border: "1px solid #fde047",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>🔒</span>
                  <span>Este período <strong>ya fue calculado y cerrado</strong>. Use "Cargar Período" para ver los datos.</span>
                </div>
              )}
              {esMesPrevioY1ro && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#fef3c7", border: "1px solid #f59e0b",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>⚠️</span>
                  <span><strong>Ventana de cierre:</strong> hoy es el último día para recalcular la nómina de este período.</span>
                </div>
              )}
              {esMesActual && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#eff6ff", border: "1px solid #93c5fd",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>📊</span>
                  <span>Mes en curso — puede calcular nómina provisional sin límite.</span>
                </div>
              )}

              <div style={{ padding: "0 20px 20px", display: "flex", gap: 10 }}>
                <button
                  className="btn btn-success"
                  onClick={handleCalcular}
                  disabled={calculando || verificando || calcularBloqueado}
                  title={calcularBloqueado && !esPeriodoFuturo() ? "Período cerrado. Use Cargar Período para ver los datos." : ""}
                  style={{ flex: 1, opacity: calcularBloqueado ? 0.45 : 1, cursor: calcularBloqueado ? "not-allowed" : "pointer" }}
                >
                  {calculando ? <><span className="btn-spinner" />Calculando...</> : calcularBloqueado ? "🔒 Cálculo bloqueado" : "⚙️ Calcular Nómina"}
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={handleCargarPeriodo}
                  disabled={loading || verificando || cargarBloqueado}
                  title={cargarBloqueado ? "Calcule la nómina primero para poder cargarla." : ""}
                  style={{ flex: 1, opacity: cargarBloqueado ? 0.45 : 1, cursor: cargarBloqueado ? "not-allowed" : "pointer" }}
                >
                  {loading || verificando ? <><span className="btn-spinner" />Cargando...</> : cargarBloqueado ? "📋 Sin datos aún" : "📋 Cargar Período"}
                </button>
              </div>
            </div>

            {resultList.length > 0 && (
              <div className="section-card nomina-results-card" style={{ padding: 0, overflow: "hidden" }}>
                <div className="nomina-results-header">
                  <h2 style={{ margin: 0 }}>
                    Resultados — {MESES.find(m => m.v === mes)?.l} {anio}
                    <span className="badge badge-success" style={{ marginLeft: "10px", fontSize: "12px" }}>
                      {resultList.length} empleado(s)
                    </span>
                  </h2>
                  <button className="btn btn-primary btn-sm" onClick={() => setModalConfirmarEnvio(true)} disabled={enviandoTodos || resultList.length === 0}>
                    {enviandoTodos ? <><span className="btn-spinner" />Enviando...</> : "Enviar todos"}
                  </button>
                </div>
                <div className="table-wrapper nomina-results-table-wrap" style={{ marginBottom: 0 }}>
                  <table className="nomina-results-table">
                    <thead>
                      <tr>
                        <th>Empleado</th>
                        <th>Sueldo Base</th>
                        <th>Bonificaciones</th>
                        <th>Descuentos</th>
                        <th>Sueldo Neto</th>
                        <th>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultList.map((n, i) => (
                        <tr key={n.idNomina ?? i}
                          onClick={() => setSelectedEmpleado(n)}
                          style={{ cursor: "pointer" }}
                          className={selectedEmpleado?.idNomina === n.idNomina ? "tr-selected" : ""}>
                          <td>
                            <strong>{n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? (n.idEmpleado ? `Emp. #${n.idEmpleado}` : "—")}</strong>
                          </td>
                          <td>{fmtS(n.sueldoContrato ?? n.sueldoBase ?? n.sueldo)}</td>
                          <td style={{ color: "var(--success)" }}>{fmtS(getBonif(n))}</td>
                          <td style={{ color: "#ef4444" }}>{fmtS(getDesc(n))}</td>
                          <td style={{ fontWeight: 700, color: "#0f172a" }}>{fmtS(n.sueldoNeto)}</td>
                          <td>
                            <div className="nomina-action-buttons">
                              <button className="btn btn-secondary btn-sm"
                                title="Vista previa detallada"
                                onClick={e => { e.stopPropagation(); setPreviewNomina(n); }}>
                                Ver
                              </button>
                              <button className="btn btn-secondary btn-sm"
                                onClick={e => { e.stopPropagation(); handleDescargar(n.idNomina); }}>
                                PDF
                              </button>
                              <button className="btn btn-primary btn-sm"
                                onClick={e => { e.stopPropagation(); handleEnviarComprobante(n.idNomina); }}
                                disabled={enviando[n.idNomina]}>
                                {enviando[n.idNomina] ? <span className="btn-spinner" /> : "Email"}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 600 }}>
                        <td colSpan={1} style={{ padding: "10px 12px", fontSize: "13px" }}>
                          TOTALES ({resultList.length} empleados)
                        </td>
                        <td style={{ padding: "10px 12px" }}>{fmtS(totalBruto)}</td>
                        <td></td>
                        <td></td>
                        <td style={{ padding: "10px 12px", color: "var(--primary)" }}>{fmtS(totalNeto)}</td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            )}

            {resultList.length === 0 && (
              <div className="section-card">
                {cargandoResultados ? (
                  <Spinner text={calculando ? "Calculando nómina..." : "Cargando nómina..."} />
                ) : (
                  <div className="empty-state">
                    <div className="empty-state-icon">📊</div>
                    <p>Seleccione un período y calcule o cargue la nómina para ver los resultados.</p>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* ── COLUMNA DERECHA ── */}
          <div className="nomina-right">
            <div className="section-card">
              <h2>Estado del Período</h2>
              <div className="nomina-status-item">
                <span>Período actual:</span>
                <strong>{MESES.find(m => m.v === mes)?.l} {anio}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total empleados:</span>
                <strong>{resultList.length > 0 ? resultList.length : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total neto a pagar:</span>
                <strong style={{ color: "var(--primary)" }}>{resultList.length > 0 ? fmtS(totalNeto) : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Estado:</span>
                <span className={`badge ${resultado ? "badge-success" : "badge-gray"}`}>
                  {resultado ? "Calculado" : "Pendiente"}
                </span>
              </div>
            </div>

            {historial.length > 0 && (
              <div className="section-card">
                <h2>Historial Reciente</h2>
                {historial.map((h, i) => (
                  <div key={i} className="nomina-hist-item">
                    <span className="nomina-hist-periodo">{h.periodo}</span>
                    <span className="badge badge-success" style={{ fontSize: "11px" }}>Calculado</span>
                  </div>
                ))}
              </div>
            )}

            <div className="section-card">
              <h2>Guía Rápida</h2>
              <ol style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "var(--text-2)", lineHeight: "1.9" }}>
                <li>Seleccione mes y año del período.</li>
                <li>Presione <strong>Calcular Nómina</strong> para procesar.</li>
                <li>Revise los resultados en la tabla.</li>
                <li>Descargue comprobantes, envíelos uno por uno o use el envío masivo del período.</li>
              </ol>
            </div>
          </div>

        </div>
      </main>

      {/* ── MODAL CONFIRMAR ENVÍO MASIVO ── */}
      {modalConfirmarEnvio && (() => {
        const n = resultList.length;
        const segMin = n * 2;
        const segMax = n * 4;
        const fmtTiempo = (s) => s >= 60 ? `${Math.ceil(s / 60)} min` : `${s} seg`;
        return (
          <div className="modal-overlay">
            <div className="modal-box" style={{ maxWidth: 440 }}>
              <div className="modal-header">
                <h3>📧 Enviar comprobantes por correo</h3>
                <button className="modal-close" onClick={() => setModalConfirmarEnvio(false)}>×</button>
              </div>
              <div className="modal-body">
                <p>Se enviarán <strong>{n} comprobante(s)</strong> de nómina al correo institucional (o personal, si no tiene) de cada empleado.</p>
                <div style={{ background: "#fef3c7", border: "1px solid #f59e0b", borderRadius: 8, padding: "10px 14px", fontSize: 13, marginTop: 10 }}>
                  ⏱️ El envío es secuencial (uno por uno), esto puede tardar aproximadamente <strong>{fmtTiempo(segMin)} a {fmtTiempo(segMax)}</strong>.
                  No cierre ni recargue esta ventana mientras dice "Enviando...".
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalConfirmarEnvio(false)}>Cancelar</button>
                <button className="btn btn-primary" onClick={handleEnviarTodos}>Confirmar envío</button>
              </div>
            </div>
          </div>
        );
      })()}

      {/* ── MODAL VISTA PREVIA NÓMINA ── */}
      {previewNomina && (() => {
        const n = previewNomina;
        const base  = Number(n.sueldoContrato ?? n.sueldoBase ?? n.sueldo ?? 0);
        const bonifs = [
          { label: "Bonif. Familiar",       val: n.bonifFamiliar },
          { label: "Bonif. Turno Nocturno", val: n.bonifTurnoNocturno },
          { label: "Bonif. Guardia",        val: n.bonifGuardia },
          { label: "Bonif. Riesgo",         val: n.bonifRiesgo },
        ];
        const descs = [
          { label: "Descuento Tardanzas", val: n.descuentoTardanzas },
          { label: "Descuento de Ley",    val: n.descuentoLey },
        ];
        const totalBonif = getBonif(n);
        const totalDesc  = getDesc(n);
        const neto       = Number(n.sueldoNeto ?? 0);
        const mesLabel   = MESES.find(m => m.v === (n.mes ?? mes))?.l ?? n.mes ?? mes;
        return (
          <div className="modal-overlay">
            <div className="modal-box" style={{ maxWidth: 500 }}>
              <div className="modal-header">
                <div>
                  <h3>📊 Vista Previa — Nómina #{n.idNomina}</h3>
                  <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13 }}>
                    {n.nombreEmpleado ?? n.empleadoNombre ?? `Empleado #${n.idEmpleado}`} — {mesLabel} {n.anio ?? anio}
                  </p>
                </div>
                <button className="modal-close" onClick={() => setPreviewNomina(null)}>×</button>
              </div>
              <div className="modal-body" style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                <div style={{ display: "flex", justifyContent: "space-between", padding: "10px 0", borderBottom: "1px solid var(--border)" }}>
                  <span style={{ fontWeight: 600 }}>Sueldo Base</span>
                  <strong>{fmtS(base)}</strong>
                </div>
                <div>
                  <div style={{ fontWeight: 700, color: "#16a34a", marginBottom: 8, fontSize: 13 }}>+ Bonificaciones</div>
                  {bonifs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#16a34a" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Bonificaciones</span>
                    <span style={{ color: "#16a34a" }}>{fmtS(totalBonif)}</span>
                  </div>
                </div>
                <div>
                  <div style={{ fontWeight: 700, color: "#ef4444", marginBottom: 8, fontSize: 13 }}>— Descuentos</div>
                  {descs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#ef4444" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Descuentos</span>
                    <span style={{ color: "#ef4444" }}>{fmtS(totalDesc)}</span>
                  </div>
                </div>
                <div style={{ background: "#f0fdf4", borderRadius: 10, padding: "12px 16px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontWeight: 700, fontSize: 15 }}>Sueldo Neto a Pagar</span>
                  <span style={{ fontWeight: 800, fontSize: 18, color: "#15803d" }}>{fmtS(neto)}</span>
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setPreviewNomina(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        );
      })()}

      {/* ── MODAL ESTRUCTURA SALARIAL ── */}
      {modalEstructura && (() => {
        const n   = modalEstructura;
        const db  = datosBase;
        const fmt = (v) => `S/ ${Number(v || 0).toLocaleString("es-PE", { minimumFractionDigits: 2 })}`;
        const mesLabel = MESES.find(m => m.v === mes)?.l ?? mes;

        const esClinico  = n.esEmpleadoClinico  === true;
        const esNocturno = n.tieneHorarioNocturno === true;

        const valorHora = db.sueldoBase > 0 ? db.sueldoBase / 30 / 8 : 0;
        const valorMin  = valorHora / 60;
        const horasDobles = Math.max(0, db.horasExtra - 2);
        const horasExtraBase = Math.max(0, Math.min(db.horasExtra, 2));
        const montoHorasExtra = (horasExtraBase * valorHora * 1.5) + (horasDobles * valorHora * 2);
        const bFam    = db.tieneHijos && db.cantidadHijos > 0 ? db.sueldoBase * 0.10 : 0;
        const bNoct   = esNocturno ? db.sueldoBase * 0.35 : 0;
        const bGuard  = esClinico  ? db.cantidadGuardias * 50 : 0;
        const bRiesgo = esClinico  ? db.sueldoBase * 0.10 : 0;
        const descTard = valorMin * db.minutosLate;
        const bruto   = db.sueldoBase + montoHorasExtra + bFam + bNoct + bGuard + bRiesgo;
        const tasa    = db.tipoPension === "AFP" ? 0.12 : 0.13;
        const descLey = bruto * tasa;
        const neto    = Math.max(0, bruto - descTard - descLey);
        // bCargo eliminado — no corresponde a concepto legal peruano

        const r1 = (v) => Math.round(Number(v ?? 0) * 10) / 10;
        const resetearValores = () => setDatosBase({
          sueldoBase:       Number(n.sueldoBase ?? 0),
          horasTrabajadas:  r1(n.totalHorasTrabajadas),
          horasExtra:       r1(n.totalHorasExtra),
          horasNocturnas:   r1(n.totalHorasNocturnas),
          minutosLate:      n.totalMinutosTardanza ?? 0,
          tieneHijos:       n.tieneHijos ?? (Number(n.cantidadHijos) > 0),
          cantidadHijos:    n.cantidadHijos ?? 0,
          tipoPension:      n.tipoPensionAplicada ?? "ONP",
          cantidadGuardias: n.cantidadGuardias ?? 0,
        });

        const InputField = ({ label, field, step = "0.01", parse, disabled }) => (
          <div style={{ marginBottom: 10 }}>
            <label style={{ fontSize: 10, fontWeight: 700, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.06em", display: "block", marginBottom: 3 }}>{label}</label>
            <input
              type="number" min="0" step={step}
              value={db[field]}
              onChange={e => setDatosBase(p => ({ ...p, [field]: parse ? parse(e.target.value) : Number(e.target.value) }))}
              disabled={disabled}
              style={{ width: "100%", padding: "7px 10px", border: "1px solid #d1d5db", borderRadius: 8, fontSize: 13, background: disabled ? "#f3f4f6" : "#fff", color: disabled ? "#9ca3af" : "#111827", boxSizing: "border-box", outline: "none" }}
            />
          </div>
        );

        const ToggleGroup = ({ label, field, options }) => (
          <div style={{ marginBottom: 10 }}>
            <label style={{ fontSize: 10, fontWeight: 700, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.06em", display: "block", marginBottom: 3 }}>{label}</label>
            <div style={{ display: "flex", gap: 6 }}>
              {options.map(({ val, label: lbl }) => (
                <button key={String(val)} onClick={() => setDatosBase(p => ({ ...p, [field]: val }))}
                  style={{ flex: 1, padding: "7px 0", border: `1.5px solid ${db[field] === val ? "#2563eb" : "#d1d5db"}`, borderRadius: 8, background: db[field] === val ? "#eff6ff" : "#fff", color: db[field] === val ? "#2563eb" : "#6b7280", fontWeight: 600, fontSize: 13, cursor: "pointer" }}>
                  {lbl}
                </button>
              ))}
            </div>
          </div>
        );

        const CalcRow = ({ label, formula, val, tipo = "bonif" }) => {
          const isDesc    = tipo === "desc";
          const isBlocked = tipo === "blocked";
          if (isBlocked) return (
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 11px", background: "#f3f4f6", borderLeft: "3px solid #d1d5db", borderRadius: 8, marginBottom: 5, opacity: 0.6 }}>
              <div>
                <div style={{ fontSize: 12, fontWeight: 600, color: "#9ca3af", display: "flex", alignItems: "center", gap: 5 }}>
                  <span>🔒</span> {label}
                </div>
                <div style={{ fontSize: 10, color: "#c0c4cc", marginTop: 1 }}>{formula}</div>
              </div>
              <span style={{ fontWeight: 700, fontSize: 13, color: "#c0c4cc", flexShrink: 0, marginLeft: 8 }}>No aplica</span>
            </div>
          );
          return (
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 11px", background: isDesc ? "#fff5f5" : "#f0fdf4", borderLeft: `3px solid ${isDesc ? "#dc2626" : "#16a34a"}`, borderRadius: 8, marginBottom: 5 }}>
              <div>
                <div style={{ fontSize: 12, fontWeight: 600, color: isDesc ? "#7f1d1d" : "#14532d" }}>{label}</div>
                <div style={{ fontSize: 10, color: "#9ca3af", marginTop: 1 }}>{formula}</div>
              </div>
              <span style={{ fontWeight: 700, fontSize: 13, color: isDesc ? "#dc2626" : "#16a34a", flexShrink: 0, marginLeft: 8 }}>
                {isDesc ? "−" : "+"} {fmt(val)}
              </span>
            </div>
          );
        };

        return (
          <div className="modal-overlay" style={{ zIndex: 1100 }}
            onClick={e => e.target === e.currentTarget && setModalEstructura(null)}>
            <div className="modal-box" style={{ maxWidth: 780, padding: 0, overflow: "hidden", borderRadius: 16 }}>

              {/* Header */}
              <div style={{ background: "linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)", color: "#fff", padding: "18px 26px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                  <div>
                    <div style={{ fontSize: 10, textTransform: "uppercase", letterSpacing: "0.12em", opacity: 0.6, marginBottom: 4 }}>Estructura Salarial · Simulador</div>
                    <h3 style={{ margin: 0, fontSize: 17, fontWeight: 700 }}>{n.nombreEmpleado ?? `Empleado #${n.idEmpleado}`}</h3>
                    <div style={{ marginTop: 3, opacity: 0.72, fontSize: 12 }}>Período: {mesLabel} {anio} · Nómina #{n.idNomina}</div>
                  </div>
                  <button onClick={() => setModalEstructura(null)} style={{ background: "rgba(255,255,255,0.18)", border: "none", color: "#fff", width: 30, height: 30, borderRadius: 7, cursor: "pointer", fontSize: 17, display: "flex", alignItems: "center", justifyContent: "center" }}>×</button>
                </div>
              </div>

              {/* Body: two columns */}
              <div className="nomina-modal-body">

                {/* LEFT: Datos ingresados (editable) */}
                <div style={{ padding: "18px 20px", borderRight: "1px solid #e5e7eb", overflowY: "auto", background: "#fff" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 14 }}>
                    <div style={{ width: 3, height: 14, background: "#2563eb", borderRadius: 2 }} />
                    <span style={{ fontWeight: 700, fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em", color: "#2563eb" }}>Datos Ingresados / Registrados</span>
                  </div>

                  <InputField label="Sueldo Devengado (S/)" field="sueldoBase" step="1" />
                  <InputField label="Horas Trabajadas" field="horasTrabajadas" step="0.5" />
                  <InputField label="Horas Extra" field="horasExtra" step="0.5" />
                  <InputField label="Horas Nocturnas" field="horasNocturnas" step="0.5" disabled={!esNocturno} />
                  <InputField label="Tardanzas (minutos)" field="minutosLate" step="1" parse={v => Math.max(0, parseInt(v) || 0)} />
                  <InputField label="Cantidad de Guardias" field="cantidadGuardias" step="1" parse={v => Math.max(0, parseInt(v) || 0)} disabled={!esClinico} />

                  <ToggleGroup label="¿Tiene Hijos?" field="tieneHijos"
                    options={[
                      { val: true,  label: "Sí" },
                      { val: false, label: "No" },
                    ]}
                  />

                  <div style={{ marginBottom: 10 }}>
                    <label style={{ fontSize: 10, fontWeight: 700, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.06em", display: "block", marginBottom: 3 }}>Cantidad de Hijos</label>
                    <input type="number" min="0" step="1"
                      value={db.cantidadHijos}
                      disabled={!db.tieneHijos}
                      onChange={e => setDatosBase(p => ({ ...p, cantidadHijos: Math.max(0, parseInt(e.target.value) || 0) }))}
                      style={{ width: "100%", padding: "7px 10px", border: "1px solid #d1d5db", borderRadius: 8, fontSize: 13, background: !db.tieneHijos ? "#f3f4f6" : "#fff", color: !db.tieneHijos ? "#9ca3af" : "#111827", boxSizing: "border-box" }}
                    />
                  </div>

                  <ToggleGroup label="Tipo de Pensión" field="tipoPension"
                    options={[
                      { val: "ONP", label: "ONP  (13%)" },
                      { val: "AFP", label: "AFP  (12%)" },
                    ]}
                  />

                  <button onClick={resetearValores}
                    style={{ width: "100%", marginTop: 6, padding: "7px 0", border: "1px solid #d1d5db", borderRadius: 8, background: "#f9fafb", color: "#6b7280", fontWeight: 600, fontSize: 11, cursor: "pointer" }}>
                    ↺ Restablecer valores originales
                  </button>
                </div>

                {/* RIGHT: Cálculos automáticos (live) */}
                <div style={{ padding: "18px 20px", overflowY: "auto", background: "#f8fafc" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 14 }}>
                    <div style={{ width: 3, height: 14, background: "#16a34a", borderRadius: 2 }} />
                    <span style={{ fontWeight: 700, fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em", color: "#16a34a" }}>Cálculos Automáticos</span>
                  </div>

                  <div style={{ fontSize: 9, fontWeight: 700, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6 }}>Bonificaciones</div>
                  <CalcRow label="Horas Extra"
                    formula={`${db.horasExtra}h con 1.5x hasta 2h y 2x restante`}
                    val={montoHorasExtra} />
                  <CalcRow label="Bonif. Familiar"
                    formula={db.tieneHijos && db.cantidadHijos > 0
                      ? `10% aplicado una vez por ${db.cantidadHijos} hijo(s) elegible(s)`
                      : "No aplica (sin hijos declarados)"}
                    val={bFam} />
                  {esNocturno
                    ? <CalcRow label="Bonif. Nocturna" formula={`35% × ${fmt(db.sueldoBase)} (turno nocturno)`} val={bNoct} />
                    : <CalcRow label="Bonif. Nocturna" formula="Solo para empleados con turno nocturno (≥ 19:00)" tipo="blocked" />}
                  {esClinico
                    ? <CalcRow label="Bonif. Guardia" formula={`${db.cantidadGuardias} guardia(s) × S/ 50.00`} val={bGuard} />
                    : <CalcRow label="Bonif. Guardia" formula="Solo para departamentos clínicos" tipo="blocked" />}
                  {esClinico
                    ? <CalcRow label="Bonif. Riesgo" formula={`10% × ${fmt(db.sueldoBase)}`} val={bRiesgo} />
                    : <CalcRow label="Bonif. Riesgo" formula="Solo para departamentos clínicos" tipo="blocked" />}

                  <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 11px", background: "#dcfce7", borderRadius: 8, margin: "8px 0 12px", fontWeight: 700, fontSize: 12 }}>
                    <span style={{ color: "#15803d" }}>Total Bruto</span>
                    <span style={{ color: "#15803d" }}>{fmt(bruto)}</span>
                  </div>

                  <div style={{ fontSize: 9, fontWeight: 700, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6 }}>Descuentos</div>
                  <CalcRow label={`Desc. de Ley (${db.tipoPension})`}
                    formula={`${(tasa * 100).toFixed(0)}% × ${fmt(bruto)} (bruto)`}
                    val={descLey} tipo="desc" />
                  <CalcRow label="Desc. Tardanzas"
                    formula={`${db.minutosLate} min × ${fmt(valorMin)}/min`}
                    val={descTard} tipo="desc" />

                  <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 11px", background: "#fee2e2", borderRadius: 8, marginTop: 8, fontWeight: 700, fontSize: 12 }}>
                    <span style={{ color: "#b91c1c" }}>Total Descuentos</span>
                    <span style={{ color: "#b91c1c" }}>− {fmt(descTard + descLey)}</span>
                  </div>

                  <div style={{ background: "linear-gradient(135deg, #1e40af 0%, #2563eb 100%)", borderRadius: 12, padding: "15px 18px", marginTop: 12, display: "flex", justifyContent: "space-between", alignItems: "center", boxShadow: "0 4px 14px rgba(37,99,235,0.25)" }}>
                    <div>
                      <div style={{ color: "rgba(255,255,255,0.7)", fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em" }}>Sueldo Neto</div>
                      <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 10, marginTop: 3 }}>{fmt(bruto)} − {fmt(descTard + descLey)}</div>
                    </div>
                    <div style={{ color: "#fff", fontWeight: 800, fontSize: 26, lineHeight: 1 }}>{fmt(neto)}</div>
                  </div>
                </div>
              </div>

              {/* Footer */}
              <div style={{ padding: "12px 24px", borderTop: "1px solid #e5e7eb", display: "flex", justifyContent: "flex-end", background: "#fff" }}>
                <button className="btn btn-primary" onClick={() => handleGuardarAjuste({ neto })} disabled={guardandoAjuste} style={{ marginRight: 10 }}>
                  {guardandoAjuste ? <><span className="btn-spinner" />Guardando...</> : "Guardar cambios"}
                </button>
                <button className="btn btn-secondary" onClick={() => setModalEstructura(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
