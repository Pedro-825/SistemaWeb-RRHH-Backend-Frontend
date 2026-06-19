import { Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState, useRef, useCallback } from "react";
import { getUser, clearSession, enable2FA, verify2FA, saveSession, getMe } from "./services/api";
import Login from "./pages/Login";
import Acceso403 from "./pages/Acceso403";
import DashboardRRHH from "./pages/DashboardRRHH";
import DashboardEmpleado from "./pages/DashboardEmpleado";
import DashboardGerencia from "./pages/DashboardGerencia";
import GestionEmpleados from "./pages/GestionEmpleados";
import ControlAsistencia from "./pages/ControlAsistencia";
import CalculoNomina from "./pages/CalculoNomina";
import SolicitudPermisos from "./pages/SolicitudPermisos";
import GenerarReportes from "./pages/GenerarReportes";
import NominaEmpleado from "./pages/NominaEmpleado";
import GerenciaJustificaciones from "./pages/GerenciaJustificaciones";
import ResetPassword from "./pages/ResetPassword";
import GestionHorarios from "./pages/GestionHorarios";
import DevLogin from "./pages/DevLogin"; // ELIMINAR ANTES DE PRODUCCIÓN
import ErrorBoundary from "./components/ErrorBoundary";

const DASH = { RRHH: '/rrhh', EMPLEADO: '/empleado', GERENCIA: '/gerencia' };

/* ── Autocorrector de URL ────────────────────────────────────────
   URL inexistente + usuario logueado → redirige a su dashboard.
   URL inexistente + sin sesión       → redirige al login.
   URLs de otro rol son capturadas antes por ProtectedRoute → 403.
──────────────────────────────────────────────────────────────── */
function AutoRedirect() {
  const navigate = useNavigate();
  const user = getUser();

  useEffect(() => {
    if (!user?.rol) { navigate('/', { replace: true }); return; }
    const t = setTimeout(() => { navigate(DASH[user.rol] ?? '/', { replace: true }); }, 2000);
    return () => clearTimeout(t);
  }, [navigate, user?.rol]);

  if (!user?.rol) return null;
  return (
    <div style={{
      minHeight: '100vh', display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center', gap: '16px',
      background: '#f8fafc', fontFamily: "'Poppins', system-ui, sans-serif",
    }}>
      <div style={{ fontSize: '60px' }}>🔍</div>
      <h2 style={{ margin: 0, color: '#1e293b', fontSize: '22px' }}>Página no encontrada</h2>
      <p style={{ color: '#64748b', margin: 0, fontSize: '14px', textAlign: 'center' }}>
        La dirección que ingresaste no existe. Redirigiendo a tu panel en 2 segundos…
      </p>
      <div style={{
        background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '8px',
        padding: '10px 22px', fontSize: '13px', color: '#2563eb', fontFamily: 'monospace',
      }}>
        → {DASH[user.rol] ?? '/'}
      </div>
    </div>
  );
}

function Setup2FAModal() {
  const [visible,   setVisible]   = useState(sessionStorage.getItem('show2FAReminder') === 'true');
  const [step,      setStep]      = useState(1); // 1=aviso 2=QR 3=verificar 4=éxito
  const [qrUrl,     setQrUrl]     = useState(null);
  const [loading,   setLoading]   = useState(false);
  const [code,      setCode]      = useState('');
  const [codeError, setCodeError] = useState('');
  const user = getUser();
  const location = useLocation();

  // Re-check sessionStorage whenever the route changes (e.g., after login redirect)
  useEffect(() => {
    if (sessionStorage.getItem('show2FAReminder') === 'true') {
      setVisible(true);
      setStep(1);
    }
  }, [location.pathname]);

  const cerrar = () => {
    sessionStorage.removeItem('show2FAReminder');
    if (qrUrl) URL.revokeObjectURL(qrUrl);
    setVisible(false);
  };

  const handleActivar = async () => {
    setLoading(true);
    try {
      const blob = await enable2FA(user.username);
      setQrUrl(URL.createObjectURL(blob));
      setStep(2);
    } catch { cerrar(); }
    finally { setLoading(false); }
  };

  const handleVerificar = async () => {
    if (code.length < 6) { setCodeError('Ingresa el código de 6 dígitos'); return; }
    setLoading(true);
    setCodeError('');
    try {
      const data = await verify2FA(user.username, code);
      if (data.success) {
        saveSession(data.token, data.rol, user.username, user.idEmpleado ?? null);
        setStep(4);
      } else {
        setCodeError(data.message || 'Código incorrecto, intenta nuevamente');
        setCode('');
      }
    } catch { setCodeError('Error de conexión'); }
    finally { setLoading(false); }
  };

  if (!visible || !user?.rol) return null;

  const overlay = {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)',
    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999,
  };
  const box = {
    background: '#fff', borderRadius: 16, padding: '36px 32px',
    maxWidth: 420, width: '90%', textAlign: 'center',
    boxShadow: '0 20px 60px rgba(0,0,0,0.25)',
    fontFamily: "'Poppins', system-ui, sans-serif",
  };
  const btnPrimary = (extra = {}) => ({
    padding: '10px 22px', borderRadius: 8, border: 'none',
    background: '#2563eb', color: '#fff', cursor: 'pointer',
    fontSize: 14, fontWeight: 600, flex: '1 1 0', maxWidth: 170, ...extra,
  });
  const btnSecondary = {
    padding: '10px 22px', borderRadius: 8, border: '1px solid #cbd5e1',
    background: '#f8fafc', color: '#475569', cursor: 'pointer',
    fontSize: 14, flex: '1 1 0', maxWidth: 170,
  };
  const btns = { display: 'flex', gap: 12, justifyContent: 'center', alignItems: 'center', width: '100%', marginTop: 20 };

  return (
    <div style={overlay}>
      <div style={box}>

        {/* ── PASO 1: Aviso ── */}
        {step === 1 && <>
          <div style={{ fontSize: 52, marginBottom: 12 }}>🔐</div>
          <h3 style={{ margin: '0 0 10px', color: '#1e293b', fontSize: 18 }}>
            Activa la autenticación en dos pasos
          </h3>
          <p style={{ color: '#475569', fontSize: 14, margin: '0 0 8px' }}>
            Tu cuenta aún no tiene la verificación en dos pasos activada.
            Te recomendamos activarla para proteger tu acceso al sistema.
          </p>
          <p style={{ color: '#94a3b8', fontSize: 12, margin: '0 0 4px' }}>
            Este aviso aparecerá en cada inicio de sesión hasta que la actives.
          </p>
          <div style={btns}>
            <button style={btnSecondary} onClick={cerrar}>Más tarde</button>
            <button style={btnPrimary()} onClick={handleActivar} disabled={loading}>
              {loading ? 'Generando QR...' : '🔐 Activar ahora'}
            </button>
          </div>
        </>}

        {/* ── PASO 2: QR ── */}
        {step === 2 && <>
          <div style={{ fontSize: 36, marginBottom: 8 }}>📱</div>
          <h3 style={{ margin: '0 0 10px', color: '#1e293b', fontSize: 18 }}>Escanea el código QR</h3>
          <p style={{ color: '#475569', fontSize: 13, margin: '0 0 16px' }}>
            Abre <strong>Google Authenticator</strong> o <strong>Authy</strong> y escanea este código.
          </p>
          {qrUrl && (
            <img src={qrUrl} alt="QR 2FA" style={{
              width: 200, height: 200, margin: '0 auto 16px', display: 'block',
              border: '4px solid #e2e8f0', borderRadius: 10,
            }} />
          )}
          <p style={{ color: '#64748b', fontSize: 12, margin: '0 0 4px' }}>
            Una vez escaneado, haz clic en <strong>Siguiente</strong> para verificar.
          </p>
          <div style={btns}>
            <button style={btnSecondary} onClick={cerrar}>Cancelar</button>
            <button style={btnPrimary()} onClick={() => setStep(3)}>Siguiente →</button>
          </div>
        </>}

        {/* ── PASO 3: Verificar código ── */}
        {step === 3 && <>
          <div style={{ fontSize: 42, marginBottom: 10 }}>🔢</div>
          <h3 style={{ margin: '0 0 10px', color: '#1e293b', fontSize: 18 }}>Verifica el código</h3>
          <p style={{ color: '#475569', fontSize: 13, margin: '0 0 20px' }}>
            Ingresa el código de 6 dígitos que aparece en tu aplicación autenticadora para confirmar la configuración.
          </p>
          <input
            type="text"
            inputMode="numeric"
            maxLength={6}
            value={code}
            onChange={e => { setCode(e.target.value.replace(/\D/g, '').slice(0, 6)); setCodeError(''); }}
            placeholder="_ _ _ _ _ _"
            autoFocus
            style={{
              width: 220, padding: '14px 20px', textAlign: 'center', fontSize: 26,
              letterSpacing: 8, border: '2px solid #cbd5e1', borderRadius: 12,
              outline: 'none', fontWeight: 700, color: '#1e293b',
              display: 'block', margin: '0 auto', boxSizing: 'border-box',
              transition: 'border-color 0.2s',
            }}
            onFocus={e => e.target.style.borderColor = '#2563eb'}
            onBlur={e => e.target.style.borderColor = '#cbd5e1'}
          />
          {codeError && <p style={{ color: '#ef4444', fontSize: 13, margin: '10px 0 0' }}>{codeError}</p>}
          <div style={btns}>
            <button style={btnSecondary} onClick={() => { setStep(2); setCode(''); setCodeError(''); }}>← Atrás</button>
            <button style={btnPrimary()} onClick={handleVerificar} disabled={loading || code.length < 6}>
              {loading ? 'Verificando...' : '✅ Verificar'}
            </button>
          </div>
        </>}

        {/* ── PASO 4: Éxito ── */}
        {step === 4 && <>
          <div style={{ fontSize: 60, marginBottom: 12 }}>✅</div>
          <h3 style={{ margin: '0 0 10px', color: '#16a34a', fontSize: 20 }}>
            ¡2FA activado correctamente!
          </h3>
          <p style={{ color: '#475569', fontSize: 14, margin: '0 0 24px' }}>
            A partir del próximo inicio de sesión se te pedirá el código de tu aplicación autenticadora.
          </p>
          <button style={btnPrimary({ maxWidth: 200, margin: '0 auto', display: 'block' })} onClick={cerrar}>
            Continuar al sistema
          </button>
        </>}

      </div>
    </div>
  );
}

function ProtectedRoute({ children, requiredRole }) {
  const user = getUser();
  if (!user?.rol) return <Navigate to="/" replace />;
  if (requiredRole && user.rol !== requiredRole) return <Acceso403 />;
  return children;
}

const WARNING_MS  = 10 * 60 * 1000; // 10 min → mostrar modal
const LOGOUT_MS   = 15 * 60 * 1000; // 15 min → cerrar sesión
const COUNTDOWN_S = 5 * 60;         // 5 min de cuenta regresiva

function AfkModal({ countdown, onContinuar, onSalir }) {
  const mins = String(Math.floor(countdown / 60)).padStart(2, '0');
  const secs = String(countdown % 60).padStart(2, '0');
  const pct   = (countdown / COUNTDOWN_S) * 100;
  const color = countdown <= 60 ? '#ef4444' : countdown <= 120 ? '#f97316' : '#2563eb';

  const overlay = {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 99999, fontFamily: "'Poppins', system-ui, sans-serif",
  };
  const box = {
    background: '#fff', borderRadius: 18, padding: '40px 36px',
    maxWidth: 420, width: '90%', textAlign: 'center',
    boxShadow: '0 24px 64px rgba(0,0,0,0.3)',
  };

  return (
    <div style={overlay}>
      <div style={box}>
        <div style={{ fontSize: 56, marginBottom: 8 }}>⏳</div>
        <h2 style={{ margin: '0 0 8px', color: '#1e293b', fontSize: 20 }}>
          ¿Sigues ahí?
        </h2>
        <p style={{ color: '#64748b', fontSize: 14, margin: '0 0 24px', lineHeight: 1.6 }}>
          No se detectó actividad en los últimos 10 minutos.<br />
          La sesión se cerrará automáticamente en:
        </p>

        {/* Círculo de cuenta regresiva */}
        <div style={{ position: 'relative', width: 110, height: 110, margin: '0 auto 24px' }}>
          <svg width="110" height="110" style={{ transform: 'rotate(-90deg)' }}>
            <circle cx="55" cy="55" r="48" fill="none" stroke="#e2e8f0" strokeWidth="8" />
            <circle
              cx="55" cy="55" r="48" fill="none"
              stroke={color} strokeWidth="8"
              strokeDasharray={`${2 * Math.PI * 48}`}
              strokeDashoffset={`${2 * Math.PI * 48 * (1 - pct / 100)}`}
              strokeLinecap="round"
              style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.5s' }}
            />
          </svg>
          <div style={{
            position: 'absolute', inset: 0,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexDirection: 'column',
          }}>
            <span style={{ fontSize: 24, fontWeight: 700, color, fontVariantNumeric: 'tabular-nums' }}>
              {mins}:{secs}
            </span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
          <button
            onClick={onSalir}
            style={{
              flex: 1, padding: '12px 0', borderRadius: 10, border: 'none',
              background: '#ef4444', color: '#fff', fontWeight: 600,
              fontSize: 14, cursor: 'pointer',
              boxShadow: '0 2px 8px rgba(239,68,68,0.3)',
            }}
          >
            🚪 Cerrar sesión
          </button>
          <button
            onClick={onContinuar}
            style={{
              flex: 1, padding: '12px 0', borderRadius: 10, border: 'none',
              background: '#16a34a', color: '#fff', fontWeight: 600,
              fontSize: 14, cursor: 'pointer',
              boxShadow: '0 2px 8px rgba(22,163,74,0.3)',
            }}
          >
            ✅ Continuar
          </button>
        </div>
      </div>
    </div>
  );
}

function App() {
  const navigate = useNavigate();
  const [showAfkModal, setShowAfkModal]   = useState(false);
  const [afkCountdown, setAfkCountdown]   = useState(COUNTDOWN_S);
  const warningTimerRef      = useRef(null);
  const logoutTimerRef       = useRef(null);
  const countdownIntervalRef = useRef(null);
  const resetAfkRef          = useRef(null);
  const showAfkModalRef      = useRef(false); // ref para leer en el event handler sin closure stale

  useEffect(() => {
    const user = getUser();
    if (!user?.rol) {
      getMe()
        .then(data => {
          saveSession(null, data.rol, data.username, data.idEmpleado ?? null);
          navigate(DASH[data.rol] ?? '/', { replace: true });
        })
        .catch(() => {});
    }
  }, [navigate]);

  const handleLogout = useCallback(() => {
    clearTimeout(warningTimerRef.current);
    clearTimeout(logoutTimerRef.current);
    clearInterval(countdownIntervalRef.current);
    showAfkModalRef.current = false;
    setShowAfkModal(false);
    clearSession();
    navigate('/', { replace: true });
  }, [navigate]);

  const resetAfk = useCallback(() => {
    clearTimeout(warningTimerRef.current);
    clearTimeout(logoutTimerRef.current);
    clearInterval(countdownIntervalRef.current);
    showAfkModalRef.current = false;
    setShowAfkModal(false);
    setAfkCountdown(COUNTDOWN_S);

    // A los 10 min → mostrar modal y arrancar countdown
    warningTimerRef.current = setTimeout(() => {
      showAfkModalRef.current = true;
      setShowAfkModal(true);
      setAfkCountdown(COUNTDOWN_S);
      let remaining = COUNTDOWN_S;
      countdownIntervalRef.current = setInterval(() => {
        remaining -= 1;
        setAfkCountdown(remaining);
        if (remaining <= 0) {
          clearInterval(countdownIntervalRef.current);
          handleLogout();
        }
      }, 1000);
    }, WARNING_MS);

    // A los 15 min → logout directo (seguro si el usuario ignora el modal)
    logoutTimerRef.current = setTimeout(handleLogout, LOGOUT_MS);
  }, [handleLogout]);

  // Guardar referencia actualizada para el event handler
  useEffect(() => { resetAfkRef.current = resetAfk; }, [resetAfk]);

  useEffect(() => {
    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];
    // Usa el ref para evitar el closure stale de showAfkModal
    const handler = () => {
      if (!showAfkModalRef.current) resetAfkRef.current?.();
    };
    events.forEach(e => window.addEventListener(e, handler, { passive: true }));
    resetAfkRef.current?.();
    return () => {
      clearTimeout(warningTimerRef.current);
      clearTimeout(logoutTimerRef.current);
      clearInterval(countdownIntervalRef.current);
      events.forEach(e => window.removeEventListener(e, handler));
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <ErrorBoundary>
    <Setup2FAModal />
    {showAfkModal && (
      <AfkModal
        countdown={afkCountdown}
        onContinuar={resetAfk}
        onSalir={handleLogout}
      />
    )}
    <Routes>
      {/* Auth (rutas públicas) */}
      <Route path="/" element={<Login />} />
      <Route path="/reset-password" element={<ResetPassword />} />

      {/* RRHH */}
      <Route path="/rrhh" element={<ProtectedRoute requiredRole="RRHH"><DashboardRRHH /></ProtectedRoute>} />
      <Route path="/rrhh/empleados" element={<ProtectedRoute requiredRole="RRHH"><GestionEmpleados /></ProtectedRoute>} />
      <Route path="/rrhh/horarios" element={<ProtectedRoute requiredRole="RRHH"><GestionHorarios /></ProtectedRoute>} />
      <Route path="/rrhh/asistencia" element={<ProtectedRoute requiredRole="RRHH"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/rrhh/nomina" element={<ProtectedRoute requiredRole="RRHH"><CalculoNomina /></ProtectedRoute>} />
      <Route path="/rrhh/solicitudes" element={<ProtectedRoute requiredRole="RRHH"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/rrhh/reportes" element={<ProtectedRoute requiredRole="RRHH"><GenerarReportes /></ProtectedRoute>} />

      {/* Empleado */}
      <Route path="/empleado" element={<ProtectedRoute requiredRole="EMPLEADO"><DashboardEmpleado /></ProtectedRoute>} />
      <Route path="/empleado/asistencia" element={<ProtectedRoute requiredRole="EMPLEADO"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/empleado/solicitudes" element={<ProtectedRoute requiredRole="EMPLEADO"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/empleado/nomina" element={<ProtectedRoute requiredRole="EMPLEADO"><NominaEmpleado /></ProtectedRoute>} />

      {/* Gerencia */}
      <Route path="/gerencia" element={<ProtectedRoute requiredRole="GERENCIA"><DashboardGerencia /></ProtectedRoute>} />
      <Route path="/gerencia/solicitudes" element={<ProtectedRoute requiredRole="GERENCIA"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/gerencia/asistencia" element={<ProtectedRoute requiredRole="GERENCIA"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/gerencia/justificaciones" element={<ProtectedRoute requiredRole="GERENCIA"><GerenciaJustificaciones /></ProtectedRoute>} />

      {/* DEV — ELIMINAR ANTES DE PRODUCCIÓN */}
      <Route path="/dev" element={<DevLogin />} />

      {/* Catch-all: URL inexistente → autocorrector */}
      <Route path="*" element={<AutoRedirect />} />
    </Routes>
    </ErrorBoundary>
  );
}

export default App;
