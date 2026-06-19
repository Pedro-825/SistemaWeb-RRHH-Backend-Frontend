import { useState, useEffect, useRef } from "react";
import * as XLSX from "xlsx";
import Sidebar from "../components/Sidebar";
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
  cambioSalarial,
  getDepartamentos,
  getHistorialEmpleado,
  getHorarios,
  asignarHorario,
  getAsignacionesEmpleado,
  getAsignacionesActivas,
} from "../services/api";

const FORM_VACIO = {
  nombres: "", apellidos: "", docIdentidad: "DNI", numeroDi: "",
  fechaNac: "", sexo: "", estadoCivil: "", direccion: "",
  correo: "", telefono: "", cargo: "", tipoContrato: "",
  fechaInicio: "", fechaFin: "", sueldo: "", idDpto: "",
};

const PAGE_SIZES = [10, 25, 50];


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
  const [ascensoForm, setAscensoForm] = useState({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
  const [cambioForm,  setCambioForm]  = useState({ idEmpleado: "", nuevoSueldo: "" });

  // Modals
  const [modalNuevo,      setModalNuevo]      = useState(false);
  const [modalEditar,     setModalEditar]     = useState(null);
  const [modalDesactivar, setModalDesactivar] = useState({ open: false, id: null, motivo: "" });
  const [modalReactivar,  setModalReactivar]  = useState({ open: false, id: null });
  const [modalSancion,    setModalSancion]    = useState({ open: false, id: null });
  const [modalAscenso,    setModalAscenso]    = useState({ open: false, id: null });
  const [modalCambio,     setModalCambio]     = useState({ open: false, id: null });
  const [modalSueldo,     setModalSueldo]     = useState(false);
  const [sueldoModo,      setSueldoModo]      = useState("editar"); // "nuevo" | "editar"
  const [showFechas,      setShowFechas]      = useState(false);
  const [sueldoForm,      setSueldoForm]      = useState({
    sueldoBase: "", bonifAsistencia: 0, bonifFamiliar: 0, bonifExtraordinaria: 0,
    sistemaPensionario: "AFP", descuentoTardanza: 0, otrosDescuentos: 0,
    horasSemanales: 48, horasExtra: 0,
  });

  // Dropdown per row
  const [openDrop, setOpenDrop] = useState(null);
  const dropRef = useRef(null);

  // Historial modal
  const [modalHistorial, setModalHistorial] = useState({ open: false, empleado: null });
  const [historialData,  setHistorialData]  = useState([]);
  const [historialLoad,  setHistorialLoad]  = useState(false);

  // Horario
  const [horarios,      setHorarios]      = useState([]);
  const [turnosMap,     setTurnosMap]     = useState({});  // { idEmpleado: {nombreTurno, horaEntrada, horaSalida} }
  const [modalHorario,  setModalHorario]  = useState({ open: false, empleado: null, asignaciones: [], loading: false });
  const HORARIO_VACIO = { idHorario: "", fechaDesde: "", fechaHasta: "", esTemporal: false };
  const [horarioForm,   setHorarioForm]   = useState(HORARIO_VACIO);

  useEffect(() => {
    getDepartamentos().then(setDepartamentos).catch(() => {});
    getHorarios().then(setHorarios).catch(() => {});
    getAsignacionesActivas().then(lista => {
      const map = {};
      (lista || []).forEach(a => { map[a.idEmpleado] = a; });
      setTurnosMap(map);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    const handler = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) setOpenDrop(null);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  // Efecto unificado: re-fetcha automáticamente al cambiar cualquier filtro, página o pageSize.
  // El campo de nombre usa debounce de 400ms para no disparar en cada tecla.
  useEffect(() => {
    let cancelled = false;

    const doFetch = async () => {
      setLoading(true);
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
        }
      } catch {
        if (!cancelled) setError("Error al cargar empleados");
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
      if (!data?.success) {
        setMensajeOk(false);
        setMensaje(data?.message || "No se pudo registrar el empleado.");
        return;
      }
      setMensajeOk(true);
      setMensaje("Empleado registrado correctamente");
      setForm(FORM_VACIO);
      setModalNuevo(false);
      cargarEmpleados();
    } catch {
      setMensajeOk(false);
      setMensaje("Error al registrar empleado. Verifique los datos.");
    }
  };

  const handleGuardarEdicion = async (e) => {
    e.preventDefault();
    if (!modalEditar) return;
    try {
      await actualizarEmpleado(modalEditar.idEmpleado, {
        cargo: modalEditar.cargo,
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
    } catch {
      setMensaje("Error al actualizar el empleado.");
    }
  };

  const confirmarDesactivar = async () => {
    if (!modalDesactivar.motivo.trim()) return;
    try {
      await desactivarEmpleado(modalDesactivar.id, modalDesactivar.motivo);
      setMensaje("Empleado desactivado correctamente");
      setModalDesactivar({ open: false, id: null, motivo: "" });
      cargarEmpleados();
    } catch {
      setMensaje("Error al desactivar el empleado");
      setModalDesactivar({ open: false, id: null, motivo: "" });
    }
  };

  const confirmarReactivar = async () => {
    try {
      await reactivarEmpleado(modalReactivar.id, "Reactivación manual");
      setMensaje("Empleado reactivado correctamente");
      setModalReactivar({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al reactivar el empleado");
      setModalReactivar({ open: false, id: null });
    }
  };

  const handleSancion = async (e) => {
    e.preventDefault();
    try {
      await registrarSancion(sancionForm.idEmpleado, {
        tipoSancion: sancionForm.tipoSancion,
        diasSuspension: parseInt(sancionForm.diasSuspension),
        justificacion: sancionForm.justificacion,
      });
      setMensaje("Sanción registrada correctamente");
      setSancionForm({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
      setModalSancion({ open: false, id: null });
    } catch {
      setMensaje("Error al registrar la sanción");
    }
  };

  const handleAscenso = async (e) => {
    e.preventDefault();
    try {
      await registrarAscenso(ascensoForm.idEmpleado, { nuevoCargo: ascensoForm.nuevoCargo });
      setMensaje("Ascenso registrado correctamente");
      setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
      setModalAscenso({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar el ascenso");
    }
  };

  const handleCambioSalarial = async (e) => {
    e.preventDefault();
    try {
      await cambioSalarial(cambioForm.idEmpleado, { nuevoSueldo: parseFloat(cambioForm.nuevoSueldo) });
      setMensaje("Cambio salarial registrado correctamente");
      setCambioForm({ idEmpleado: "", nuevoSueldo: "" });
      setModalCambio({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar el cambio salarial");
    }
  };

  const openDropMenu = (id) => { setOpenDrop(openDrop === id ? null : id); };

  const abrirModalHorario = async (emp) => {
    setOpenDrop(null);
    setHorarioForm(HORARIO_VACIO);
    setModalHorario({ open: true, empleado: emp, asignaciones: [], loading: true });
    try {
      const data = await getAsignacionesEmpleado(emp.idEmpleado);
      setModalHorario(m => ({ ...m, asignaciones: data || [], loading: false }));
    } catch {
      setModalHorario(m => ({ ...m, loading: false }));
    }
  };

  const handleAsignarHorario = async (e) => {
    e.preventDefault();
    try {
      await asignarHorario({
        idEmpleado: modalHorario.empleado.idEmpleado,
        idHorario:  Number(horarioForm.idHorario),
        fechaDesde: horarioForm.fechaDesde,
        fechaHasta: horarioForm.fechaHasta || null,
        esTemporal: horarioForm.esTemporal,
      });
      // Refrescar historial en el modal
      const data = await getAsignacionesEmpleado(modalHorario.empleado.idEmpleado);
      setModalHorario(m => ({ ...m, asignaciones: data || [] }));
      // Refrescar columna Turno en la tabla
      const activas = await getAsignacionesActivas();
      const map = {};
      (activas || []).forEach(a => { map[a.idEmpleado] = a; });
      setTurnosMap(map);
      setHorarioForm(HORARIO_VACIO);
      setMensaje("Horario asignado correctamente");
      setMensajeOk(true);
    } catch {
      setMensaje("Error al asignar horario");
      setMensajeOk(false);
    }
  };

  const abrirSueldoDetalle = (modo = "editar") => {
    const base = modo === "nuevo" ? (form.sueldo ?? "") : (modalEditar?.sueldo ?? "");
    setSueldoForm(f => ({ ...f, sueldoBase: base }));
    setSueldoModo(modo);
    setModalSueldo(true);
  };

  const aplicarCambioSueldo = () => {
    if (sueldoModo === "nuevo") {
      setForm(f => ({ ...f, sueldo: sueldoForm.sueldoBase }));
    } else {
      setModalEditar(prev => ({ ...prev, sueldo: sueldoForm.sueldoBase }));
    }
    setModalSueldo(false);
  };

  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));

  const formatFecha = (str) => {
    if (!str) return "—";
    const d = new Date(str);
    return isNaN(d) ? str : `${String(d.getDate()).padStart(2,"0")}/${String(d.getMonth()+1).padStart(2,"0")}/${d.getFullYear()}`;
  };

  const formatSueldo = (v) =>
    v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const exportarEmpleados = () => {
    if (!empleados.length) return;
    const data = empleados.map(e => ({
      "ID":             e.idEmpleado ?? "—",
      "Nombre Completo": `${e.nombres ?? ""} ${e.apellidos ?? ""}`.trim(),
      "DNI":            e.numeroDi ?? "—",
      "Cargo":          e.cargo ?? "—",
      "Departamento":           e.departamento ?? "—",
      "Sueldo Base":    e.sueldo != null ? Number(e.sueldo).toFixed(2) : "—",
      "Estado":         e.estado ?? "—",
      "Tipo Contrato":  e.tipoContrato ?? "—",
      "Fecha Ingreso":  formatFecha(e.fechaInicio),
      "Correo":         e.correo ?? "—",
      "Teléfono":       e.telefono ?? "—",
    }));
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Empleados");
    XLSX.writeFile(wb, `empleados_${new Date().toISOString().split("T")[0]}.xlsx`);
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
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
            <button className="btn btn-primary" onClick={exportarEmpleados}>⬇ Exportar</button>
          </div>
        </header>

        {mensaje && (
          <div className={`alert ${mensajeOk ? "alert-success" : "alert-warning"}`}>
            {mensajeOk ? "✅" : "⚠️"} {mensaje}
          </div>
        )}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ── FILTER ROW ── */}
        <div className="filter-row">
          <div className="filter-group">
            <label className="filter-label">Tipo Documento</label>
            <select className="filter-select" value={filterDocType} onChange={e => setFilterDocType(e.target.value)}>
              <option value="">Todos</option>
              <option value="DNI">DNI</option>
              <option value="CE">Carnet Ext.</option>
            </select>
          </div>
          <div className="filter-group">
            <label className="filter-label">Departamento</label>
            <select className="filter-select" value={filterArea} onChange={e => { setFilterArea(e.target.value); setFilterCargo(""); }}>
              <option value="">Todos</option>
              {departamentos.map(d => (
                <option key={d.idDpto} value={d.nombre}>{d.nombre}</option>
              ))}
            </select>
          </div>
          <div className="filter-group">
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
          <div className="filter-group">
            <label className="filter-label">Estado</label>
            <select className="filter-select" value={filterEstado} onChange={e => setFilterEstado(e.target.value)}>
              <option value="">Todos</option>
              <option value="ACTIVO">Activo</option>
              <option value="INACTIVO">Inactivo</option>
            </select>
          </div>
          <div className="filter-group filter-search">
            <label className="filter-label">Buscar por nombre</label>
            <div className="filter-search-wrap">
              <input className="filter-input" placeholder="Ingrese nombre" value={searchName}
                onChange={e => setSearchName(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleBuscar()} />
              <span className="filter-search-icon">🔍</span>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "flex-end", gap: "8px", position: "relative", top: "-5px" }}>
            <button className="btn btn-primary btn-sm" onClick={handleBuscar}>Buscar</button>
            <button className="btn btn-secondary btn-sm" onClick={handleLimpiar}>☰ Limpiar filtros</button>
          </div>
        </div>

        {/* ── TABLE ── */}
        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div className="table-wrapper" style={{ marginBottom: 0 }} ref={dropRef}>
            {loading ? (
              <div className="loading-text">Cargando empleados...</div>
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
                    <tr key={emp.idEmpleado}>
                      <td>{emp.nombres} {emp.apellidos}</td>
                      <td style={{ color: "var(--text-2)" }}>
                        {emp.docIdentidad ?? "DNI"} · {emp.numeroDi}
                      </td>
                      <td>{emp.departamento ?? "—"}</td>
                      <td>{emp.cargo ?? "—"}</td>
                      <td style={{ textAlign: "center" }}>
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
                      <td>
                        <span className={`emp-status-dot ${emp.estado === "ACTIVO" ? "emp-status-active" : "emp-status-inactive"}`}>
                          {emp.estado === "ACTIVO" ? "Activo" : "Inactivo"}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: "inline-flex", gap: "6px", alignItems: "center" }}>
                          <button className="btn btn-secondary btn-sm"
                            onClick={() => setModalEditar({ ...emp })}>
                            ✏️ Editar
                          </button>
                          <div className="action-drop-wrap">
                            <button className="action-drop-btn"
                              onClick={() => openDropMenu(emp.idEmpleado)}>
                              ⋯
                            </button>
                            {openDrop === emp.idEmpleado && (
                              <div className="action-drop-menu">
                                {emp.estado === "ACTIVO" ? (
                                  <div className="action-drop-item drop-red"
                                    onClick={() => { setModalDesactivar({ open: true, id: emp.idEmpleado, motivo: "" }); setOpenDrop(null); }}>
                                    🚫 Desactivar empleado
                                  </div>
                                ) : (
                                  <div className="action-drop-item drop-green"
                                    onClick={() => { setModalReactivar({ open: true, id: emp.idEmpleado }); setOpenDrop(null); }}>
                                    ✅ Reactivar empleado
                                  </div>
                                )}
                                <div className="action-drop-item drop-yellow"
                                  onClick={() => { setModalSancion({ open: true, id: emp.idEmpleado }); setSancionForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  ⚠️ Registrar sanción
                                </div>
                                <div className="action-drop-item drop-green"
                                  onClick={() => { setModalAscenso({ open: true, id: emp.idEmpleado }); setAscensoForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  ↑ Registrar ascenso
                                </div>
                                <div className="action-drop-item drop-blue"
                                  onClick={() => { setModalCambio({ open: true, id: emp.idEmpleado }); setCambioForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  💲 Cambio salarial
                                </div>
                                <div className="action-drop-item drop-blue"
                                  onClick={() => abrirModalHorario(emp)}>
                                  📅 Asignar horario
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
                                  🕐 Ver historial
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
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && (setModalNuevo(false), setForm(FORM_VACIO), setShowFechas(false))}>
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
                    <select value={form.cargo} onChange={e => setForm({ ...form, cargo: e.target.value })} required>
                      <option value="">Seleccione cargo</option>
                      {(form.idDpto
                        ? DEPT_CARGOS[departamentos.find(d => d.idDpto === parseInt(form.idDpto))?.nombre] ?? []
                        : Object.values(DEPT_CARGOS).flat().sort()
                      ).map(c => <option key={c} value={c}>{c}</option>)}
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
                    <button type="button" className="sueldo-detalle-btn" onClick={() => abrirSueldoDetalle("nuevo")}>
                      <span className="sueldo-detalle-valor">
                        {form.sueldo ? formatSueldo(form.sueldo) : "Sin definir"}
                      </span>
                      <span className="sueldo-detalle-hint">Ver estructura salarial →</span>
                    </button>
                    {/* campo oculto para validación required */}
                    <input type="hidden" value={form.sueldo} required />
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
              <button className="btn btn-secondary" onClick={() => { setModalNuevo(false); setForm(FORM_VACIO); setShowFechas(false); }}>Cancelar</button>
              <button className="btn btn-success" type="submit" form="form-nuevo">✅ Registrar Empleado</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: EDITAR ══ */}
      {modalEditar && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalEditar(null)}>
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
                    <label>Cargo</label>
                    <input value={modalEditar.cargo ?? ""} onChange={e => setModalEditar({ ...modalEditar, cargo: e.target.value })} />
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
                    <button type="button" className="sueldo-detalle-btn" onClick={abrirSueldoDetalle}>
                      <span className="sueldo-detalle-valor">{formatSueldo(modalEditar.sueldo)}</span>
                      <span className="sueldo-detalle-hint">Ver estructura salarial →</span>
                    </button>
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
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalSancion({ open: false, id: null })}>
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

      {/* ══ MODAL: ASCENSO ══ */}
      {modalAscenso.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalAscenso({ open: false, id: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>↑ Registrar Ascenso</h3><p>Empleado #{modalAscenso.id}</p></div>
              <button className="modal-close" onClick={() => setModalAscenso({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <form id="form-ascenso" onSubmit={handleAscenso}>
                <div className="form-group">
                  <label>Nuevo Cargo</label>
                  <input value={ascensoForm.nuevoCargo}
                    onChange={e => setAscensoForm({ ...ascensoForm, nuevoCargo: e.target.value })}
                    placeholder="Ej: Jefe de Área, Especialista..." />
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalAscenso({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-primary" type="submit" form="form-ascenso">↑ Registrar Ascenso</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: CAMBIO SALARIAL ══ */}
      {modalCambio.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalCambio({ open: false, id: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>💲 Cambio Salarial</h3><p>Empleado #{modalCambio.id}</p></div>
              <button className="modal-close" onClick={() => setModalCambio({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <form id="form-cambio" onSubmit={handleCambioSalarial}>
                <div className="form-group">
                  <label>Nuevo Sueldo (S/) *</label>
                  <input type="number" step="0.01" min="0" value={cambioForm.nuevoSueldo}
                    onChange={e => setCambioForm({ ...cambioForm, nuevoSueldo: e.target.value })}
                    placeholder="0.00" required />
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalCambio({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-primary" type="submit" form="form-cambio">💾 Guardar Cambio</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ SUB-MODAL: ESTRUCTURA SALARIAL ══ */}
      {modalSueldo && (sueldoModo === "nuevo" || modalEditar) && (() => {
        const empleadoNombre = sueldoModo === "nuevo"
          ? `${form.nombres || "—"} ${form.apellidos || ""}`.trim()
          : `${modalEditar?.nombres ?? ""} ${modalEditar?.apellidos ?? ""}`.trim();
        const empleadoCargo = sueldoModo === "nuevo" ? (form.cargo ?? "—") : (modalEditar?.cargo ?? "—");
        const base      = parseFloat(sueldoForm.sueldoBase       || 0);
        const bAsist    = parseFloat(sueldoForm.bonifAsistencia   || 0);
        const bFam      = parseFloat(sueldoForm.bonifFamiliar     || 0);
        const bExtra    = parseFloat(sueldoForm.bonifExtraordinaria || 0);
        const bruto     = base + bAsist + bFam + bExtra;
        const tasaPens  = sueldoForm.sistemaPensionario === "ONP" ? 0.13 : 0.1317;
        const descPens  = bruto * tasaPens;
        const descTard  = parseFloat(sueldoForm.descuentoTardanza || 0);
        const descOtros = parseFloat(sueldoForm.otrosDescuentos   || 0);
        const totalDesc = descPens + descTard + descOtros;
        const neto      = bruto - totalDesc;
        const fmt       = (n) => `S/ ${Number(n).toLocaleString("es-PE", { minimumFractionDigits: 2 })}`;
        return (
          <div className="modal-overlay" style={{ zIndex: 1100 }}
            onClick={e => e.target === e.currentTarget && setModalSueldo(false)}>
            <div className="modal-box modal-box-wide">
              <div className="modal-header">
                <div>
                  <h3>💰 Estructura Salarial</h3>
                  <p>{empleadoNombre || "Nuevo empleado"} — {empleadoCargo}</p>
                </div>
                <button className="modal-close" onClick={() => setModalSueldo(false)}>×</button>
              </div>
              <div className="modal-body">
                <div className="sueldo-detalle-grid">

                  {/* Columna izquierda: inputs */}
                  <div className="sueldo-col">
                    <p className="sueldo-section-title">Jornada</p>
                    <div className="form-grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
                      <div className="form-group">
                        <label>Horas semanales</label>
                        <input type="number" min="0" value={sueldoForm.horasSemanales}
                          onChange={e => setSueldoForm(f => ({ ...f, horasSemanales: e.target.value }))} />
                      </div>
                      <div className="form-group">
                        <label>Horas extra</label>
                        <input type="number" min="0" value={sueldoForm.horasExtra}
                          onChange={e => setSueldoForm(f => ({ ...f, horasExtra: e.target.value }))} />
                      </div>
                    </div>

                    <p className="sueldo-section-title" style={{ marginTop: "14px" }}>Remuneración</p>
                    <div className="form-grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
                      <div className="form-group">
                        <label>Sueldo Base (S/) *</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.sueldoBase}
                          onChange={e => setSueldoForm(f => ({ ...f, sueldoBase: e.target.value }))} autoFocus />
                      </div>
                      <div className="form-group">
                        <label>Bonif. asistencia (S/)</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.bonifAsistencia}
                          onChange={e => setSueldoForm(f => ({ ...f, bonifAsistencia: e.target.value }))} />
                      </div>
                      <div className="form-group">
                        <label>Bonif. familiar (S/)</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.bonifFamiliar}
                          onChange={e => setSueldoForm(f => ({ ...f, bonifFamiliar: e.target.value }))} />
                      </div>
                      <div className="form-group">
                        <label>Bonif. extraordinaria (S/)</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.bonifExtraordinaria}
                          onChange={e => setSueldoForm(f => ({ ...f, bonifExtraordinaria: e.target.value }))} />
                      </div>
                    </div>

                    <p className="sueldo-section-title" style={{ marginTop: "14px" }}>Descuentos</p>
                    <div className="form-grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
                      <div className="form-group">
                        <label>Sistema pensionario</label>
                        <select value={sueldoForm.sistemaPensionario}
                          onChange={e => setSueldoForm(f => ({ ...f, sistemaPensionario: e.target.value }))}>
                          <option value="AFP">AFP (~13.17%)</option>
                          <option value="ONP">ONP (13%)</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>Desc. por tardanza (S/)</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.descuentoTardanza}
                          onChange={e => setSueldoForm(f => ({ ...f, descuentoTardanza: e.target.value }))} />
                      </div>
                      <div className="form-group">
                        <label>Otros descuentos (S/)</label>
                        <input type="number" step="0.01" min="0" value={sueldoForm.otrosDescuentos}
                          onChange={e => setSueldoForm(f => ({ ...f, otrosDescuentos: e.target.value }))} />
                      </div>
                    </div>
                  </div>

                  {/* Columna derecha: resumen */}
                  <div className="sueldo-resumen">
                    <p className="sueldo-section-title">Resumen</p>
                    <div className="sueldo-resumen-row">
                      <span>Sueldo base</span><span>{fmt(base)}</span>
                    </div>
                    <div className="sueldo-resumen-row">
                      <span>Bonificaciones</span><span>+ {fmt(bAsist + bFam + bExtra)}</span>
                    </div>
                    <div className="sueldo-resumen-row sueldo-resumen-subtotal">
                      <span>Total bruto</span><span>{fmt(bruto)}</span>
                    </div>
                    <div className="sueldo-resumen-row sueldo-resumen-neg">
                      <span>{sueldoForm.sistemaPensionario} ({sueldoForm.sistemaPensionario === "ONP" ? "13%" : "13.17%"})</span>
                      <span>− {fmt(descPens)}</span>
                    </div>
                    <div className="sueldo-resumen-row sueldo-resumen-neg">
                      <span>Tardanza / otros</span><span>− {fmt(descTard + descOtros)}</span>
                    </div>
                    <div className="sueldo-resumen-row sueldo-resumen-neto">
                      <span>Neto a pagar</span><span>{fmt(neto)}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setModalSueldo(false)}>Cancelar</button>
                <button className="btn btn-success" onClick={aplicarCambioSueldo}>✅ Aplicar sueldo base</button>
              </div>
            </div>
          </div>
        );
      })()}

      {/* ══ MODAL: ASIGNAR HORARIO ══ */}
      {modalHorario.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalHorario(m => ({ ...m, open: false }))}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div>
                <h3>📅 Horario — {modalHorario.empleado?.nombres} {modalHorario.empleado?.apellidos}</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  DNI: {modalHorario.empleado?.numeroDi} · {modalHorario.empleado?.cargo ?? "—"}
                </p>
              </div>
              <button className="modal-close" onClick={() => setModalHorario(m => ({ ...m, open: false }))}>×</button>
            </div>

            <div className="modal-body">
              {/* ── Formulario de nueva asignación ── */}
              <p style={{ fontWeight: 600, fontSize: 13, margin: "0 0 10px", color: "var(--text-1)" }}>
                Nueva asignación
              </p>
              <form id="form-horario" onSubmit={handleAsignarHorario}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Turno *</label>
                    <select
                      value={horarioForm.idHorario}
                      onChange={e => setHorarioForm(f => ({ ...f, idHorario: e.target.value }))}
                      required
                    >
                      <option value="">Seleccione turno</option>
                      {horarios.map(h => (
                        <option key={h.idHorario} value={h.idHorario}>
                          {h.nombreTurno} ({h.horaEntrada?.slice(0,5)} – {h.horaSalida?.slice(0,5)})
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Desde *</label>
                    <input
                      type="date"
                      value={horarioForm.fechaDesde}
                      onChange={e => setHorarioForm(f => ({ ...f, fechaDesde: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Hasta <span style={{ color: "var(--text-3)", fontWeight: 400 }}>(opcional)</span></label>
                    <input
                      type="date"
                      value={horarioForm.fechaHasta}
                      onChange={e => setHorarioForm(f => ({ ...f, fechaHasta: e.target.value }))}
                      min={horarioForm.fechaDesde || undefined}
                    />
                  </div>
                  <div className="form-group" style={{ display: "flex", alignItems: "center", gap: 8, paddingTop: 24 }}>
                    <input
                      type="checkbox"
                      id="esTemporal"
                      checked={horarioForm.esTemporal}
                      onChange={e => setHorarioForm(f => ({ ...f, esTemporal: e.target.checked }))}
                      style={{ width: 16, height: 16, cursor: "pointer" }}
                    />
                    <label htmlFor="esTemporal" style={{ margin: 0, cursor: "pointer", fontWeight: 500 }}>
                      Asignación temporal
                    </label>
                  </div>
                </div>
              </form>

              {/* ── Historial de asignaciones ── */}
              <p style={{ fontWeight: 600, fontSize: 13, margin: "18px 0 10px", color: "var(--text-1)" }}>
                Historial de asignaciones
              </p>
              {modalHorario.loading ? (
                <div className="loading-text">Cargando...</div>
              ) : modalHorario.asignaciones.length === 0 ? (
                <div className="empty-state" style={{ padding: "16px 0" }}>
                  <div className="empty-state-icon">📅</div>
                  <p>Sin asignaciones de horario registradas.</p>
                </div>
              ) : (
                <div className="table-wrapper" style={{ maxHeight: 260, overflowY: "auto" }}>
                  <table>
                    <thead>
                      <tr>
                        <th>Turno</th>
                        <th>Horario</th>
                        <th>Desde</th>
                        <th>Hasta</th>
                        <th style={{ textAlign: "center" }}>Temporal</th>
                        <th style={{ textAlign: "center" }}>Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {modalHorario.asignaciones.map(a => (
                        <tr key={a.idAsignacion}>
                          <td style={{ fontWeight: 600 }}>{a.nombreTurno}</td>
                          <td style={{ fontSize: 12, color: "var(--text-2)" }}>
                            {a.horaEntrada?.slice(0,5)} – {a.horaSalida?.slice(0,5)}
                          </td>
                          <td style={{ fontSize: 12 }}>{a.fechaDesde ?? "—"}</td>
                          <td style={{ fontSize: 12 }}>{a.fechaHasta ?? "Indefinido"}</td>
                          <td style={{ textAlign: "center", fontSize: 12 }}>{a.esTemporal ? "Sí" : "No"}</td>
                          <td style={{ textAlign: "center" }}>
                            <span className={`emp-status-dot ${a.activo ? "emp-status-active" : "emp-status-inactive"}`}>
                              {a.activo ? "Activo" : "Inactivo"}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalHorario(m => ({ ...m, open: false }))}>
                Cerrar
              </button>
              <button className="btn btn-success" type="submit" form="form-horario">
                📅 Asignar turno
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: HISTORIAL DEL EMPLEADO ══ */}
      {modalHistorial.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalHistorial({ open: false, empleado: null })}>
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
                <div className="loading-text">Cargando historial...</div>
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
                          <td style={{ fontSize: 12, color: "var(--text-2)", maxWidth: 220 }}>
                            <span title={h.valorAnterior ?? ""} style={{ display: "block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                              {h.valorAnterior ?? "—"}
                            </span>
                          </td>
                          <td style={{ fontSize: 12, color: "var(--text-1)", maxWidth: 280 }}>
                            <span title={h.valorNuevo ?? ""} style={{ display: "block", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                              {h.valorNuevo ?? "—"}
                            </span>
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
