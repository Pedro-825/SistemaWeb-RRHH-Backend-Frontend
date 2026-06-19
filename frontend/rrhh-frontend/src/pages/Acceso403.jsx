import { useNavigate } from "react-router-dom";
import { getUser, clearSession, logout } from "../services/api";
import logo from "../assets/logo.png";
import "../styles/Dashboard.css";

export default function Acceso403() {
  const navigate = useNavigate();
  const user = getUser();

  const handleLogout = async () => {
    try { await logout(); } catch {}
    clearSession();
    navigate("/", { replace: true });
  };

  const handleHome = () => {
    const rol = user.rol;
    if (rol === "RRHH") navigate("/rrhh", { replace: true });
    else if (rol === "EMPLEADO") navigate("/empleado", { replace: true });
    else if (rol === "GERENCIA") navigate("/gerencia", { replace: true });
    else navigate("/", { replace: true });
  };

  return (
    <div className="acceso-403-page">
      <div className="acceso-403-card">
        <img src={logo} alt="Logo Hospital" className="acceso-403-logo" />
        <div className="acceso-403-code">403</div>
        <h1 className="acceso-403-title">Acceso No Autorizado</h1>
        <p className="acceso-403-subtitle">Error 403 — Acceso Denegado</p>
        <p className="acceso-403-desc">
          No tienes permisos suficientes para acceder a esta página.
        </p>

        <div className="acceso-403-info">
          <p className="acceso-403-info-title">Información de sesión</p>
          <p>Usuario: <strong>{user.username || "—"}</strong></p>
          <p>Rol actual: <strong>{user.rol || "ANÓNIMO"}</strong></p>
        </div>

        <div className="acceso-403-actions">
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>
            ← Volver atrás
          </button>
          <button className="btn btn-primary" onClick={handleHome}>
            Ir a Inicio
          </button>
          <button className="btn btn-danger" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </div>
    </div>
  );
}
