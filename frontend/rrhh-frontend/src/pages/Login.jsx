import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";
import logo from "../assets/logo.png";
import TwoFA from "./TwoFA";
import PrimerAccesoModal from "./PrimerAccesoModal";
import Setup2FAReminderModal from "./Setup2FAReminderModal";
import DownloadAppModal from "../components/DownloadAppModal";
import { login, saveSession, getUser, solicitarRecuperacion } from "../services/api";

function UserIcon() {
  return (
    <svg className="login-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg className="login-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M17 9h-1V7a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2Zm-7-2a2 2 0 0 1 4 0v2h-4V7Z" />
    </svg>
  );
}

export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername]     = useState("");
  const [password, setPassword]     = useState("");
  const [showPwd,  setShowPwd]      = useState(false);
  const [mensaje,  setMensaje]      = useState("");
  const [intentos, setIntentos]     = useState(null);
  const [show2FA,        setShow2FA]        = useState(false);
  const [resetKey,       setResetKey]       = useState(0);
  const [primerAcceso,       setPrimerAcceso]       = useState(false);
  const [primerUsername,     setPrimerUsername]     = useState("");
  const [dosFaYaConfigurado, setDosFaYaConfigurado] = useState(false);
  const [showForgot,    setShowForgot]   = useState(false);
  const [forgotInput,   setForgotInput]  = useState("");
  const [forgotOk,      setForgotOk]    = useState(false);
  const [forgotLoading, setForgotLoading] = useState(false);
  const [forgotError,   setForgotError]  = useState("");
  const [showDownloadModal, setShowDownloadModal] = useState(false);
  const [pendingRol,        setPendingRol]        = useState(null);
  const [show2FASetup,      setShow2FASetup]      = useState(false);
  const [pendingRedirect,   setPendingRedirect]   = useState(null);


  useEffect(() => {
    const lastApiError = sessionStorage.getItem('lastApiError');
    if (lastApiError) {
      setMensaje(lastApiError);
      sessionStorage.removeItem('lastApiError');
    }
    // Si hay un primer acceso pendiente por recarga de pagina, restaurar el modal
    const pendingUser = sessionStorage.getItem('primerAccesoPendiente');
    if (pendingUser) {
      const dfa = sessionStorage.getItem('primerAccesoDosFa') === 'true';
      setPrimerUsername(pendingUser);
      setDosFaYaConfigurado(dfa);
      setPrimerAcceso(true);
      return;
    }
    const user = getUser();
    if (user?.rol) redirectByRole(user.rol);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  function redirectByRole(role) {
    if      (role === "GERENCIA") navigate("/gerencia", { replace: true });
    else if (role === "RRHH")     navigate("/rrhh", { replace: true });
    else if (role === "EMPLEADO") navigate("/empleado", { replace: true });
    else setMensaje("Rol de usuario no reconocido.");
  }

  const USUARIOS_PRUEBA = ['admin.rrhh', 'empleado_test', 'gerencia_test'];

  const redirigirConModal = (rol, idEmpleado, appInstalada = false) => {
    const recordatorioOculto = localStorage.getItem(`downloadAppDismissed:${idEmpleado}`) === "true";
    if ((rol === "EMPLEADO" || rol === "GERENCIA") && idEmpleado && !appInstalada && !recordatorioOculto && !USUARIOS_PRUEBA.includes(username)) {
      setPendingRol(rol);
      setShowDownloadModal(true);
    } else {
      redirectByRole(rol);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setMensaje("");
    setIntentos(null);
    sessionStorage.removeItem("lastApiError");
    try {
      const data = await login(username, password);
      if (!data.success) {
        const rem = data.intentosRestantes ?? data.remainingAttempts ?? null;
        setIntentos(rem);
        setMensaje(data.message || "Credenciales invalidas. Intente nuevamente.");
        return;
      }
      if (data.requires2FA) {
        setResetKey(k => k + 1);
        setShow2FA(true);
        return;
      }
      const nombreUsuarioReal = data.nombreUsuario || username;
      saveSession(data.token, data.rol, nombreUsuarioReal, data.idEmpleado ?? null, data.requiereCambioPassword ?? false);
      if (data.requiereCambioPassword) {
        const dfa = data.dosFaYaConfigurado ?? false;
        sessionStorage.setItem('primerAccesoPendiente', nombreUsuarioReal);
        sessionStorage.setItem('primerAccesoDosFa', dfa ? 'true' : 'false');
        setPrimerUsername(nombreUsuarioReal);
        setDosFaYaConfigurado(dfa);
        setPrimerAcceso(true);
        return;
      }
      if (data.requires2FASetup) {
        setPendingRedirect({ rol: data.rol, idEmpleado: data.idEmpleado ?? null, appInstalada: data.appMovilInstalada ?? false });
        setShow2FASetup(true);
        return;
      }
      redirigirConModal(data.rol, data.idEmpleado ?? null, data.appMovilInstalada ?? false);
    } catch (err) {
      setMensaje(err?.message || "No se pudo conectar con el servidor. Verifique que el servicio este disponible.");
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
    } catch (err) {
      setForgotError(err?.message || "No se pudo conectar con el servidor.");
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        {/* PANEL IZQUIERDO */}
        <div className="login-left">
          <img src={logo} className="logo-img" alt="Hospital San Gabriel" />
          <p className="login-tagline">Sistema de Recursos Humanos - Hospital</p>
          <p className="login-sys-label">Sistema de Recursos Humanos - Hospital</p>
        </div>

        {/* PANEL DERECHO */}
        <div className="login-right">
          <div className="login-avatar"><UserIcon /></div>
          <h2>Iniciar Sesion</h2>
          <p className="login-subtitle">Ingrese sus credenciales para acceder al sistema</p>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="login-field">
              <label>Usuario o email</label>
              <div className="lf-wrap">
                <span className="lf-prefix"><UserIcon /></span>
                <input
                  type="text"
                  placeholder="Ingrese su usuario o email"
                  value={username}
                  onChange={e => { setUsername(e.target.value); setMensaje(""); }}
                  autoComplete="username"
                  required
                />
              </div>
            </div>

            <div className="login-field">
              <label>Contrasena</label>
              <div className="lf-wrap">
                <span className="lf-prefix"><LockIcon /></span>
                <input
                  type={showPwd ? "text" : "password"}
                  placeholder="Ingrese su contrasena"
                  value={password}
                  onChange={e => { setPassword(e.target.value); setMensaje(""); }}
                  autoComplete="current-password"
                  required
                />
                <button type="button" className="lf-eye" onClick={() => setShowPwd(v => !v)} tabIndex={-1}>
                  {showPwd ? "Ocultar" : "Ver"}
                </button>
              </div>
            </div>

            <button type="submit" className="login-btn">Iniciar Sesion</button>
          </form>

          <p className="login-forgot">
            <button type="button" className="login-forgot-btn" onClick={() => setShowForgot(true)}>
              Olvido su contrasena?
            </button>
          </p>

          {show2FA && (
            <div className="login-info-alert">
              Se requiere un codigo de verificacion de dos pasos para acceder a su cuenta.
            </div>
          )}

          {mensaje && (
            <div className="login-error">
              <div>{mensaje}</div>
              {intentos !== null && (
                <div className="login-attempts">Intentos restantes: <strong>{intentos}</strong></div>
              )}
            </div>
          )}

        </div>
      </div>

      {/* 2FA POPUP */}
      {show2FA && (
        <TwoFA
          key={resetKey}
          open={show2FA}
          username={username}
          onClose={() => setShow2FA(false)}
          onSuccess={(rol, idEmpleado, requiereCambioPassword, appMovilInstalada) => {
            setShow2FA(false);
            const nombreUsuarioReal = getUser()?.username || username;
            if (requiereCambioPassword) {
              saveSession(null, rol, nombreUsuarioReal, idEmpleado, true);
              setPrimerUsername(nombreUsuarioReal);
              setPrimerAcceso(true);
              return;
            }
            redirigirConModal(rol, idEmpleado, appMovilInstalada);
          }}
        />
      )}

      {primerAcceso && (
        <PrimerAccesoModal
          username={primerUsername}
          dosFaYaConfigurado={dosFaYaConfigurado}
          onDone={(rol, usernameModal, idEmpleadoModal) => {
            sessionStorage.removeItem('primerAccesoPendiente');
            sessionStorage.removeItem('primerAccesoDosFa');
            setPrimerAcceso(false);
            setDosFaYaConfigurado(false);
            if (usernameModal) setUsername(usernameModal);
            redirigirConModal(rol, idEmpleadoModal ?? getUser().idEmpleado);
          }}
        />
      )}

      {show2FASetup && (
        <Setup2FAReminderModal
          username={getUser()?.username}
          onDone={() => {
            setShow2FASetup(false);
            if (pendingRedirect) {
              redirigirConModal(pendingRedirect.rol, pendingRedirect.idEmpleado, pendingRedirect.appInstalada);
              setPendingRedirect(null);
            }
          }}
        />
      )}

      {showDownloadModal && (
        <DownloadAppModal
          idEmpleado={getUser().idEmpleado}
          onClose={() => {
            setShowDownloadModal(false);
            redirectByRole(pendingRol);
          }}
          onInstalada={() => {
            setShowDownloadModal(false);
            redirectByRole(pendingRol);
          }}
        />
      )}

      {/* MODAL RECUPERAR CONTRASENA */}
      {showForgot && (
        <div className="login-modal-overlay">
          <div className="login-modal-box">
            <h3>Recuperar contrasena</h3>
            {!forgotOk ? (
              <>
                <p>Ingrese su usuario o correo electronico registrado. Le enviaremos las instrucciones de recuperacion.</p>
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
                    <div className="login-error" style={{ margin: "8px 0 0" }}>{forgotError}</div>
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
                  Si su usuario o correo esta registrado en el sistema, recibira las instrucciones en su correo institucional.
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
