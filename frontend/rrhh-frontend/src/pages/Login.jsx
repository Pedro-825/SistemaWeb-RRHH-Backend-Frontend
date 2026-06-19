import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";
import PrimerAccesoModal from "./PrimerAccesoModal";
import { login, saveSession, getUser, solicitarRecuperacion } from "../services/api";

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername]     = useState("");
  const [password, setPassword]     = useState("");
  const [showPwd,  setShowPwd]      = useState(false);
  const [mensaje,  setMensaje]      = useState("");
  const [intentos, setIntentos]     = useState(null);
  const [show2FA,        setShow2FA]        = useState(false);
  const [resetKey,       setResetKey]       = useState(0);
  const [primerAcceso,   setPrimerAcceso]   = useState(false);
  const [primerUsername, setPrimerUsername] = useState("");
  const [showForgot,    setShowForgot]   = useState(false);
  const [forgotInput,   setForgotInput]  = useState("");
  const [forgotOk,      setForgotOk]    = useState(false);
  const [forgotLoading, setForgotLoading] = useState(false);
  const [forgotError,   setForgotError]  = useState("");

  useEffect(() => {
    const user = getUser();
    if (user?.rol) redirectByRole(user.rol);
  }, []);

  const redirectByRole = (role) => {
    if      (role === "GERENCIA") navigate("/gerencia", { replace: true });
    else if (role === "RRHH")     navigate("/rrhh", { replace: true });
    else if (role === "EMPLEADO") navigate("/empleado", { replace: true });
    else setMensaje("Rol de usuario no reconocido.");
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setMensaje("");
    setIntentos(null);
    try {
      const data = await login(username, password);
      if (!data.success) {
        const rem = data.intentosRestantes ?? data.remainingAttempts ?? null;
        setIntentos(rem);
        setMensaje(data.message || "Credenciales inválidas. Intente nuevamente.");
        return;
      }
      if (data.requires2FA) {
        setResetKey(k => k + 1);
        setShow2FA(true);
        return;
      }
      saveSession(data.token, data.rol, username, data.idEmpleado ?? null, data.requiereCambioPassword ?? false);
      if (data.requires2FASetup) sessionStorage.setItem('show2FAReminder', 'true');
      if (data.requiereCambioPassword) {
        setPrimerUsername(username);
        setPrimerAcceso(true);
        return;
      }
      redirectByRole(data.rol);
    } catch {
      setMensaje("Error de conexión con el servidor. Verifique que el servicio esté disponible.");
    }
  };

  const cerrarForgot = () => {
    setShowForgot(false);
    setForgotInput("");
    setForgotOk(false);
    setForgotError("");
    setForgotLoading(false);
  };

  const handleForgotSubmit = async (e) => {
    e.preventDefault();
    setForgotError("");
    setForgotLoading(true);
    try {
      const data = await solicitarRecuperacion(forgotInput.trim());
      if (data.success) {
        setForgotOk(true);
      } else {
        setForgotError(data.message || "No se pudo procesar la solicitud.");
      }
    } catch {
      setForgotError("Error de conexión con el servidor.");
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        {/* ── PANEL IZQUIERDO ── */}
        <div className="login-left">
          <img src={logo} className="logo-img" alt="Hospital San Gabriel" />
          <p className="login-tagline">Sistema de Recursos Humanos – Hospital</p>
          <p className="login-sys-label">Sistema de Recursos Humanos – Hospital</p>
        </div>

        {/* ── PANEL DERECHO ── */}
        <div className="login-right">
          <div className="login-avatar">👤</div>
          <h2>Iniciar Sesión</h2>
          <p className="login-subtitle">Ingrese sus credenciales para acceder al sistema</p>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="login-field">
              <label>Usuario o email</label>
              <div className="lf-wrap">
                <span className="lf-prefix">👤</span>
                <input
                  type="text"
                  placeholder="Ingrese su usuario o email"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  autoComplete="username"
                  required
                />
              </div>
            </div>

            <div className="login-field">
              <label>Contraseña</label>
              <div className="lf-wrap">
                <span className="lf-prefix">🔒</span>
                <input
                  type={showPwd ? "text" : "password"}
                  placeholder="Ingrese su contraseña"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  autoComplete="current-password"
                  required
                />
                <button type="button" className="lf-eye" onClick={() => setShowPwd(v => !v)} tabIndex={-1}>
                  {showPwd ? "🙈" : "👁"}
                </button>
              </div>
            </div>

            <button type="submit" className="login-btn">🔒 Iniciar Sesión</button>
          </form>

          <p className="login-forgot">
            <button type="button" className="login-forgot-btn" onClick={() => setShowForgot(true)}>
              ¿Olvidó su contraseña?
            </button>
          </p>

          {show2FA && (
            <div className="login-info-alert">
              ℹ️ Se requiere un código de verificación de dos pasos para acceder a su cuenta.
            </div>
          )}

          {mensaje && (
            <div className="login-error">
              <div>⚠️ {mensaje}</div>
              {intentos !== null && (
                <div className="login-attempts">Intentos restantes: <strong>{intentos}</strong></div>
              )}
            </div>
          )}

        </div>
      </div>

      {/* ── 2FA POPUP ── */}
      {show2FA && (
        <TwoFA
          key={resetKey}
          open={show2FA}
          username={username}
          onClose={() => setShow2FA(false)}
          onSuccess={(rol, idEmpleado, requiereCambioPassword) => {
            setShow2FA(false);
            if (requiereCambioPassword) {
              setPrimerUsername(username);
              setPrimerAcceso(true);
              return;
            }
            redirectByRole(rol);
          }}
        />
      )}

      {primerAcceso && (
        <PrimerAccesoModal
          username={primerUsername}
          onDone={(rol) => {
            setPrimerAcceso(false);
            redirectByRole(rol);
          }}
        />
      )}

      {/* ── MODAL RECUPERAR CONTRASEÑA ── */}
      {showForgot && (
        <div className="login-modal-overlay" onClick={e => e.target === e.currentTarget && cerrarForgot()}>
          <div className="login-modal-box">
            <h3>🔑 Recuperar Contraseña</h3>
            {!forgotOk ? (
              <>
                <p>Ingrese su usuario o correo electrónico registrado. Le enviaremos las instrucciones de recuperación.</p>
                <form onSubmit={handleForgotSubmit}>
                  <div className="login-field">
                    <label>Usuario o correo</label>
                    <input
                      type="text"
                      placeholder="Ingrese su usuario o correo"
                      value={forgotInput}
                      onChange={e => { setForgotInput(e.target.value); setForgotError(""); }}
                      required autoFocus
                      disabled={forgotLoading}
                    />
                  </div>
                  {forgotError && (
                    <div className="login-error" style={{ margin: "8px 0 0" }}>⚠️ {forgotError}</div>
                  )}
                  <div className="login-modal-actions">
                    <button type="button" className="login-modal-btn-secondary" onClick={cerrarForgot} disabled={forgotLoading}>Cancelar</button>
                    <button type="submit" className="login-btn" style={{ margin: 0 }} disabled={forgotLoading || !forgotInput.trim()}>
                      {forgotLoading ? "Enviando..." : "Enviar instrucciones"}
                    </button>
                  </div>
                </form>
              </>
            ) : (
              <>
                <div className="login-modal-success">
                  ✅ Si su usuario o correo está registrado en el sistema, recibirá las instrucciones en su correo institucional.
                </div>
                <div className="login-modal-actions">
                  <button className="login-btn" style={{ margin: 0 }} onClick={cerrarForgot}>Cerrar</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
