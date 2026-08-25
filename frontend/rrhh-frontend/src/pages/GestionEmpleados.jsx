import { useState, useEffect, useRef } from "react";
import Sidebar from "../components/Sidebar";
import Spinner from "../components/Spinner";
import ApiAlert from "../components/ApiAlert";
import "../styles/Dashboard.css";
import {
  listarEmpleados,
  buscarEmpleados,
  buscarEmpleadosAvanzado,
  registrarEmpleado,
  actualizarEmpleado,
  desactivarEmpleado,
  reactivarEmpleado,
  registrarSancion,
  registrarAscenso,
  getDepartamentos,
  getHistorialEmpleado,
  getAsignacionesActivas,
  getBeneficiosEmpleado,
  guardarPerfilBeneficios,
  agregarFamiliarEmpleado,
  actualizarFamiliarEmpleado,
  eliminarFamiliarEmpleado,
  getUser,
} from "../services/api";

const FORM_VACIO = {
  nombres: "", apellidos: "", docIdentidad: "DNI", numeroDi: "",
  fechaNac: "", sexo: "", estadoCivil: "", direccion: "",
  correo: "", telefono: "", cargo: "", rol: "", tipoContrato: "",
  fechaInicio: "", fechaFin: "", sueldo: "", idDpto: "",
};

const PAGE_SIZES = [10, 25, 50];
const EMPLEADOS_CACHE_TTL_MS = 60_000;

const PERFIL_BENEFICIOS_VACIO = {
  regimenSalud: "ESSALUD",
  regimenPension: "ONP",
  afp: "",
  cuspp: "",
  tipoComisionAfp: "",
  contactoEmergencia: "",
  telefonoEmergencia: "",
  vigenteDesde: "",
  vigenteHasta: "",
};

const FAMILIAR_VACIO = {
  nombres: "",
  apellidos: "",
  parentesco: "HIJO",
  numeroDi: "",
  fechaNacimiento: "",
  esDerechohabiente: true,
  elegibleAsignacionFamiliar: true,
  validado: false,
  coberturaDesde: "",
  coberturaHasta: "",
};


const ESTADO_EMPLEADO_INFO = {
  ACTIVO:       { label: "Activo",       clase: "emp-status-active" },
  SUSPENDIDO:   { label: "Suspendido",   clase: "emp-status-suspended" },
  DESVINCULADO: { label: "Desvinculado", clase: "emp-status-inactive" },
};

const DEPT_CARGOS = {
  "Recursos Humanos":  ["Especialista RRHH", "Analista RRHH", "Coordinador RRHH", "Asistente RRHH", "Auxiliar RRHH", "Administrador RRHH"],
  "Dirección General": ["Director General", "Subgerente", "Gerente Administrativo", "Asistente de Gerencia"],
  "Emergencia":        ["Médico de Emergencia", "Paramédico", "Triagista", "Técnico de Emergencias"],
  "Enfermería":        ["Enfermera Jefe", "Enfermera", "Enfermero", "Auxiliar de Enfermería"],
  "Medicina":          ["Médico General", "Médico Especialista", "Médico Residente", "Médico de Guardia"],
  "Administración":    ["Administrador", "Administradora", "Recepcionista", "Asistente Administrativo", "Secretaria"],
  "Laboratorio":       ["Técnico de Laboratorio", "Analista de Laboratorio", "Biólogo", "Químico Farmacéutico"],
  "Finanzas":          ["Contador", "Contadora", "Analista Financiero", "Tesorero", "Asistente Contable"],
  "Radiología":        ["Radiólogo", "Técnico en Radiología", "Operador de Equipos de Imagen"],
};

const cacheKey = (name, payload = "") => `rrhh:${name}:${payload}`;

const leerCache = (key) => {
  try {
    const raw = sessionStorage.getItem(key);
    if (!raw) return null;
    const data = JSON.parse(raw);
    if (!data?.expiresAt || data.expiresAt < Date.now()) {
      sessionStorage.removeItem(key);
      return null;
    }
    return data.value;
  } catch {
    return null;
  }
};

const guardarCache = (key, value) => {
  try {
    sessionStorage.setItem(key, JSON.stringify({
      value,
      expiresAt: Date.now() + EMPLEADOS_CACHE_TTL_MS,
    }));
  } catch {
    // El cache solo mejora la experiencia; si falla, no bloquea la pantalla.
  }
};

const normalizarRol = (rol) => String(rol || "").trim().toUpperCase().replace(/^ROLE_/, "");

const formatearCampoAuditoria = (campo) => String(campo || "")
  .replace(/([a-z])([A-Z])/g, "$1 $2")
  .replace(/_/g, " ")
  .trim()
  .replace(/\b\w/g, c => c.toUpperCase());

const dividirValorHistorial = (valor) => {
  if (valor == null || valor === "") return ["—"];
  const raw = String(valor).trim();
  const sinLlaves = raw.startsWith("{") && raw.endsWith("}")
    ? raw.slice(1, -1)
    : raw;
  const partes = sinLlaves
    .split(/\s+\|\s+|,\s*(?=[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9_]+\s*=)/)
    .map(p => p.trim())
    .filter(Boolean);

  if (partes.length === 0) return ["—"];

  return partes.map(parte => {
    const separador = parte.includes("=") ? "=" : parte.includes(":") ? ":" : null;
    if (!separador) return parte;
    const idx = parte.indexOf(separador);
    const campo = formatearCampoAuditoria(parte.slice(0, idx));
    const contenido = parte.slice(idx + 1).trim() || "—";
    return `${campo}: ${contenido}`;
  });
};

const ValorHistorial = ({ valor, muted = false }) => (
  <span style={{ display: "flex", flexDirection: "column", gap: 3, whiteSpace: "normal", wordBreak: "break-word" }}>
    {dividirValorHistorial(valor).map((linea, idx) => (
      <span key={`${linea}-${idx}`} style={{ color: muted ? "var(--text-3)" : undefined, lineHeight: 1.35 }}>
        {linea}
      </span>
    ))}
  </span>
);

export default function GestionEmpleados() {
  const [empleados,     setEmpleados]     = useState([]);
  const [loading,       setLoading]       = useState(true);
  const [error,         setError]         = useState("");
  const [mensaje,       setMensaje]       = useState("");
  const [mensajeOk,     setMensajeOk]     = useState(true); // true=éxito, false=error
  const [totalElements, setTotalElements] = useState(0);
  const [page,          setPage]          = useState(0);
  const [pageSize,      setPageSize]      = useState(10);
  const [fetchKey,      setFetchKey]      = useState(0);

  // Departamentos (cargados del backend)
  const [departamentos, setDepartamentos] = useState([]);

  // Filters
  const [searchName,  setSearchName]  = useState("");
  const [filterArea,  setFilterArea]  = useState("");
  const [filterCargo, setFilterCargo] = useState("");
  const [filterEstado, setFilterEstado] = useState("");
  const [filterDocType, setFilterDocType] = useState("");

  // Forms
  const [form,        setForm]        = useState(FORM_VACIO);
  const [sancionForm, setSancionForm] = useState({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
  const [ascensoForm, setAscensoForm] = useState({ idEmpleado: "", nuevoCargo: "", nuevoRol: "", nuevoSueldo: "" });

  // Modals
  const [modalNuevo,      setModalNuevo]      = useState(false);
  const [registrandoEmpleado, setRegistrandoEmpleado] = useState(false);
  const [modalEditar,     setModalEditar]     = useState(null);
  const [modalDesactivar, setModalDesactivar] = useState({ open: false, id: null, motivo: "" });
  const [modalReactivar,  setModalReactivar]  = useState({ open: false, id: null });
  const [modalSancion,    setModalSancion]    = useState({ open: false, id: null });
  const [modalAscenso,    setModalAscenso]    = useState({ open: false, id: null });
  const [, setShowFechas]      = useState(false);

  // Dropdown per row
  const [openDrop, setOpenDrop] = useState(null);
  const dropRef = useRef(null);

  // Historial modal
  const [modalHistorial, setModalHistorial] = useState({ open: false, empleado: null });
  const [historialData,  setHistorialData]  = useState([]);
  const [historialLoad,  setHistorialLoad]  = useState(false);

  // Ficha de beneficios y familia
  const [modalBeneficios, setModalBeneficios] = useState({ open: false, empleado: null });
  const [beneficiosLoad, setBeneficiosLoad] = useState(false);
  const [beneficiosSaving, setBeneficiosSaving] = useState(false);
  const [perfilBeneficios, setPerfilBeneficios] = useState(PERFIL_BENEFICIOS_VACIO);
  const [familiares, setFamiliares] = useState([]);
  const [familiarForm, setFamiliarForm] = useState(FAMILIAR_VACIO);
  const [editFamiliarId, setEditFamiliarId] = useState(null);

  // Horario
  const [turnosMap,     setTurnosMap]     = useState({});  // { idEmpleado: {nombreTurno, horaEntrada, horaSalida} }
  const usuarioActual = getUser();
  const empleadoAscenso = empleados.find(emp => Number(emp.idEmpleado) === Number(modalAscenso.id));
  const esAutoCambioRol = Number(usuarioActual?.idEmpleado) === Number(modalAscenso.id);
  const actorEsRrhh = normalizarRol(usuarioActual?.rol) === "RRHH";
  const empleadoAscensoEsRrhh = normalizarRol(empleadoAscenso?.rol) === "RRHH";
  const bloqueoRrhhSobreRrhh = actorEsRrhh && empleadoAscensoEsRrhh && !esAutoCambioRol;

  const cargarTurnosActivos = async () => {
    const key = cacheKey("turnos-activos");
    const cached = leerCache(key);
    if (cached) {
      setTurnosMap(cached);
    }
    try {
      const lista = await getAsignacionesActivas();
      const map = {};
      (lista || []).forEach(a => { map[a.idEmpleado] = a; });
      setTurnosMap(map);
      guardarCache(key, map);
    } catch {
      if (!cached) setTurnosMap({});
    }
  };

  useEffect(() => {
    getDepartamentos().then(setDepartamentos).catch(() => {});
    cargarTurnosActivos();
  }, []);

  useEffect(() => {
    const close = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) setOpenDrop(null);
    };
    document.addEventListener("mousedown", close);
    return () => {
      document.removeEventListener("mousedown", close);
    };
  }, []);

  // Efecto unificado: re-fetcha automáticamente al cambiar cualquier filtro, página o pageSize.
  // El campo de nombre usa debounce de 400ms para no disparar en cada tecla.
  useEffect(() => {
    let cancelled = false;

    const doFetch = async () => {
      const cachePayload = JSON.stringify({ searchName: searchName.trim(), filterArea, filterCargo, filterEstado, page, pageSize, fetchKey });
      const key = cacheKey("empleados", cachePayload);
      const cached = leerCache(key);
      if (cached) {
        setEmpleados(cached.list || []);
        setTotalElements(cached.totalElements || 0);
      }
      setLoading(!cached);
      setError("");
      try {
        let data;
        if (searchName.trim()) {
          data = await buscarEmpleados(searchName.trim(), page);
        } else if (filterArea || filterCargo || filterEstado) {
          const idDpto = filterArea
            ? departamentos.find(d => d.nombre === filterArea)?.idDpto
            : undefined;
          data = await buscarEmpleadosAvanzado({ estado: filterEstado, cargo: filterCargo, idDpto });
        } else {
          data = await listarEmpleados(page, pageSize);
        }
        if (!cancelled) {
          const list = data?.content ?? (Array.isArray(data) ? data : []);
          setEmpleados(list);
          setTotalElements(data?.totalElements ?? list.length);
          guardarCache(key, { list, totalElements: data?.totalElements ?? list.length });
        }
      } catch (err) {
        if (!cancelled) setError(err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (searchName.trim()) {
      const timer = setTimeout(doFetch, 400);
      return () => { cancelled = true; clearTimeout(timer); };
    }
    doFetch();
    return () => { cancelled = true; };
  }, [searchName, filterArea, filterCargo, filterEstado, page, pageSize, fetchKey]); // eslint-disable-line react-hooks/exhaustive-deps

  const cargarEmpleados = () => setFetchKey(k => k + 1);

  const cargarBeneficios = async (empleado) => {
    setModalBeneficios({ open: true, empleado });
    setBeneficiosLoad(true);
    setPerfilBeneficios(PERFIL_BENEFICIOS_VACIO);
    setFamiliares([]);
    setFamiliarForm(FAMILIAR_VACIO);
    setEditFamiliarId(null);
    try {
      const data = await getBeneficiosEmpleado(empleado.idEmpleado);
      setPerfilBeneficios({ ...PERFIL_BENEFICIOS_VACIO, ...(data?.perfil || {}) });
      setFamiliares(Array.isArray(data?.familiares) ? data.familiares : []);
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    } finally {
      setBeneficiosLoad(false);
    }
  };

  const guardarPerfilNomina = async () => {
    if (!modalBeneficios.empleado) return;
    if (!perfilBeneficios.contactoEmergencia?.trim() || !perfilBeneficios.telefonoEmergencia?.trim()) {
      setMensajeOk(false);
      setMensaje("Complete el contacto y teléfono de emergencia antes de guardar.");
      return;
    }
    setBeneficiosSaving(true);
    try {
      const limpio = { ...perfilBeneficios };
      if (limpio.regimenPension !== "AFP") {
        limpio.afp = "";
        limpio.cuspp = "";
        limpio.tipoComisionAfp = "";
      }
      const actualizado = await guardarPerfilBeneficios(modalBeneficios.empleado.idEmpleado, limpio);
      setPerfilBeneficios({ ...PERFIL_BENEFICIOS_VACIO, ...(actualizado || {}) });
      setMensajeOk(true);
      setMensaje("Perfil previsional actualizado. Se usará en el próximo cálculo de nómina.");
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    } finally {
      setBeneficiosSaving(false);
    }
  };

  const guardarFamiliar = async (e) => {
    e.preventDefault();
    if (!modalBeneficios.empleado) return;
    setBeneficiosSaving(true);
    try {
      const payload = {
        ...familiarForm,
        coberturaDesde: familiarForm.coberturaDesde || null,
        coberturaHasta: familiarForm.coberturaHasta || null,
      };
      const guardado = editFamiliarId
        ? await actualizarFamiliarEmpleado(modalBeneficios.empleado.idEmpleado, editFamiliarId, payload)
        : await agregarFamiliarEmpleado(modalBeneficios.empleado.idEmpleado, payload);
      setFamiliares(prev => editFamiliarId
        ? prev.map(f => Number(f.id) === Number(editFamiliarId) ? guardado : f)
        : [...prev, guardado]);
      setFamiliarForm(FAMILIAR_VACIO);
      setEditFamiliarId(null);
      setMensajeOk(true);
      setMensaje("Familiar actualizado. Si es hijo validado, impactará en la asignación familiar.");
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    } finally {
      setBeneficiosSaving(false);
    }
  };

  const editarFamiliar = (f) => {
    setEditFamiliarId(f.id);
    setFamiliarForm({
      ...FAMILIAR_VACIO,
      nombres: f.nombres || "",
      apellidos: f.apellidos || "",
      parentesco: f.parentesco || "HIJO",
      numeroDi: f.numeroDi || "",
      fechaNacimiento: f.fechaNacimiento || "",
      esDerechohabiente: Boolean(f.esDerechohabiente),
      elegibleAsignacionFamiliar: Boolean(f.elegibleAsignacionFamiliar),
      validado: Boolean(f.validado),
      coberturaDesde: f.coberturaDesde || "",
      coberturaHasta: f.coberturaHasta || "",
    });
  };

  const quitarFamiliar = async (idFamiliar) => {
    if (!modalBeneficios.empleado) return;
    setBeneficiosSaving(true);
    try {
      await eliminarFamiliarEmpleado(modalBeneficios.empleado.idEmpleado, idFamiliar);
      setFamiliares(prev => prev.filter(f => Number(f.id) !== Number(idFamiliar)));
      if (Number(editFamiliarId) === Number(idFamiliar)) {
        setEditFamiliarId(null);
        setFamiliarForm(FAMILIAR_VACIO);
      }
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    } finally {
      setBeneficiosSaving(false);
    }
  };

  const handleBuscar = () => {
    if (page !== 0) setPage(0);
    else setFetchKey(k => k + 1);
  };

  const handleLimpiar = () => {
    setSearchName(""); setFilterArea(""); setFilterCargo(""); setFilterEstado(""); setFilterDocType("");
    setPage(0);
    setFetchKey(k => k + 1);
  };

  const handleRegistrar = async (e) => {
    e.preventDefault();
    setMensaje("");
    setRegistrandoEmpleado(true);
    try {
      const data = await registrarEmpleado({
        ...form,
        sueldo: parseFloat(form.sueldo),
        idDpto: parseInt(form.idDpto),
        fechaNac: form.fechaNac || null,
        fechaInicio: form.fechaInicio || null,
        fechaFin: form.fechaFin || null,
        correo: form.correo || "",
        estadoCivil: form.estadoCivil || null,
        sexo: form.sexo || null,
        contrasenia: form.numeroDi,
      });
      setMensajeOk(true);
      setMensaje(data?.message || "Empleado registrado correctamente");
      setForm(FORM_VACIO);
      setModalNuevo(false);
      cargarEmpleados();
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    } finally {
      setRegistrandoEmpleado(false);
    }
  };

  const handleGuardarEdicion = async (e) => {
    e.preventDefault();
    if (!modalEditar) return;
    try {
      await actualizarEmpleado(modalEditar.idEmpleado, {
        correo: modalEditar.correo,
        telefono: modalEditar.telefono,
        direccion: modalEditar.direccion,
        tipoContrato: modalEditar.tipoContrato,
        sueldo: parseFloat(modalEditar.sueldo),
        idDpto: parseInt(modalEditar.idDpto),
      });
      setMensaje("Datos del empleado actualizados correctamente");
      setModalEditar(null);
      cargarEmpleados();
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    }
  };

  const confirmarDesactivar = async () => {
    if (!modalDesactivar.motivo.trim()) return;
    try {
      const resp = await desactivarEmpleado(modalDesactivar.id, modalDesactivar.motivo);
      setMensaje(resp?.message || "Empleado desactivado correctamente");
      setMensajeOk(true);
      setModalDesactivar({ open: false, id: null, motivo: "" });
      cargarEmpleados();
      cargarTurnosActivos();
    } catch (err) {
      setMensaje(err);
      setMensajeOk(false);
      setModalDesactivar({ open: false, id: null, motivo: "" });
    }
  };

  const confirmarReactivar = async () => {
    try {
      const resp = await reactivarEmpleado(modalReactivar.id, "Reactivación manual");
      setMensaje(resp?.message || "Empleado reactivado correctamente");
      setMensajeOk(true);
      setModalReactivar({ open: false, id: null });
      cargarEmpleados();
      cargarTurnosActivos();
    } catch (err) {
      setMensaje(err);
      setMensajeOk(false);
      setModalReactivar({ open: false, id: null });
    }
  };

  const handleSancion = async (e) => {
    e.preventDefault();
    try {
      const resp = await registrarSancion(sancionForm.idEmpleado, {
        tipoSancion: sancionForm.tipoSancion,
        diasSuspension: parseInt(sancionForm.diasSuspension),
        justificacion: sancionForm.justificacion,
      });
      setMensajeOk(true);
      setMensaje(resp?.message || "Sanción registrada correctamente");
      setSancionForm({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
      setModalSancion({ open: false, id: null });
      cargarEmpleados();
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    }
  };

  const handleAscenso = async (e) => {
    e.preventDefault();
    if (!ascensoForm.nuevoRol) {
      setMensajeOk(false);
      setMensaje("Seleccione el nuevo rol del empleado.");
      return;
    }
    try {
      const resp = await registrarAscenso(ascensoForm.idEmpleado, {
        nuevoCargo: ascensoForm.nuevoCargo,
        nuevoRol: ascensoForm.nuevoRol,
      });
      setMensajeOk(true);
      setMensaje(resp?.message || "Ascenso registrado correctamente");
      setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoRol: "", nuevoSueldo: "" });
      setModalAscenso({ open: false, id: null });
      cargarEmpleados();
    } catch (err) {
      setMensajeOk(false);
      setMensaje(err);
    }
  };

  const openDropMenu = (id, e) => {
    e.preventDefault();
    e.stopPropagation();
    if (openDrop === id) { setOpenDrop(null); return; }
    setOpenDrop(id);
  };

  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));


  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content empleados-home">
        <header className="main-header header-rrhh empleados-header">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Gestión de Empleados</span>
            <h1>Gestión de Empleados</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Administre la información de los empleados de la clínica.
            </p>
          </div>
          <div style={{ display: "flex", gap: "10px", position: "relative", top: "10px" }}>
            <button className="btn btn-success" onClick={() => { setForm(FORM_VACIO); setShowFechas(false); setMensaje(""); setModalNuevo(true); }}>
              ＋ Nuevo Empleado
            </button>
          </div>
        </header>

        <ApiAlert type={mensajeOk ? "success" : "error"} message={mensaje} />
        <ApiAlert type="error" message={error} />

        {/* ── FILTER ROW ── */}
        <div className="filter-row empleados-filters">
          <div className="filter-group empleados-field">
            <label className="filter-label">Tipo Documento</label>
            <select className="filter-select" value={filterDocType} onChange={e => setFilterDocType(e.target.value)}>
              <option value="">Todos</option>
              <option value="DNI">DNI</option>
              <option value="CE">Carnet Ext.</option>
            </select>
          </div>
          <div className="filter-group empleados-field">
            <label className="filter-label">Departamento</label>
            <select className="filter-select" value={filterArea} onChange={e => { setFilterArea(e.target.value); setFilterCargo(""); }}>
              <option value="">Todos</option>
              {departamentos.map(d => (
                <option key={d.idDpto} value={d.nombre}>{d.nombre}</option>
              ))}
            </select>
          </div>
          <div className="filter-group empleados-field">
            <label className="filter-label">Cargo</label>
            <select className="filter-select" value={filterCargo} onChange={e => setFilterCargo(e.target.value)}>
              <option value="">Todos</option>
              {(filterArea && DEPT_CARGOS[filterArea]
                ? DEPT_CARGOS[filterArea]
                : [...new Set(empleados.map(e => e.cargo).filter(Boolean))].sort()
              ).map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
          <div className="filter-group empleados-field">
            <label className="filter-label">Estado</label>
            <select className="filter-select" value={filterEstado} onChange={e => setFilterEstado(e.target.value)}>
              <option value="">Todos</option>
              <option value="ACTIVO">Activo</option>
              <option value="SUSPENDIDO">Suspendido</option>
              <option value="DESVINCULADO">Desvinculado</option>
            </select>
          </div>
          <div className="filter-group filter-search empleados-field empleados-search">
            <label className="filter-label">Buscar por nombre</label>
            <div className="filter-search-wrap">
              <input className="filter-input" placeholder="Ingrese nombre" value={searchName}
                onChange={e => setSearchName(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleBuscar()} />
              <span className="filter-search-icon">🔍</span>
            </div>
          </div>
          <div className="empleados-filter-actions">
            <button className="btn btn-primary btn-sm empleados-search-btn" onClick={handleBuscar}>Buscar</button>
            <button className="btn btn-secondary btn-sm" onClick={handleLimpiar}>☰ Limpiar filtros</button>
          </div>
        </div>

        {/* ── TABLE ── */}
        <div className="section-card empleados-table-card" style={{ padding: 0 }}>
          <div className="table-wrapper" style={{ marginBottom: 0 }} ref={dropRef}>
            {loading ? (
              <Spinner />
            ) : (
              <table className="table-empleados">
                <thead>
                  <tr>
                    <th>Nombre Completo</th>
                    <th style={{ textAlign: "center" }}>Tipo Doc / DNI</th>
                    <th>Departamento</th>
                    <th>Cargo</th>
                    <th style={{ textAlign: "center" }}>Turno</th>
                    <th style={{ textAlign: "center" }}>Estado</th>
                    <th style={{ textAlign: "center" }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {(() => {
                    const visibles = filterDocType
                      ? empleados.filter(e => (e.docIdentidad ?? "DNI") === filterDocType)
                      : empleados;
                    if (visibles.length === 0) return (
                      <tr><td colSpan={7}>
                        <div className="empty-state">
                          <div className="empty-state-icon">🔍</div>
                          <p>No se encontraron empleados</p>
                        </div>
                      </td></tr>
                    );
                    return visibles.map(emp => (
                    <tr
                      key={emp.idEmpleado}
                      className={openDrop === emp.idEmpleado ? "empleado-row-dropdown-open" : ""}
                    >
                      <td data-label="Nombre Completo">{emp.nombres} {emp.apellidos}</td>
                      <td data-label="Tipo Doc / DNI" style={{ color: "var(--text-2)" }}>
                        {emp.docIdentidad ?? "DNI"} · {emp.numeroDi}
                      </td>
                      <td data-label="Departamento">{emp.departamento ?? "—"}</td>
                      <td data-label="Cargo">{emp.cargo ?? "—"}</td>
                      <td data-label="Turno" style={{ textAlign: "center" }}>
                        {turnosMap[emp.idEmpleado] ? (
                          <span style={{
                            display: "inline-flex", flexDirection: "column", alignItems: "center",
                            gap: 1, fontSize: 11,
                          }}>
                            <span style={{ fontWeight: 600, color: "var(--primary)", fontSize: 12 }}>
                              {turnosMap[emp.idEmpleado].nombreTurno}
                            </span>
                            <span style={{ color: "var(--text-3)" }}>
                              {turnosMap[emp.idEmpleado].horaEntrada?.slice(0,5)} – {turnosMap[emp.idEmpleado].horaSalida?.slice(0,5)}
                            </span>
                          </span>
                        ) : (
                          <span style={{ color: "var(--text-3)", fontSize: 12 }}>—</span>
                        )}
                      </td>
                      <td data-label="Estado">
                        <span className={`emp-status-dot ${ESTADO_EMPLEADO_INFO[emp.estado]?.clase ?? "emp-status-inactive"}`}>
                          {ESTADO_EMPLEADO_INFO[emp.estado]?.label ?? emp.estado ?? "—"}
                        </span>
                      </td>
                      <td data-label="Acciones">
                        <div style={{ display: "inline-flex", gap: "6px", alignItems: "center" }}>
                          {emp.estado !== "DESVINCULADO" && (
                            <button className="btn btn-secondary btn-sm"
                              onClick={() => setModalEditar({ ...emp })}>
                              ✏️ Editar
                            </button>
                          )}
                          <div className="action-drop-wrap">
                            <button type="button" className="action-drop-btn"
                              onClick={(e) => openDropMenu(emp.idEmpleado, e)}>
                              ⋯
                            </button>
                            {openDrop === emp.idEmpleado && (
                              <div className="action-drop-menu">
                                {emp.estado === "ACTIVO" ? (
                                  <div className="action-drop-item drop-red"
                                    onClick={() => { setModalDesactivar({ open: true, id: emp.idEmpleado, motivo: "" }); setOpenDrop(null); }}>
                                    <span className="action-drop-icon">🚫</span>
                                    <span>Desactivar empleado</span>
                                  </div>
                                ) : (
                                  <div className="action-drop-item drop-green"
                                    onClick={() => { setModalReactivar({ open: true, id: emp.idEmpleado }); setOpenDrop(null); }}>
                                    <span className="action-drop-icon">✅</span>
                                    <span>Reactivar empleado</span>
                                  </div>
                                )}
                                {emp.estado !== "DESVINCULADO" && (
                                  <div className="action-drop-item drop-yellow"
                                    onClick={() => { setModalSancion({ open: true, id: emp.idEmpleado }); setSancionForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                    <span className="action-drop-icon">⚠️</span>
                                    <span>Registrar sanción</span>
                                  </div>
                                )}
                                {emp.estado === "ACTIVO" && (
                                  <div className="action-drop-item drop-green"
                                    onClick={() => { setModalAscenso({ open: true, id: emp.idEmpleado }); setAscensoForm(f => ({ ...f, idEmpleado: emp.idEmpleado, nuevoCargo: "", nuevoRol: emp.rol || "" })); setOpenDrop(null); }}>
                                    <span className="action-drop-icon">↑</span>
                                    <span>Cambio de cargo/rol</span>
                                  </div>
                                )}
                                <div className="action-drop-item drop-gray"
                                  onClick={() => { setOpenDrop(null); cargarBeneficios(emp); }}>
                                  <span className="action-drop-icon">🧾</span>
                                  <span>Beneficios y familia</span>
                                </div>
                                <div className="action-drop-item drop-gray"
                                  onClick={async () => {
                                    setOpenDrop(null);
                                    setModalHistorial({ open: true, empleado: emp });
                                    setHistorialData([]);
                                    setHistorialLoad(true);
                                    try {
                                      const data = await getHistorialEmpleado(emp.idEmpleado);
                                      setHistorialData(Array.isArray(data) ? data : []);
                                    } catch {
                                      setHistorialData([]);
                                    } finally {
                                      setHistorialLoad(false);
                                    }
                                  }}>
                                  <span className="action-drop-icon">🕐</span>
                                  <span>Ver historial</span>
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                    </tr>
                  ));
                  })()}
                </tbody>
              </table>
            )}
          </div>

          {/* ── PAGINATION ── */}
          <div className="pagination-bar">
            <div className="pagination-left">
              Mostrar
              <select className="pagination-size-select" value={pageSize}
                onChange={e => { setPageSize(Number(e.target.value)); setPage(0); }}>
                {PAGE_SIZES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
              registros
            </div>
            <div className="pagination-btns">
              <button className="pagination-btn" onClick={() => setPage(0)} disabled={page === 0}>«</button>
              <button className="pagination-btn" onClick={() => setPage(p => p - 1)} disabled={page === 0}>‹</button>
              {Array.from({ length: Math.min(3, totalPages) }, (_, i) => (
                <button key={i} className={`pagination-btn ${page === i ? "active" : ""}`} onClick={() => setPage(i)}>
                  {i + 1}
                </button>
              ))}
              {totalPages > 4 && <span style={{ padding: "0 4px", color: "var(--text-3)" }}>...</span>}
              {totalPages > 3 && (
                <button className={`pagination-btn ${page === totalPages - 1 ? "active" : ""}`} onClick={() => setPage(totalPages - 1)}>
                  {totalPages}
                </button>
              )}
              <button className="pagination-btn" onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>›</button>
              <button className="pagination-btn" onClick={() => setPage(totalPages - 1)} disabled={page >= totalPages - 1}>»</button>
            </div>
            <div className="pagination-info">
              {page * pageSize + 1}–{Math.min((page + 1) * pageSize, totalElements)} de {totalElements} empleados
            </div>
          </div>
        </div>

      </main>

      {/* ══ MODAL: NUEVO EMPLEADO ══ */}
      {modalNuevo && (
        <div className="modal-overlay">
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div><h3>➕ Registrar Nuevo Empleado</h3></div>
              <button className="modal-close" onClick={() => { setModalNuevo(false); setForm(FORM_VACIO); setShowFechas(false); }}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info">
                Complete todos los campos obligatorios (*). El sistema verificará la duplicidad del DNI.
              </div>
              <form id="form-nuevo" onSubmit={handleRegistrar}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Tipo Documento *</label>
                    <select value={form.docIdentidad} onChange={e => setForm({ ...form, docIdentidad: e.target.value })}>
                      <option value="DNI">DNI</option>
                      <option value="CE">Carnet de Extranjería</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Número Documento *</label>
                    <input value={form.numeroDi} onChange={e => setForm({ ...form, numeroDi: e.target.value })} placeholder="Ej: 12345678" required />
                  </div>
                  <div className="form-group">
                    <label>Nombres *</label>
                    <input value={form.nombres} onChange={e => setForm({ ...form, nombres: e.target.value })} placeholder="Nombres completos" required />
                  </div>
                  <div className="form-group">
                    <label>Apellidos *</label>
                    <input value={form.apellidos} onChange={e => setForm({ ...form, apellidos: e.target.value })} placeholder="Apellidos completos" required />
                  </div>
                  <div className="form-group">
                    <label>Fecha de Nacimiento *</label>
                    <input type="date" value={form.fechaNac} onChange={e => setForm({ ...form, fechaNac: e.target.value })} required />
                  </div>
                  <div className="form-group">
                    <label>Sexo *</label>
                    <select value={form.sexo} onChange={e => setForm({ ...form, sexo: e.target.value })} required>
                      <option value="">Seleccione</option>
                      <option value="M">Masculino</option>
                      <option value="F">Femenino</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Estado Civil</label>
                    <select value={form.estadoCivil} onChange={e => setForm({ ...form, estadoCivil: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="SOLTERO">Soltero/a</option>
                      <option value="CASADO">Casado/a</option>
                      <option value="CONVIVIENTE">Conviviente</option>
                      <option value="DIVORCIADO">Divorciado/a</option>
                      <option value="VIUDO">Viudo/a</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Correo Personal</label>
                    <input type="email" value={form.correo} onChange={e => setForm({ ...form, correo: e.target.value })} placeholder="correo@ejemplo.com" />
                    <small style={{ color: "var(--text-3)", fontSize: "11px" }}>
                      El correo institucional se genera automáticamente al registrar.
                    </small>
                  </div>
                  <div className="form-group">
                    <label>Teléfono</label>
                    <input value={form.telefono} onChange={e => setForm({ ...form, telefono: e.target.value })} placeholder="999 999 999" />
                  </div>
                  <div className="form-group">
                    <label>Dirección *</label>
                    <input value={form.direccion} onChange={e => setForm({ ...form, direccion: e.target.value })} placeholder="Av. / Jr. / Calle, número, distrito" required />
                  </div>
                  <div className="form-group">
                    <label>Departamento *</label>
                    <select value={form.idDpto} onChange={e => setForm({ ...form, idDpto: e.target.value, cargo: "" })} required>
                      <option value="">Seleccione departamento</option>
                      {departamentos.map(d => (
                        <option key={d.idDpto} value={d.idDpto}>{d.nombre}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Cargo *</label>
                    <select
                      value={form.cargo}
                      onChange={e => setForm({ ...form, cargo: e.target.value })}
                      required
                      disabled={!form.idDpto}
                    >
                      <option value="">Seleccione cargo</option>
                      {(form.idDpto
                        ? DEPT_CARGOS[departamentos.find(d => d.idDpto === parseInt(form.idDpto))?.nombre] ?? []
                        : []
                      ).map(c => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Rol del sistema *</label>
                    <select value={form.rol} onChange={e => setForm({ ...form, rol: e.target.value })} required>
                      <option value="">Seleccione un rol</option>
                      <option value="EMPLEADO">Empleado</option>
                      <option value="RRHH">RRHH</option>
                      <option value="GERENCIA">Gerencia</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select
                      value={form.tipoContrato}
                      onChange={e => {
                        setForm({ ...form, tipoContrato: e.target.value, fechaInicio: "", fechaFin: "" });
                        setShowFechas(!!e.target.value);
                      }}
                    >
                      <option value="">Seleccione</option>
                      <option value="PLAZO FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="POR OBRA">Por obra/servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Sueldo Base (S/) *</label>
                    <input type="number" step="0.01" min="0" placeholder="0.00"
                      value={form.sueldo}
                      onChange={e => setForm({ ...form, sueldo: e.target.value })}
                      required />
                  </div>

                  {form.tipoContrato && (
                    <div className="form-group form-full">
                      <div style={{ display: "grid", gridTemplateColumns: form.tipoContrato === "INDEFINIDO" ? "1fr" : "1fr 1fr", gap: "12px" }}>
                        <div className="form-group" style={{ margin: 0 }}>
                          <label>Fecha Inicio Contrato {form.tipoContrato !== "INDEFINIDO" && "*"}</label>
                          <input
                            type="date"
                            value={form.fechaInicio}
                            onChange={e => setForm({ ...form, fechaInicio: e.target.value })}
                            required={form.tipoContrato !== "INDEFINIDO"}
                          />
                        </div>
                        {form.tipoContrato !== "INDEFINIDO" && (
                          <div className="form-group" style={{ margin: 0 }}>
                            <label>Fecha Fin Contrato *</label>
                            <input
                              type="date"
                              value={form.fechaFin}
                              onChange={e => setForm({ ...form, fechaFin: e.target.value })}
                              required
                              min={form.fechaInicio || undefined}
                            />
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => { setModalNuevo(false); setForm(FORM_VACIO); setShowFechas(false); }} disabled={registrandoEmpleado}>Cancelar</button>
              <button className="btn btn-success" type="submit" form="form-nuevo" disabled={registrandoEmpleado}>
                {registrandoEmpleado ? "Cargando" : "✅ Registrar Empleado"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: EDITAR ══ */}
      {modalEditar && (
        <div className="modal-overlay">
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div><h3>✏️ Editar Empleado</h3><p>{modalEditar.nombres} {modalEditar.apellidos} — {modalEditar.numeroDi}</p></div>
              <button className="modal-close" onClick={() => setModalEditar(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info">Los campos DNI, nombres y apellidos no son editables.</div>
              <form id="form-editar" onSubmit={handleGuardarEdicion}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Cargo actual</label>
                    <input value={modalEditar.cargo ?? ""} disabled />
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select value={modalEditar.tipoContrato ?? ""} onChange={e => setModalEditar({ ...modalEditar, tipoContrato: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="PLAZO FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="POR OBRA">Por obra/servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Sueldo Base (S/)</label>
                    <input type="number" step="0.01" min="0" placeholder="0.00"
                      value={modalEditar.sueldo ?? ""}
                      onChange={e => setModalEditar({ ...modalEditar, sueldo: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Departamento</label>
                    <select value={modalEditar.idDpto ?? ""} onChange={e => setModalEditar({ ...modalEditar, idDpto: e.target.value })}>
                      <option value="">Seleccione departamento</option>
                      {departamentos.map(d => (
                        <option key={d.idDpto} value={d.idDpto}>{d.nombre}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" value={modalEditar.correo ?? ""} onChange={e => setModalEditar({ ...modalEditar, correo: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Teléfono</label>
                    <input value={modalEditar.telefono ?? ""} onChange={e => setModalEditar({ ...modalEditar, telefono: e.target.value })} />
                  </div>
                  <div className="form-group form-full">
                    <label>Dirección</label>
                    <input value={modalEditar.direccion ?? ""} onChange={e => setModalEditar({ ...modalEditar, direccion: e.target.value })} />
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalEditar(null)}>Cancelar</button>
              <button className="btn btn-success" type="submit" form="form-editar">💾 Guardar Cambios</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: DESACTIVAR ══ */}
      {modalDesactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>🚫 Desactivar Empleado</h3><p>El acceso al sistema quedará bloqueado.</p></div>
              <button className="modal-close" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">⚠️ El historial se conservará. El empleado no podrá acceder al sistema.</div>
              <div className="form-group">
                <label>Motivo de desactivación *</label>
                <textarea rows={3} value={modalDesactivar.motivo}
                  onChange={e => setModalDesactivar({ ...modalDesactivar, motivo: e.target.value })}
                  placeholder="Describa el motivo..." autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>Cancelar</button>
              <button className="btn btn-danger" onClick={confirmarDesactivar} disabled={!modalDesactivar.motivo.trim()}>Confirmar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: REACTIVAR ══ */}
      {modalReactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>✅ Reactivar Empleado</h3></div>
              <button className="modal-close" onClick={() => setModalReactivar({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: "14px", color: "#475569" }}>¿Confirma la reactivación? El empleado recuperará acceso al sistema.</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalReactivar({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-success" onClick={confirmarReactivar}>Confirmar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: SANCIÓN ══ */}
      {modalSancion.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>⚠️ Registrar Sanción</h3><p>Empleado #{modalSancion.id}</p></div>
              <button className="modal-close" onClick={() => setModalSancion({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">⚠️ La justificación es obligatoria.</div>
              <form id="form-sancion" onSubmit={handleSancion}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Tipo de Sanción *</label>
                    <select value={sancionForm.tipoSancion}
                      onChange={e => setSancionForm({ ...sancionForm, tipoSancion: e.target.value })} required>
                      <option value="">Seleccione el tipo</option>
                      <option value="VERBAL">Amonestación verbal</option>
                      <option value="ESCRITA">Amonestación escrita</option>
                      <option value="SUSPENSION">Suspensión (días)</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Días de Suspensión</label>
                    <input type="number" min="0" value={sancionForm.diasSuspension}
                      onChange={e => setSancionForm({ ...sancionForm, diasSuspension: e.target.value })} />
                  </div>
                  <div className="form-group form-full">
                    <label>Justificación *</label>
                    <textarea value={sancionForm.justificacion}
                      onChange={e => setSancionForm({ ...sancionForm, justificacion: e.target.value })}
                      placeholder="Describa detalladamente el motivo..." required />
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalSancion({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-danger" type="submit" form="form-sancion">🚫 Registrar Sanción</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: CAMBIO DE CARGO/ROL ══ */}
      {modalAscenso.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>Cambio de cargo/rol</h3><p>{empleadoAscenso ? `${empleadoAscenso.nombres} ${empleadoAscenso.apellidos}` : `Empleado #${modalAscenso.id}`}</p></div>
              <button className="modal-close" onClick={() => setModalAscenso({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              {bloqueoRrhhSobreRrhh && (
                <div className="alert alert-warning">Un usuario RRHH no puede modificar el cargo o rol de otro usuario RRHH.</div>
              )}
              <form id="form-ascenso" onSubmit={handleAscenso}>
                <div className="form-group">
                  <label>Nuevo Cargo</label>
                  <input value={ascensoForm.nuevoCargo}
                    onChange={e => setAscensoForm({ ...ascensoForm, nuevoCargo: e.target.value })}
                    placeholder={empleadoAscenso?.cargo || "Ej: Jefe de Área, Especialista..."}
                    disabled={bloqueoRrhhSobreRrhh} />
                </div>
                <div className="form-group">
                  <label>Nuevo Rol</label>
                  <select value={ascensoForm.nuevoRol} required
                    onChange={e => setAscensoForm({ ...ascensoForm, nuevoRol: e.target.value })}
                    disabled={esAutoCambioRol || bloqueoRrhhSobreRrhh}>
                    <option value="">Seleccione un rol</option>
                    <option value="EMPLEADO">Empleado</option>
                    <option value="RRHH">RRHH</option>
                    <option value="GERENCIA">Gerencia</option>
                  </select>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalAscenso({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-primary" type="submit" form="form-ascenso" disabled={bloqueoRrhhSobreRrhh}>Guardar cambio</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: BENEFICIOS Y FAMILIA ══ */}
      {modalBeneficios.open && (
        <div className="modal-overlay">
          <div className="modal-box modal-box-wide beneficios-modal">
            <div className="modal-header">
              <div>
                <h3>🧾 Beneficios y familia</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  {modalBeneficios.empleado?.nombres} {modalBeneficios.empleado?.apellidos} · DNI {modalBeneficios.empleado?.numeroDi}
                </p>
              </div>
              <button className="modal-close" onClick={() => setModalBeneficios({ open: false, empleado: null })}>×</button>
            </div>
            <div className="modal-body">
              {beneficiosLoad ? (
                <Spinner />
              ) : (
                <>
                  <div className="alert alert-info">
                    Estos datos alimentan el cálculo de nómina: régimen pensionario, descuento legal y asignación familiar por hijos validados.
                  </div>

                  <section className="beneficios-section">
                    <div className="beneficios-section-head">
                      <h4>Perfil previsional y salud</h4>
                      <button className="btn btn-primary btn-sm" onClick={guardarPerfilNomina} disabled={beneficiosSaving}>
                        {beneficiosSaving ? "Guardando..." : "Guardar perfil"}
                      </button>
                    </div>
                    <div className="form-grid">
                      <div className="form-group">
                        <label>Régimen de salud</label>
                        <select value={perfilBeneficios.regimenSalud}
                          onChange={e => setPerfilBeneficios({ ...perfilBeneficios, regimenSalud: e.target.value })}>
                          <option value="ESSALUD">EsSalud</option>
                          <option value="EPS">EPS</option>
                          <option value="SIS">SIS</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>Régimen pensionario</label>
                        <select value={perfilBeneficios.regimenPension}
                          onChange={e => setPerfilBeneficios({ ...perfilBeneficios, regimenPension: e.target.value })}>
                          <option value="ONP">ONP</option>
                          <option value="AFP">AFP</option>
                        </select>
                      </div>
                      {perfilBeneficios.regimenPension === "AFP" && (
                        <>
                          <div className="form-group">
                            <label>AFP</label>
                            <input value={perfilBeneficios.afp}
                              onChange={e => setPerfilBeneficios({ ...perfilBeneficios, afp: e.target.value })}
                              placeholder="Integra, Prima, Profuturo..." />
                          </div>
                          <div className="form-group">
                            <label>CUSPP</label>
                            <input value={perfilBeneficios.cuspp}
                              onChange={e => setPerfilBeneficios({ ...perfilBeneficios, cuspp: e.target.value })}
                              placeholder="Código CUSPP" />
                          </div>
                          <div className="form-group">
                            <label>Tipo de comisión</label>
                            <select value={perfilBeneficios.tipoComisionAfp}
                              onChange={e => setPerfilBeneficios({ ...perfilBeneficios, tipoComisionAfp: e.target.value })}>
                              <option value="">Seleccione</option>
                              <option value="FLUJO">Flujo</option>
                              <option value="MIXTA">Mixta</option>
                            </select>
                          </div>
                        </>
                      )}
                      <div className="form-group">
                        <label>Contacto de emergencia</label>
                        <input value={perfilBeneficios.contactoEmergencia}
                          onChange={e => setPerfilBeneficios({ ...perfilBeneficios, contactoEmergencia: e.target.value })}
                          placeholder="Nombre completo" />
                      </div>
                      <div className="form-group">
                        <label>Teléfono de emergencia</label>
                        <input value={perfilBeneficios.telefonoEmergencia}
                          onChange={e => setPerfilBeneficios({ ...perfilBeneficios, telefonoEmergencia: e.target.value })}
                          placeholder="999 999 999" />
                      </div>
                    </div>
                  </section>

                  <section className="beneficios-section">
                    <div className="beneficios-section-head">
                      <h4>Carga familiar y cobertura</h4>
                      <span className="badge badge-info" style={{ fontSize: 11 }}>
                        {familiares.filter(f => f.parentesco === "HIJO" && f.elegibleAsignacionFamiliar && f.validado).length} hijo(s) con asignación familiar
                      </span>
                    </div>

                    <form onSubmit={guardarFamiliar} className="beneficios-family-form">
                      <div className="form-grid">
                        <div className="form-group">
                          <label>Nombres *</label>
                          <input value={familiarForm.nombres}
                            onChange={e => setFamiliarForm({ ...familiarForm, nombres: e.target.value })}
                            required />
                        </div>
                        <div className="form-group">
                          <label>Apellidos</label>
                          <input value={familiarForm.apellidos}
                            onChange={e => setFamiliarForm({ ...familiarForm, apellidos: e.target.value })} />
                        </div>
                        <div className="form-group">
                          <label>Parentesco</label>
                          <select value={familiarForm.parentesco}
                            onChange={e => setFamiliarForm({ ...familiarForm, parentesco: e.target.value })}>
                            <option value="HIJO">Hijo/a</option>
                            <option value="CONYUGE">Cónyuge</option>
                            <option value="CONVIVIENTE">Conviviente</option>
                            <option value="PADRE">Padre/Madre</option>
                          </select>
                        </div>
                        <div className="form-group">
                          <label>Documento</label>
                          <input value={familiarForm.numeroDi}
                            onChange={e => setFamiliarForm({ ...familiarForm, numeroDi: e.target.value })} />
                        </div>
                        <div className="form-group">
                          <label>Fecha de nacimiento *</label>
                          <input type="date" value={familiarForm.fechaNacimiento}
                            onChange={e => setFamiliarForm({ ...familiarForm, fechaNacimiento: e.target.value })}
                            required />
                        </div>
                        <div className="beneficios-checks">
                          <label><input type="checkbox" checked={familiarForm.esDerechohabiente}
                            onChange={e => setFamiliarForm({ ...familiarForm, esDerechohabiente: e.target.checked })} /> Incluir en cobertura de salud</label>
                          <label><input type="checkbox" checked={familiarForm.elegibleAsignacionFamiliar}
                            onChange={e => setFamiliarForm({ ...familiarForm, elegibleAsignacionFamiliar: e.target.checked })} /> Considerar para asignación familiar</label>
                          <label><input type="checkbox" checked={familiarForm.validado}
                            onChange={e => setFamiliarForm({ ...familiarForm, validado: e.target.checked })} /> Documentación validada</label>
                          <p className="beneficios-checks-help">
                            La asignación familiar solo impacta nómina cuando el familiar elegible tiene documentación validada.
                          </p>
                        </div>
                      </div>
                      <div className="form-actions">
                        <button className="btn btn-success" type="submit" disabled={beneficiosSaving}>
                          {editFamiliarId ? "Actualizar familiar" : "Agregar familiar"}
                        </button>
                        {editFamiliarId && (
                          <button type="button" className="btn btn-secondary"
                            onClick={() => { setEditFamiliarId(null); setFamiliarForm(FAMILIAR_VACIO); }}>
                            Cancelar edición
                          </button>
                        )}
                      </div>
                    </form>

                    <div className="table-wrapper beneficios-table">
                      <table>
                        <thead>
                          <tr>
                            <th>Familiar</th>
                            <th>Parentesco</th>
                            <th>Documento</th>
                            <th>Nacimiento</th>
                            <th>Nómina</th>
                            <th>Acciones</th>
                          </tr>
                        </thead>
                        <tbody>
                          {familiares.length === 0 ? (
                            <tr><td colSpan={6}><div className="empty-state"><p>No hay familiares registrados.</p></div></td></tr>
                          ) : familiares.map(f => (
                            <tr key={f.id}>
                              <td>{f.nombres} {f.apellidos}</td>
                              <td>{f.parentesco}</td>
                              <td>{f.numeroDi || "—"}</td>
                              <td>{f.fechaNacimiento || "—"}</td>
                              <td>
                                <span className={`badge ${f.elegibleAsignacionFamiliar && f.validado ? "badge-success" : "badge-gray"}`}>
                                  {f.elegibleAsignacionFamiliar && f.validado ? "Aplica" : "No aplica"}
                                </span>
                              </td>
                              <td>
                                <div style={{ display: "flex", gap: 8, justifyContent: "center" }}>
                                  <button className="btn btn-secondary btn-sm" onClick={() => editarFamiliar(f)}>Editar</button>
                                  <button className="btn btn-danger btn-sm" onClick={() => quitarFamiliar(f.id)}>Retirar</button>
                                </div>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                </>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalBeneficios({ open: false, empleado: null })}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: HISTORIAL DEL EMPLEADO ══ */}
      {modalHistorial.open && (
        <div className="modal-overlay">
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div>
                <h3>🕐 Historial — {modalHistorial.empleado?.nombres} {modalHistorial.empleado?.apellidos}</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  DNI: {modalHistorial.empleado?.numeroDi} · ID #{modalHistorial.empleado?.idEmpleado}
                </p>
              </div>
              <button className="modal-close" onClick={() => setModalHistorial({ open: false, empleado: null })}>×</button>
            </div>
            <div className="modal-body">
              {historialLoad ? (
                <Spinner />
              ) : historialData.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-state-icon">📋</div>
                  <p>No hay registros de historial para este empleado.</p>
                </div>
              ) : (
                <div className="table-wrapper" style={{ maxHeight: 420, overflowY: "auto" }}>
                  <table>
                    <thead>
                      <tr>
                        <th>Fecha</th>
                        <th>Acción</th>
                        <th>Valor Anterior</th>
                        <th>Valor Nuevo</th>
                      </tr>
                    </thead>
                    <tbody>
                      {historialData.map((h, i) => (
                        <tr key={h.idAuditoria ?? i}>
                          <td style={{ whiteSpace: "nowrap", fontSize: 12, color: "var(--text-3)" }}>
                            {h.fechaDeCambio
                              ? new Date(h.fechaDeCambio).toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" })
                              : "—"}
                          </td>
                          <td>
                            <span className={`badge ${
                              h.accion?.includes("CREAR") ? "badge-success" :
                              h.accion?.includes("DESACTIVAR") ? "badge-danger" :
                              h.accion?.includes("SANCION") ? "badge-warning" :
                              "badge-gray"
                            }`} style={{ fontSize: 11 }}>
                              {h.accion ?? "—"}
                            </span>
                          </td>
                          <td style={{ fontSize: 12, color: "var(--text-2)", maxWidth: 180 }}>
                            <ValorHistorial valor={h.valorAnterior} muted />
                          </td>
                          <td style={{ fontSize: 12, color: "var(--text-1)", maxWidth: 300 }}>
                            <ValorHistorial valor={h.valorNuevo} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
            <div className="modal-footer">
              <span style={{ fontSize: 12, color: "var(--text-3)" }}>
                {historialData.length > 0 && `${historialData.length} registro(s)`}
              </span>
              <button className="btn btn-secondary" onClick={() => setModalHistorial({ open: false, empleado: null })}>Cerrar</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
