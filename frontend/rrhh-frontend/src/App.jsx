import { Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState, useRef, useCallback, lazy, Suspense } from "react";
import { getUser, clearSession, saveSession, getMe, logout } from "./services/api";
import Acceso403 from "./pages/Acceso403";
import ErrorBoundary from "./components/ErrorBoundary";

const Login                = lazy(() => import("./pages/Login"));
const DashboardRRHH        = lazy(() => import("./pages/DashboardRRHH"));
const DashboardEmpleado    = lazy(() => import("./pages/DashboardEmpleado"));
const DashboardGerencia    = lazy(() => import("./pages/DashboardGerencia"));
const GestionEmpleados     = lazy(() => import("./pages/GestionEmpleados"));
const ControlAsistencia    = lazy(() => import("./pages/ControlAsistencia"));
const CalculoNomina        = lazy(() => import("./pages/CalculoNomina"));
const SolicitudPermisos    = lazy(() => import("./pages/SolicitudPermisos"));
const GenerarReportes      = lazy(() => import("./pages/GenerarReportes"));
const NominaEmpleado       = lazy(() => import("./pages/NominaEmpleado"));
const GerenciaJustificaciones = lazy(() => import("./pages/GerenciaJustificaciones"));
const ResetPassword        = lazy(() => import("./pages/ResetPassword"));
const GestionHorarios      = lazy(() => import("./pages/GestionHorarios"));
const DevLogin             = lazy(() => import("./pages/DevLogin")); // ELIMINAR ANTES DE PRODUCCIÓN

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

function ProtectedRoute({ children, requiredRole }) {
  const user = getUser();
  if (!user?.rol) return <Navigate to="/" replace />;
  const normalizeRole = (rol) => String(rol || '').replace(/^ROLE_/i, '').toUpperCase();
  if (requiredRole && normalizeRole(user.rol) !== normalizeRole(requiredRole)) return <Acceso403 />;
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
  const location = useLocation();
  const sessionActive = Boolean(getUser()?.rol) && !['/', '/reset-password', '/dev'].includes(location.pathname);
  const [showAfkModal, setShowAfkModal]   = useState(false);
  const [afkCountdown, setAfkCountdown]   = useState(COUNTDOWN_S);
  const [apiError, setApiError] = useState(null);
  const warningTimerRef      = useRef(null);
  const logoutTimerRef       = useRef(null);
  const countdownIntervalRef = useRef(null);
  const resetAfkRef          = useRef(null);
  const showAfkModalRef      = useRef(false); // ref para leer en el event handler sin closure stale

  useEffect(() => {
    const handler = (event) => {
      setApiError(event.detail?.message || 'Ocurrio un error al comunicarse con el servidor.');
      setTimeout(() => setApiError(null), 6500);
    };
    window.addEventListener('api-error', handler);
    return () => window.removeEventListener('api-error', handler);
  }, []);

  useEffect(() => {
    // Si hay un primer acceso pendiente y NO estamos en reset-password, volver al login
    if (sessionStorage.getItem('primerAccesoPendiente') && !location.pathname.startsWith('/reset-password')) {
      navigate('/', { replace: true });
      return;
    }
    const user = getUser();
    if (!user?.rol) {
      getMe()
        .then(data => {
          saveSession(null, data.rol, data.username, data.idEmpleado ?? null);
          navigate(DASH[data.rol] ?? '/', { replace: true });
        })
        .catch(() => {});
    }
  }, [navigate, location.pathname]);

  const handleLogout = useCallback(async () => {
    clearTimeout(warningTimerRef.current);
    clearTimeout(logoutTimerRef.current);
    clearInterval(countdownIntervalRef.current);
    showAfkModalRef.current = false;
    setShowAfkModal(false);
    try { await logout(); } catch {}
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

  const handleContinuar = useCallback(async () => {
    // Cancelar de inmediato el cierre forzado (15 min) para que no dispare
    // mientras se espera la verificación de sesión con el servidor.
    clearTimeout(warningTimerRef.current);
    clearTimeout(logoutTimerRef.current);
    clearInterval(countdownIntervalRef.current);
    try {
      await getMe();
      resetAfk();
    } catch {
      handleLogout();
    }
  }, [resetAfk, handleLogout]);

  // Guardar referencia actualizada para el event handler
  useEffect(() => { resetAfkRef.current = resetAfk; }, [resetAfk]);

  useEffect(() => {
    const events = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart'];
    if (!sessionActive) {
      clearTimeout(warningTimerRef.current);
      clearTimeout(logoutTimerRef.current);
      clearInterval(countdownIntervalRef.current);
      showAfkModalRef.current = false;
      return;
    }

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
  }, [sessionActive]);

  return (
    <ErrorBoundary>
    {apiError && (
      <div style={{
        position: 'fixed',
        top: 16,
        right: 16,
        zIndex: 10000,
        maxWidth: 420,
        background: '#7f1d1d',
        color: '#fff',
        border: '1px solid #fca5a5',
        borderRadius: 10,
        padding: '12px 16px',
        boxShadow: '0 16px 40px rgba(0,0,0,0.22)',
        fontFamily: "'Poppins', system-ui, sans-serif",
        fontSize: 13,
        lineHeight: 1.45,
      }}>
        {apiError}
      </div>
    )}
    {sessionActive && showAfkModal && (
      <AfkModal
        countdown={afkCountdown}
        onContinuar={handleContinuar}
        onSalir={handleLogout}
      />
    )}
    <Suspense fallback={<div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#f8fafc", fontFamily: "system-ui", color: "#64748b" }}>Cargando...</div>}>
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
      <Route path="/empleado/nomina"   element={<ProtectedRoute requiredRole="EMPLEADO"><NominaEmpleado /></ProtectedRoute>} />


      {/* Gerencia */}
      <Route path="/gerencia" element={<ProtectedRoute requiredRole="GERENCIA"><DashboardGerencia /></ProtectedRoute>} />
      <Route path="/gerencia/solicitudes" element={<ProtectedRoute requiredRole="GERENCIA"><SolicitudPermisos /></ProtectedRoute>} />
      <Route path="/gerencia/asistencia" element={<ProtectedRoute requiredRole="GERENCIA"><ControlAsistencia /></ProtectedRoute>} />
      <Route path="/gerencia/justificaciones" element={<ProtectedRoute requiredRole="GERENCIA"><GerenciaJustificaciones /></ProtectedRoute>} />

      {/* DEV — solo disponible en desarrollo */}
      {import.meta.env.DEV && <Route path="/dev" element={<DevLogin />} />}

      {/* Catch-all: URL inexistente → autocorrector */}
      <Route path="*" element={<AutoRedirect />} />
    </Routes>
    </Suspense>
    </ErrorBoundary>
  );
}

export default App;
