import { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import { clearSession, logout } from "../services/api";

const I = {
  home: "\u{1F3E0}",
  people: "\u{1F465}",
  person: "\u{1F464}",
  clock: "\u{1F551}",
  board: "\u{1F4CB}",
  money: "\u{1F4B0}",
  calendar: "\u{1F4C5}",
  chart: "\u{1F4CA}",
  logout: "\u{1F6AA}",
};

const NAV = {
  RRHH: {
    title: "Panel RRHH",
    subtitle: "Recursos Humanos",
    themeClass: "sidebar-rrhh",
    links: [
      { to: "/rrhh", icon: I.home, label: "Inicio" },
      {
        icon: I.people, label: "Gestion de Empleados",
        group: true,
        basePaths: ["/rrhh/empleados", "/rrhh/horarios"],
        children: [
          { to: "/rrhh/empleados", icon: I.person, label: "Empleados" },
          { to: "/rrhh/horarios", icon: I.clock, label: "Horarios" },
        ],
      },
      { to: "/rrhh/asistencia", icon: I.board, label: "Control de Asistencia" },
      { to: "/rrhh/nomina", icon: I.money, label: "Calculo de Nomina" },
      { to: "/rrhh/solicitudes", icon: I.calendar, label: "Solicitudes" },
      { to: "/rrhh/reportes", icon: I.chart, label: "Reportes" },
    ],
  },
  EMPLEADO: {
    title: "Mi Portal",
    subtitle: "Portal del Empleado",
    themeClass: "sidebar-empleado",
    links: [
      { to: "/empleado", icon: I.home, label: "Inicio" },
      { to: "/empleado/asistencia", icon: I.board, label: "Mi Asistencia" },
      { to: "/empleado/solicitudes", icon: I.calendar, label: "Mis Solicitudes" },
      { to: "/empleado/nomina", icon: I.money, label: "Mi Nomina" },
    ],
  },
  GERENCIA: {
    title: "Panel Gerencia",
    subtitle: "Gestion Ejecutiva",
    themeClass: "sidebar-gerencia",
    links: [
      { to: "/gerencia", icon: I.home, label: "Inicio" },
      { to: "/gerencia/solicitudes", icon: I.calendar, label: "Solicitudes" },
      { to: "/gerencia/asistencia", icon: I.board, label: "Mi Asistencia" },
      { to: "/gerencia/justificaciones", icon: I.chart, label: "Justificaciones" },
    ],
  },
};

export default function Sidebar({ rol }) {
  const location = useLocation();
  const navigate = useNavigate();
  const config = NAV[rol] || NAV.RRHH;

  const initialOpen = () => {
    const item = config.links.find(l =>
      l.group && l.basePaths?.some(p => location.pathname.startsWith(p))
    );
    return item ? item.label : null;
  };
  const [openGroup, setOpenGroup] = useState(initialOpen);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => { setMobileOpen(false); }, [location.pathname]);

  const isActive = (to) => location.pathname === to;
  const toggleGroup = (label) => setOpenGroup(g => g === label ? null : label);

  const handleLogout = async () => {
    try { await logout(); } catch {}
    clearSession();
    navigate("/", { replace: true });
  };

  return (
    <>
      <button
        className={`hamburger-btn ${config.themeClass}`}
        onClick={() => setMobileOpen(o => !o)}
        aria-label="Abrir menu"
      >
        <span /><span /><span />
      </button>

      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />}

      <aside className={`sidebar ${config.themeClass}${mobileOpen ? " open" : ""}`}>
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
                        <Link key={child.to} to={child.to} className={isActive(child.to) ? "active" : ""}>
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
              <Link key={link.to} to={link.to} className={isActive(link.to) ? "active" : ""}>
                <span className="nav-icon">{link.icon}</span>
                <span>{link.label}</span>
              </Link>
            );
          })}

          <div className="nav-logout">
            <button onClick={handleLogout}>
              <span className="nav-icon">{I.logout}</span>
              <span>Cerrar Sesion</span>
            </button>
          </div>
        </nav>
      </aside>
    </>
  );
}
