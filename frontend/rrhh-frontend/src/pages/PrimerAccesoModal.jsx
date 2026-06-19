import { useState } from "react";
import { enable2FA, verify2FA, setPassword, solicitarRecuperacion, saveSession, getUser } from "../services/api";
import "../styles/Login.css";

const PASO = {
  ELECCION:        "ELECCION",
  QR:              "QR",
  VERIFICAR_2FA:   "VERIFICAR_2FA",
  CAMBIAR_PASS:    "CAMBIAR_PASS",
  EMAIL_ENVIADO:   "EMAIL_ENVIADO",
};

export default function PrimerAccesoModal({ username, onDone }) {
  const [paso,      setPaso]      = useState(PASO.ELECCION);
  const [qrSrc,     setQrSrc]    = useState(null);
  const [codigo2fa, setCodigo2fa] = useState("");
  const [nueva,     setNueva]     = useState("");
  const [confirmar, setConfirmar] = useState("");
  const [error,     setError]     = useState("");
  const [loading,   setLoading]   = useState(false);
  const [showNueva, setShowNueva] = useState(false);
  const [showConf,  setShowConf]  = useState(false);

  const user = getUser();

  // ── OPCIÓN 1: Activar 2FA ───────────────────────────────────────────────────
  const handleActivar2FA = async () => {
    setError("");
    setLoading(true);
    try {
      const blob = await enable2FA(username);
      const url  = URL.createObjectURL(blob);
      setQrSrc(url);
      setPaso(PASO.QR);
    } catch {
      setError("No se pudo generar el código QR. Intente de nuevo.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerificar2FA = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await verify2FA(username, codigo2fa.trim());
      if (!data.success) {
        setError(data.message || "Código incorrecto. Intente de nuevo.");
        return;
      }
      // Actualizar token en sesión con el nuevo que incluye 2FA
      saveSession(data.token, data.rol, username, user.idEmpleado, true);
      setPaso(PASO.CAMBIAR_PASS);
    } catch {
      setError("Error al verificar el código.");
    } finally {
      setLoading(false);
    }
  };

  const handleSetPassword = async (e) => {
    e.preventDefault();
    setError("");
    if (nueva.length < 8) {
      setError("La contraseña debe tener al menos 8 caracteres.");
      return;
    }
    if (nueva !== confirmar) {
      setError("Las contraseñas no coinciden.");
      return;
    }
    setLoading(true);
    try {
      const data = await setPassword(nueva, confirmar);
      if (!data.success) {
        setError(data.message || "No se pudo actualizar la contraseña.");
        return;
      }
      // Limpiar flag en sesión
      saveSession(user.token, user.rol, username, user.idEmpleado, false);
      onDone(user.rol);
    } catch {
      setError("Error al cambiar la contraseña.");
    } finally {
      setLoading(false);
    }
  };

  // ── OPCIÓN 2: Más tarde — enviar email y proceder ───────────────────────────
  const handleMasTarde = async () => {
    setError("");
    setLoading(true);
    try {
      await solicitarRecuperacion(username);
    } catch {
      // El email puede fallar silenciosamente
    } finally {
      setLoading(false);
      setPaso(PASO.EMAIL_ENVIADO);
    }
  };

  const handleProcederSinCambio = () => {
    onDone(user.rol);
  };

  // ── RENDER ──────────────────────────────────────────────────────────────────
  return (
    <div className="login-modal-overlay" style={{ zIndex: 2000 }}>
      <div className="login-modal-box" style={{ maxWidth: 440 }}>

        {/* ── PASO 1: Elección ── */}
        {paso === PASO.ELECCION && (
          <>
            <div style={{ textAlign: "center", marginBottom: 16 }}>
              <div style={{ fontSize: 40, marginBottom: 8 }}>👋</div>
              <h3 style={{ margin: 0 }}>Bienvenido al sistema</h3>
              <p style={{ color: "#6b7280", fontSize: 14, marginTop: 6 }}>
                Es tu primer acceso. Por seguridad, debes configurar tu cuenta.
              </p>
            </div>

            <div style={{
              background: "#fffbeb", border: "1px solid #fcd34d",
              borderRadius: 8, padding: "10px 14px", fontSize: 13,
              color: "#92400e", marginBottom: 20,
            }}>
              Tu contraseña temporal es tu número de documento (DNI). Te recomendamos cambiarla ahora.
            </div>

            {error && (
              <div className="login-error" style={{ marginBottom: 12 }}>⚠️ {error}</div>
            )}

            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              <button
                className="login-btn"
                style={{ margin: 0 }}
                onClick={handleActivar2FA}
                disabled={loading}
              >
                🔐 Activar autenticación de dos pasos (2FA)
              </button>
              <button
                className="login-modal-btn-secondary"
                style={{ padding: "10px 0" }}
                onClick={handleMasTarde}
                disabled={loading}
              >
                {loading ? "Procesando..." : "⏩ Más tarde — enviar enlace a mi correo"}
              </button>
            </div>

            <p style={{ fontSize: 12, color: "#9ca3af", textAlign: "center", marginTop: 14 }}>
              Si eliges "Más tarde", recibirás un enlace en tu correo personal para cambiar tu contraseña.
            </p>
          </>
        )}

        {/* ── PASO 2: QR ── */}
        {paso === PASO.QR && (
          <>
            <h3 style={{ marginTop: 0 }}>📱 Escanea el código QR</h3>
            <p style={{ fontSize: 14, color: "#6b7280" }}>
              Abre <strong>Google Authenticator</strong> u otra app compatible y escanea el siguiente código:
            </p>
            {qrSrc && (
              <div style={{ textAlign: "center", margin: "16px 0" }}>
                <img src={qrSrc} alt="QR 2FA" style={{ width: 180, height: 180, border: "1px solid #e5e7eb", borderRadius: 8 }} />
              </div>
            )}
            <p style={{ fontSize: 13, color: "#374151" }}>
              Luego de escanear, ingresa el código de 6 dígitos que aparece en la app para confirmar:
            </p>
            <form onSubmit={handleVerificar2FA}>
              <div className="login-field">
                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  placeholder="000000"
                  value={codigo2fa}
                  onChange={e => { setCodigo2fa(e.target.value); setError(""); }}
                  style={{ letterSpacing: 8, textAlign: "center", fontSize: 22, padding: "10px" }}
                  autoFocus required
                />
              </div>
              {error && <div className="login-error" style={{ margin: "8px 0" }}>⚠️ {error}</div>}
              <div className="login-modal-actions">
                <button type="button" className="login-modal-btn-secondary" onClick={() => setPaso(PASO.ELECCION)} disabled={loading}>
                  Atrás
                </button>
                <button type="submit" className="login-btn" style={{ margin: 0 }} disabled={loading || codigo2fa.length < 6}>
                  {loading ? "Verificando..." : "✅ Verificar código"}
                </button>
              </div>
            </form>
          </>
        )}

        {/* ── PASO 3: Cambiar contraseña (tras 2FA) ── */}
        {paso === PASO.CAMBIAR_PASS && (
          <>
            <div style={{ textAlign: "center", marginBottom: 16 }}>
              <div style={{ fontSize: 32, marginBottom: 6 }}>🔑</div>
              <h3 style={{ margin: 0 }}>Establece tu contraseña</h3>
              <p style={{ color: "#6b7280", fontSize: 14, marginTop: 6 }}>
                2FA activado correctamente. Ahora elige una contraseña segura.
              </p>
            </div>
            <form onSubmit={handleSetPassword}>
              <div className="login-field">
                <label>Nueva contraseña</label>
                <div className="lf-wrap">
                  <span className="lf-prefix">🔒</span>
                  <input
                    type={showNueva ? "text" : "password"}
                    placeholder="Mínimo 8 caracteres"
                    value={nueva}
                    onChange={e => { setNueva(e.target.value); setError(""); }}
                    required autoFocus
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
                    placeholder="Repite la contraseña"
                    value={confirmar}
                    onChange={e => { setConfirmar(e.target.value); setError(""); }}
                    required
                  />
                  <button type="button" className="lf-eye" onClick={() => setShowConf(v => !v)} tabIndex={-1}>
                    {showConf ? "🙈" : "👁"}
                  </button>
                </div>
              </div>
              {error && <div className="login-error" style={{ margin: "8px 0" }}>⚠️ {error}</div>}
              <button
                type="submit"
                className="login-btn"
                style={{ marginTop: 8 }}
                disabled={loading || !nueva || !confirmar}
              >
                {loading ? "Guardando..." : "💾 Guardar contraseña e ingresar"}
              </button>
            </form>
          </>
        )}

        {/* ── PASO 4: Email enviado ── */}
        {paso === PASO.EMAIL_ENVIADO && (
          <>
            <div style={{ textAlign: "center" }}>
              <div style={{ fontSize: 40, marginBottom: 10 }}>📧</div>
              <h3 style={{ margin: 0 }}>Revisa tu correo</h3>
              <p style={{ color: "#6b7280", fontSize: 14, marginTop: 8 }}>
                Hemos enviado un enlace a tu correo personal para cambiar tu contraseña.
                Puedes ingresar ahora y cambiarla después desde el enlace recibido.
              </p>
            </div>
            <div style={{
              background: "#f0fdf4", border: "1px solid #86efac",
              borderRadius: 8, padding: "10px 14px", fontSize: 13,
              color: "#166534", margin: "16px 0",
            }}>
              ✅ Enlace enviado. Recuerda que expira en 15 minutos.
            </div>
            <button className="login-btn" style={{ marginTop: 4 }} onClick={handleProcederSinCambio}>
              Ingresar al sistema
            </button>
          </>
        )}

      </div>
    </div>
  );
}
