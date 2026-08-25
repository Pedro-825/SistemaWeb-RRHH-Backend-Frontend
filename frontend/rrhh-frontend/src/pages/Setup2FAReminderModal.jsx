import { useState } from "react";
import { enable2FA, verify2FA, saveSession, getUser } from "../services/api";

export default function Setup2FAReminderModal({ username, onDone }) {
  const [step,      setStep]      = useState(1); // 1=aviso 2=QR 3=verificar 4=éxito
  const [qrUrl,     setQrUrl]     = useState(null);
  const [loading,   setLoading]   = useState(false);
  const [code,      setCode]      = useState('');
  const [codeError, setCodeError] = useState('');
  const [activarError, setActivarError] = useState('');

  const cerrar = () => {
    if (qrUrl) URL.revokeObjectURL(qrUrl);
    onDone();
  };

  const handleActivar = async () => {
    setLoading(true);
    setActivarError('');
    try {
      const blob = await enable2FA(username);
      setQrUrl(URL.createObjectURL(blob));
      setStep(2);
    } catch (err) {
      setActivarError(err?.message || 'No se pudo generar el código QR. Intente de nuevo.');
    }
    finally { setLoading(false); }
  };

  const handleVerificar = async () => {
    if (code.length < 6) { setCodeError('Ingresa el código de 6 dígitos'); return; }
    setLoading(true);
    setCodeError('');
    try {
      const data = await verify2FA(username, code);
      if (data.success) {
        const user = getUser();
        saveSession(data.token, data.rol, data.nombreUsuario || username, data.idEmpleado ?? user?.idEmpleado ?? null);
        setStep(4);
      } else {
        setCodeError(data.message || 'Código incorrecto, intenta nuevamente');
        setCode('');
      }
    } catch { setCodeError('Error de conexión'); }
    finally { setLoading(false); }
  };

  const overlay = {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.55)',
    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 5000,
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
          {activarError && <p style={{ color: '#dc2626', fontSize: 13, margin: '8px 0 0' }}>⚠️ {activarError}</p>}
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
