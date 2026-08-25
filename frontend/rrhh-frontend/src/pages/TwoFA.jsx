import { useState, useEffect, useRef } from "react";
import "../styles/TwoFA.css";
import { verify2FA, saveSession } from "../services/api";

export default function TwoFA({ open, username, onClose, onSuccess }) {
  const [code,     setCode]     = useState(["", "", "", "", "", ""]);
  const [time,     setTime]     = useState(300);
  const [intentos, setIntentos] = useState(5);
  const [error,    setError]    = useState("");
  const [loading,  setLoading]  = useState(false);

  const intervalRef = useRef(null);
  const inputRefs   = useRef([]);

  useEffect(() => {
    if (open) inputRefs.current[0]?.focus();
  }, [open]);

  useEffect(() => {
    if (!open) return;
    intervalRef.current = setInterval(() => {
      setTime(prev => {
        if (prev <= 1) { clearInterval(intervalRef.current); onClose(); return 0; }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(intervalRef.current);
  }, [open, onClose]);

  const handleChange = (value, index) => {
    if (!/^[0-9]?$/.test(value)) return;
    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    const paste = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (paste.length === 6) {
      setCode(paste.split(""));
      inputRefs.current[5]?.focus();
    }
  };

  const verify = async () => {
    const finalCode = code.join("");
    if (finalCode.length < 6) { setError("Ingresa el código completo de 6 dígitos"); return; }

    setLoading(true);
    setError("");
    try {
      const data = await verify2FA(username, finalCode);
      if (data.success) {
        saveSession(data.token, data.rol, data.nombreUsuario || username, data.idEmpleado ?? null, data.requiereCambioPassword ?? false);
        onSuccess(data.rol, data.idEmpleado ?? null, data.requiereCambioPassword ?? false, data.appMovilInstalada ?? false);
        onClose();
      } else {
        const nuevosIntentos = intentos - 1;
        setIntentos(nuevosIntentos);
        setError(data.message || "El token de sesión es incorrecto");
        setCode(["", "", "", "", "", ""]);
        inputRefs.current[0]?.focus();
        if (nuevosIntentos <= 0) {
          setError("Cuenta bloqueada por exceso de intentos. Intente en 5 minutos.");
          setTimeout(onClose, 3000);
        }
      }
    } catch {
      setError("Error de conexión con el servidor");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = () => {
    setCode(["", "", "", "", "", ""]);
    setError("");
    setTime(300);
    setIntentos(5);
    inputRefs.current[0]?.focus();
  };

  const fmt = (s) =>
    `${Math.floor(s / 60).toString().padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;

  if (!open) return null;

  return (
    <div className="twofa-overlay">
      <div className="twofa-card">
        <div className="twofa-lock-wrap">🔒</div>

        <h2 className="twofa-title">Verificación en dos pasos</h2>
        <p className="twofa-subtitle">
          Ingrese el código de 6 dígitos generado por su aplicación autenticadora.
        </p>

        <div className="twofa-info-box">
          ℹ️ Abra Google Authenticator o Authy y use el código de 6 dígitos que se muestra para su cuenta.
        </div>

        <p className="twofa-code-label">Código de verificación</p>
        <div className="twofa-code-box" onPaste={handlePaste}>
          {code.map((c, i) => (
            <input
              key={i}
              ref={el => (inputRefs.current[i] = el)}
              maxLength="1"
              value={c}
              onChange={e => handleChange(e.target.value, i)}
              onKeyDown={e => handleKeyDown(e, i)}
              inputMode="numeric"
            />
          ))}
        </div>

        <p className="twofa-timer">
          El código expira en <strong>{fmt(time)}</strong>
        </p>

        {intentos < 5 && (
          <p className="twofa-attempts">Intentos restantes: <span>{intentos}</span></p>
        )}

        {error && <p className="twofa-error">{error}</p>}

        <button
          onClick={verify}
          disabled={code.some(c => c === "") || loading}
          className="twofa-button"
        >
          {loading ? "Verificando..." : "🔒 Verificar código"}
        </button>

        <button className="twofa-resend" onClick={handleResend}>
          🔄 Limpiar campos
        </button>

        <p className="twofa-hint">
          El código se renueva cada 30 segundos en su aplicación autenticadora.
        </p>
      </div>
    </div>
  );
}
