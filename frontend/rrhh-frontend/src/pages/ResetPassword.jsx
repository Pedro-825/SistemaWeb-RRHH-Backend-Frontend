import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { resetPassword } from "../services/api";
import "../styles/Login.css";
import logo from "../assets/logo.png";

export default function ResetPassword() {
  const [searchParams]  = useSearchParams();
  const navigate        = useNavigate();
  const token           = searchParams.get("token") || "";

  const [nueva,      setNueva]      = useState("");
  const [confirmar,  setConfirmar]  = useState("");
  const [showNueva,  setShowNueva]  = useState(false);
  const [showConf,   setShowConf]   = useState(false);
  const [loading,    setLoading]    = useState(false);
  const [mensaje,    setMensaje]    = useState("");
  const [exito,      setExito]      = useState(false);

  useEffect(() => {
    if (!token) setMensaje("El enlace de recuperación no es válido o ha expirado.");
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (nueva !== confirmar) {
      setMensaje("Las contraseñas no coinciden.");
      return;
    }
    setMensaje("");
    setLoading(true);
    try {
      const data = await resetPassword(token, nueva, confirmar);
      if (data.success) {
        setExito(true);
        setTimeout(() => navigate("/", { replace: true }), 3000);
      } else {
        setMensaje(data.message || "No se pudo actualizar la contraseña.");
      }
    } catch {
      setMensaje("Error de conexión con el servidor.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        {/* ── PANEL IZQUIERDO ── */}
        <div className="login-left">
          <img src={logo} className="logo-img" alt="Hospital San Gabriel" />
          <p className="login-tagline">Sistema de Recursos Humanos – Hospital</p>
        </div>

        {/* ── PANEL DERECHO ── */}
        <div className="login-right">
          <div className="login-avatar">🔑</div>
          <h2>Restablecer Contraseña</h2>
          <p className="login-subtitle">Ingrese su nueva contraseña para recuperar el acceso al sistema.</p>

          {exito ? (
            <>
              <div className="login-modal-success" style={{ marginTop: 24 }}>
                ✅ Contraseña actualizada correctamente. Redirigiendo al inicio de sesión...
              </div>
            </>
          ) : !token ? (
            <div className="login-error" style={{ marginTop: 24 }}>
              ⚠️ El enlace de recuperación no es válido o ha expirado. Solicite uno nuevo desde la pantalla de inicio de sesión.
            </div>
          ) : (
            <form className="login-form" onSubmit={handleSubmit}>
              <div className="login-field">
                <label>Nueva contraseña</label>
                <div className="lf-wrap">
                  <span className="lf-prefix">🔒</span>
                  <input
                    type={showNueva ? "text" : "password"}
                    placeholder="Mínimo 8 caracteres"
                    value={nueva}
                    onChange={e => { setNueva(e.target.value); setMensaje(""); }}
                    minLength={8}
                    required
                    autoFocus
                    disabled={loading}
                  />
                  <button type="button" className="lf-eye" onClick={() => setShowNueva(v => !v)} tabIndex={-1}>
                    {showNueva ? "🙈" : "👁"}
                  </button>
                </div>
              </div>

              <div className="login-field">
                <label>Confirmar contraseña</label>
                <div className="lf-wrap">
                  <span className="lf-prefix">🔒</span>
                  <input
                    type={showConf ? "text" : "password"}
                    placeholder="Repita la contraseña"
                    value={confirmar}
                    onChange={e => { setConfirmar(e.target.value); setMensaje(""); }}
                    minLength={8}
                    required
                    disabled={loading}
                  />
                  <button type="button" className="lf-eye" onClick={() => setShowConf(v => !v)} tabIndex={-1}>
                    {showConf ? "🙈" : "👁"}
                  </button>
                </div>
              </div>

              {mensaje && (
                <div className="login-error">⚠️ {mensaje}</div>
              )}

              <button type="submit" className="login-btn" disabled={loading || !nueva || !confirmar}>
                {loading ? "Actualizando..." : "🔒 Actualizar contraseña"}
              </button>

              <p className="login-forgot">
                <button type="button" className="login-forgot-btn" onClick={() => navigate("/")}>
                  ← Volver al inicio de sesión
                </button>
              </p>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
