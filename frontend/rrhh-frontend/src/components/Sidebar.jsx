import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import { clearSession, logout } from "../services/api";

const NAV = {
  RRHH: {
    title: "Panel RRHH",
    subtitle: "Recursos Humanos",
    themeClass: "sidebar-rrhh",
    links: [
      { to: "/rrhh",             icon: "🏠", label: "Inicio" },
      {
        icon: "👥", label: "Gestión de Empleados",
        group: true,
        basePaths: ["/rrhh/empleados", "/rrhh/horarios"],
        children: [
          { to: "/rrhh/empleados", icon: "👤", label: "Empleados" },
          { to: "/rrhh/horarios",  icon: "🕐", label: "Horarios" },
        ],
      },
      { to: "/rrhh/asistencia",  icon: "📋", label: "Control de Asistencia" },
      { to: "/rrhh/nomina",      icon: "💰", label: "Cálculo de Nómina" },
      { to: "/rrhh/solicitudes", icon: "📅", label: "Solicitudes" },
      { to: "/rrhh/reportes",    icon: "📊", label: "Reportes" },
    ],
  },
  EMPLEADO: {
    title: "Mi Portal",
    subtitle: "Portal del Empleado",
    themeClass: "sidebar-empleado",
    links: [
      { to: "/empleado",             icon: "🏠", label: "Inicio" },
      { to: "/empleado/asistencia",  icon: "📋", label: "Mi Asistencia" },
      { to: "/empleado/solicitudes", icon: "📅", label: "Mis Solicitudes" },
      { to: "/empleado/nomina",      icon: "💰", label: "Mi Nómina" },
    ],
  },
  GERENCIA: {
    title: "Panel Gerencia",
    subtitle: "Gestión Ejecutiva",
    themeClass: "sidebar-gerencia",
    links: [
      { to: "/gerencia",               icon: "🏠", label: "Inicio" },
      { to: "/gerencia/solicitudes",   icon: "📅", label: "Solicitudes" },
      { to: "/gerencia/asistencia",    icon: "📋", label: "Mi Asistencia" },
    ],
  },
};

export default function Sidebar({ rol }) {
  const location  = useLocation();
  const navigate  = useNavigate();
  const config    = NAV[rol] || NAV.RRHH;

  // Inicializa abierto si la ruta actual pertenece a algún grupo
  const initialOpen = () => {
    const item = config.links.find(l =>
      l.group && l.basePaths?.some(p => location.pathname.startsWith(p))
    );
    return item ? item.label : null;
  };
  const [openGroup, setOpenGroup] = useState(initialOpen);

  const isActive   = (to) => location.pathname === to;
  const toggleGroup = (label) => setOpenGroup(g => g === label ? null : label);

  const handleLogout = async () => {
    try { await logout(); } catch {}
    clearSession();
    navigate("/", { replace: true });
  };

  return (
    <aside className={`sidebar ${config.themeClass}`}>
      <div className="sidebar-brand">
        <div className="sidebar-brand-icon">
          <img src={logo} alt="Logo Hospital" className="sidebar-logo-img" />
        </div>
        <div className="sidebar-brand-text">
          <h2>{config.title}</h2>
          <p>{config.subtitle}</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        {config.links.map((link) => {
          if (link.group) {
            const isExpanded = openGroup === link.label;
            const isGroupActive = link.basePaths?.some(p => location.pathname.startsWith(p));
            return (
              <div key={link.label} className="sidebar-group">
                <button
                  className={`sidebar-group-header ${isGroupActive ? "active" : ""}`}
                  onClick={() => toggleGroup(link.label)}
                >
                  <span className="nav-icon">{link.icon}</span>
                  <span className="sidebar-group-label">{link.label}</span>
                  <span className={`sidebar-group-arrow ${isExpanded ? "open" : ""}`}>›</span>
                </button>
                {isExpanded && (
                  <div className="sidebar-group-items">
                    {link.children.map(child => (
                      <Link
                        key={child.to}
                        to={child.to}
                        className={isActive(child.to) ? "active" : ""}
                      >
                        <span className="nav-icon">{child.icon}</span>
                        <span>{child.label}</span>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            );
          }
          return (
            <Link
              key={link.to}
              to={link.to}
              className={isActive(link.to) ? "active" : ""}
            >
              <span className="nav-icon">{link.icon}</span>
              <span>{link.label}</span>
            </Link>
          );
        })}

        <div className="nav-logout">
          <button onClick={handleLogout}>
            <span className="nav-icon">🚪</span>
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </nav>
    </aside>
  );
}
