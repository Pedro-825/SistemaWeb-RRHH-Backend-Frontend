import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import logo from "../assets/logo.png";
import "../styles/Dashboard.css";
import { getNominasPorEmpleado, getIdEmpleado } from "../services/api";
import { abrirComprobante } from "../services/comprobanteHtml";

const getBonif = (n) =>
  (Number(n.bonifFamiliar)      || 0) +
  (Number(n.bonifTurnoNocturno) || 0) +
  (Number(n.bonifGuardia)       || 0) +
  (Number(n.bonifRiesgo)        || 0) +
  (Number(n.bonifCargo)         || 0);

const getDesc = (n) =>
  (Number(n.descuentoTardanzas) || 0) +
  (Number(n.descuentoLey)       || 0);

export default function NominaEmpleado() {
  const idEmpleado = getIdEmpleado();
  const [nominas,  setNominas]  = useState([]);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState("");
  const [mensaje,  setMensaje]  = useState("");
  useEffect(() => {
    if (idEmpleado) cargarNominas();
    else setError("No se pudo obtener tu ID de empleado. Contacta a RRHH.");
  }, [idEmpleado]);

  const cargarNominas = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getNominasPorEmpleado(idEmpleado);
      setNominas(Array.isArray(data) ? data : []);
    } catch {
      setError("No se pudo cargar tu historial de nómina.");
    } finally {
      setLoading(false);
    }
  };

  const handleDescargar = (idNomina) => {
    const n = nominas.find(x => String(x.idNomina ?? x.id) === String(idNomina));
    if (n) {
      const logoUrl = window.location.origin + logo;
      abrirComprobante(n, logoUrl, "#0891b2", undefined, "Sistema de Recursos Humanos — Boleta de Pago");
    } else {
      setError("No se encontró la nómina. Recargue la página e intente de nuevo.");
    }
  };

  const fmtS = (v) =>
    v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const fmtPeriodo = (mes, anio) => {
    const MESES = ["Enero","Febrero","Marzo","Abril","Mayo","Junio",
                   "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];
    const m = parseInt(mes);
    return `${isNaN(m) ? mes : MESES[m - 1]} ${anio}`;
  };

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">
        <header className="main-header header-empleado">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Mi Nómina</span>
            <h1>Mi Nómina</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Historial de pagos y comprobantes de nómina.
            </p>
          </div>
          <span className="role-pill role-pill-empleado">Empleado</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div style={{ padding: "16px 20px 12px", borderBottom: "1px solid var(--border)" }}>
            <h2 style={{ margin: 0 }}>Historial de Pagos</h2>
          </div>

          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? (
              <div className="loading-text">Cargando tu historial de nómina...</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Período</th>
                    <th>Sueldo Base</th>
                    <th>Bonificaciones</th>
                    <th>Descuentos</th>
                    <th>Sueldo Neto</th>
                    <th>Comprobante</th>
                  </tr>
                </thead>
                <tbody>
                  {nominas.length === 0 ? (
                    <tr>
                      <td colSpan={7}>
                        <div className="empty-state">
                          <div className="empty-state-icon">💲</div>
                          <p>No hay registros de nómina disponibles</p>
                        </div>
                      </td>
                    </tr>
                  ) : nominas.map((n, i) => (
                    <tr key={n.idNomina ?? i}>
                      <td style={{ color: "var(--text-3)", fontSize: "13px" }}>{n.idNomina ?? i + 1}</td>
                      <td style={{ fontWeight: 600 }}>
                        {n.mes && n.anio
                          ? fmtPeriodo(n.mes, n.anio)
                          : n.periodo ?? "—"}
                      </td>
                      <td>{fmtS(n.sueldoBase ?? n.sueldo)}</td>
                      <td style={{ color: "var(--success)" }}>
                        {fmtS(getBonif(n))}
                      </td>
                      <td style={{ color: "#ef4444" }}>
                        {fmtS(getDesc(n))}
                      </td>
                      <td style={{ fontWeight: 700, color: "#0f172a" }}>
                        {fmtS(n.sueldoNeto)}
                      </td>
                      <td>
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => handleDescargar(n.idNomina)}
                          disabled={!n.idNomina}
                        >
                          ⬇ PDF
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {nominas.length > 0 && (
            <div className="table-footer-bar">
              <span>ℹ️ Haga clic en ⬇ PDF para descargar el comprobante de cada período.</span>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
