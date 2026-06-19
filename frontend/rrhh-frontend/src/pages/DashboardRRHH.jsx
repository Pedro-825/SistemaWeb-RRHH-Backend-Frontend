import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  buscarEmpleadosAvanzado,
  getSolicitudesPorEstado,
  listarJustificacionesPendientes,
} from "../services/api";

const toCount = (data) => {
  if (Array.isArray(data)) return data.length;
  if (data?.totalElements != null) return data.totalElements;
  if (Array.isArray(data?.content)) return data.content.length;
  return 0;
};

export default function DashboardRRHH() {
  const [stats, setStats] = useState({
    empleadosActivos:     null,
    solicitudesPendientes: null,
    tardanzas:            null,
    incidencias:          null,
  });

  useEffect(() => {
    buscarEmpleadosAvanzado({ estado: "ACTIVO" })
      .then(data => setStats(s => ({ ...s, empleadosActivos: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, empleadosActivos: "—" })));

    getSolicitudesPorEstado("PENDIENTE")
      .then(data => setStats(s => ({ ...s, solicitudesPendientes: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, solicitudesPendientes: "—" })));

    listarJustificacionesPendientes()
      .then(data => setStats(s => ({ ...s, tardanzas: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, tardanzas: "—" })));

    getSolicitudesPorEstado("EN_REVISION")
      .then(data => setStats(s => ({ ...s, incidencias: toCount(data) })))
      .catch(() => setStats(s => ({ ...s, incidencias: "—" })));
  }, []);

  const fmt = (v) => v === null ? "..." : v;

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">Panel Principal</span>
            <h1>Bienvenido, RRHH</h1>
          </div>
          <span className="role-pill role-pill-rrhh">Recursos Humanos</span>
        </header>

        <div className="stats-row">
          <div className="stat-box accent-blue">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <div className="stat-value">{fmt(stats.empleadosActivos)}</div>
              <div className="stat-label">Empleados Activos</div>
            </div>
          </div>
          <div className="stat-box accent-orange">
            <div className="stat-icon">📅</div>
            <div className="stat-info">
              <div className="stat-value">{fmt(stats.solicitudesPendientes)}</div>
              <div className="stat-label">Solicitudes Pendientes</div>
            </div>
          </div>
          <div className="stat-box accent-red">
            <div className="stat-icon">⏰</div>
            <div className="stat-info">
              <div className="stat-value">{fmt(stats.tardanzas)}</div>
              <div className="stat-label">Tardanzas / Justif. Pendientes</div>
            </div>
          </div>
          <div className="stat-box accent-purple">
            <div className="stat-icon">⚠️</div>
            <div className="stat-info">
              <div className="stat-value">{fmt(stats.incidencias)}</div>
              <div className="stat-label">En Revisión (Gerencia)</div>
            </div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/rrhh/empleados" className="card">
            <span className="card-icon">👥</span>
            <h3>Gestión de Empleados</h3>
            <p>Registrar, editar y administrar empleados</p>
          </Link>
          <Link to="/rrhh/asistencia" className="card">
            <span className="card-icon">📋</span>
            <h3>Control de Asistencia</h3>
            <p>Gestionar correcciones y justificaciones</p>
          </Link>
          <Link to="/rrhh/nomina" className="card">
            <span className="card-icon">💰</span>
            <h3>Cálculo de Nómina</h3>
            <p>Procesar y detallar pagos del personal</p>
          </Link>
          <Link to="/rrhh/solicitudes" className="card">
            <span className="card-icon">📅</span>
            <h3>Solicitudes</h3>
            <p>Validar permisos y vacaciones</p>
          </Link>
          <Link to="/rrhh/reportes" className="card">
            <span className="card-icon">📊</span>
            <h3>Reportes</h3>
            <p>Generar y exportar reportes del sistema</p>
          </Link>
        </div>

        <div className="section-card">
          <div className="alert alert-warning">
            ⚠️ Recuerde: el registro de asistencia biométrica se realiza directamente desde el dispositivo lector de huella dactilar.
          </div>
        </div>
      </main>
    </div>
  );
}
