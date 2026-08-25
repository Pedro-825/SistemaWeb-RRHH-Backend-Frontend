-- =========================
-- ROLES BASE
-- =========================

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('RRHH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('GERENCIA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('EMPLEADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;


-- =========================
-- DEPARTAMENTOS BASE
-- =========================

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Recursos Humanos', 'RRHH01', 'Piso 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Dirección General', 'DIR01', 'Piso 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Emergencia', 'EMG01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;


-- =========================
-- EMPLEADO ADMIN RRHH
-- =========================

INSERT INTO empleado(
    nombres,
    apellidos,
    doc_identidad,
    numero_di,
    fecha_nac,
    sexo,
    estado_civil,
    direccion,
    correo,
    telefono,
    estado,
    creado_el,
    actualizado_el
)
VALUES (
    'Admin',
    'RRHH',
    'DNI',
    '00000001',
    '1995-01-01',
    'M',
    'SOLTERO',
    'Lima',
    'admin.rrhh.personal@gmail.com',
    '999999999',
    'ACTIVO',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (numero_di) DO NOTHING;


-- =========================
-- CONTRATO ADMIN RRHH
-- =========================

INSERT INTO contrato(
    cargo,
    tipo_contrato,
    fecha_inicio,
    fecha_fin,
    sueldo,
    estado,
    id_empleado,
    id_dpto,
    creado_el,
    actualizado_el
)
SELECT
    'Administrador RRHH',
    'INDEFINIDO',
    CURRENT_DATE,
    NULL,
    5000.00,
    'ACTIVO',
    e.id_empleado,
    d.id_dpto,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM empleado e
INNER JOIN departamento d ON d.cod_dpto = 'RRHH01'
WHERE e.numero_di = '00000001'
  AND NOT EXISTS (
      SELECT 1
      FROM contrato c
      WHERE c.id_empleado = e.id_empleado
        AND c.estado = 'ACTIVO'
  );


-- =========================
-- USUARIO ADMIN RRHH
-- =========================

INSERT INTO usuario(
    nombre_usuario,
    correo_inst,
    contrasenia,
    intentos_fallidos,
    cuenta_bloqueada,
    fecha_bloqueo,
    dosfa_activo,
    dosfa_secret,
    activo,
    id_empleado,
    id_rol,
    creado_el,
    actualizado_el
)
SELECT
    'admin.rrhh',
    'admin.rrhh@hospitalsangabriel.com',
    '$2b$10$AqTU2mor2WTsIJYjwkWv1uRZasDdNuDISpfvZWGB.kaRJHW/vl3L.',
    0,
    false,
    NULL,
    false,
    NULL,
    true,
    e.id_empleado,
    r.id_rol,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM empleado e
INNER JOIN rol r ON r.nombre_rol = 'RRHH'
WHERE e.numero_di = '00000001'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario u
      WHERE u.nombre_usuario = 'admin.rrhh'
         OR u.correo_inst = 'admin.rrhh@hospitalsangabriel.com'
  );


INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, minutos_tardanza, minutos_extra, id_empleado)
SELECT 
    v.fecha, v.hora_entrada, v.hora_salida, v.minutos_tardanza, v.minutos_extra, e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-01-02'::date, '08:00:00'::time, '17:00:00'::time, 0, 30),
    ('2026-01-03', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-05', '08:05:00', '17:00:00', 5, 0),
    ('2026-01-06', '08:00:00', '18:30:00', 0, 90),
    ('2026-01-07', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-08', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-09', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-12', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-13', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-14', '08:00:00', '17:00:00', 0, 0)
) AS v(fecha, hora_entrada, hora_salida, minutos_tardanza, minutos_extra)
WHERE e.numero_di = '00000001';


-- =========================
-- FAMILIA ADMIN RRHH (para bonificación familiar en nómina)
-- =========================

INSERT INTO familia_info(nombres, parentesco, numero_di, fecha_nacimiento, id_empleado, activo)
SELECT 'Maria RRHH', 'HIJO', '11111111', '2018-03-15', e.id_empleado, true
FROM empleado e WHERE e.numero_di = '00000001'
ON CONFLICT (numero_di) DO NOTHING;

INSERT INTO familia_info(nombres, parentesco, numero_di, fecha_nacimiento, id_empleado, activo)
SELECT 'Carlos RRHH', 'HIJO', '22222222', '2020-07-22', e.id_empleado, true
FROM empleado e WHERE e.numero_di = '00000001'
ON CONFLICT (numero_di) DO NOTHING;

-- Hijos de prueba: candidatos a asignación familiar y derechohabientes.
-- RRHH deberá validarlos mediante la gestión de beneficios antes del cálculo.
UPDATE familia_info
SET elegible_asignacion_familiar = true,
    es_derechohabiente = true
WHERE parentesco = 'HIJO' AND activo = true;

-- =========================
-- FERIADOS
-- =========================

INSERT INTO feriado (fecha, descripcion) VALUES
    ('2026-01-01', 'Año Nuevo'),
    ('2026-05-01', 'Día del Trabajo'),
    ('2026-07-28', 'Fiestas Patrias'),
    ('2026-07-29', 'Fiestas Patrias'),
    ('2026-12-25', 'Navidad')
ON CONFLICT (fecha) DO NOTHING;


-- =========================
-- DEPARTAMENTOS ADICIONALES
-- =========================

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo) VALUES
    ('Enfermería',     'ENF01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Medicina',       'MED01', 'Piso 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Administración', 'ADM01', 'Piso 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Laboratorio',    'LAB01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Finanzas',       'FIN01', 'Piso 4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Radiología',     'RAD01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;


-- =========================
-- 10 EMPLEADOS (PERSONAL HOSPITALARIO)
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Ana',    'García López',    'DNI', '12345678', '1990-05-15', 'F', 'CASADO',    'Av. Lima 123, Miraflores',        'ana.garcia@hospital.pe',    '987654321', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carlos', 'Méndez Torres',   'DNI', '23456789', '1985-08-22', 'M', 'CASADO',    'Jr. Arequipa 456, San Isidro',   'c.mendez@hospital.pe',      '976543210', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('María',  'Quispe Riva',     'DNI', '34567890', '1992-11-30', 'F', 'SOLTERO',   'Av. Tacna 789, Lima',            'm.quispe@hospital.pe',      '965432109', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Luis',   'Fernández Paz',   'DNI', '45678901', '1988-03-10', 'M', 'SOLTERO',   'Calle Miraflores 101, Lince',    'l.fernandez@hospital.pe',   '954321098', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Rosa',   'Vargas Chávez',   'DNI', '56789012', '1995-07-18', 'F', 'SOLTERO',   'Av. Brasil 202, Breña',          'r.vargas@hospital.pe',      '943210987', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pedro',  'Ramos Soto',      'DNI', '67890123', '1982-12-05', 'M', 'CASADO',    'Jr. Cusco 303, La Victoria',     'p.ramos@hospital.pe',       '932109876', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Elena',  'Castro Neira',    'DNI', '78901234', '1987-04-25', 'F', 'CASADO',    'Av. Salaverry 404, Jesús María', 'e.castro@hospital.pe',      '921098765', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jorge',  'Huamán Pinto',    'DNI', '89012345', '1990-09-12', 'M', 'SOLTERO',   'Jr. Puno 505, Lima',             'j.huaman@hospital.pe',      '910987654', 'DESVINCULADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sofía',  'Delgado Ríos',    'DNI', '90123456', '1998-02-28', 'F', 'SOLTERO',   'Av. Colonial 606, Pueblo Libre', 's.delgado@hospital.pe',     '909876543', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Miguel', 'Paredes Luna',    'DNI', '01234567', '1983-06-14', 'M', 'CASADO',    'Calle Piura 707, Magdalena',     'm.paredes@hospital.pe',     '998765432', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- =========================
-- CONTRATOS (10 EMPLEADOS)
-- =========================

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermera Jefe', 'INDEFINIDO', '2020-03-15', NULL, 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '12345678' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Médico General', 'INDEFINIDO', '2018-06-01', NULL, 5500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '23456789' AND d.cod_dpto = 'MED01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Administradora', 'PLAZO FIJO', '2021-01-10', '2027-01-10', 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '34567890' AND d.cod_dpto = 'ADM01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Técnico de Laboratorio', 'INDEFINIDO', '2019-09-20', NULL, 2400.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '45678901' AND d.cod_dpto = 'LAB01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermera', 'INDEFINIDO', '2022-02-14', NULL, 2800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '56789012' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Médico Especialista', 'INDEFINIDO', '2017-11-05', NULL, 7200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '67890123' AND d.cod_dpto = 'MED01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Contadora', 'INDEFINIDO', '2020-07-22', NULL, 3800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '78901234' AND d.cod_dpto = 'FIN01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermero', 'PLAZO FIJO', '2019-04-08', '2023-04-08', 2600.00, 'RESUELTO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '89012345' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado);

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Recepcionista', 'INDEFINIDO', '2023-01-03', NULL, 2200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '90123456' AND d.cod_dpto = 'ADM01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Radiólogo', 'INDEFINIDO', '2016-08-15', NULL, 6500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '01234567' AND d.cod_dpto = 'RAD01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- =========================
-- USUARIOS (10 EMPLEADOS — rol EMPLEADO)
-- =========================

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'a.garcia', 'a.garcia@hospitalsangabriel.com', '$2b$10$w/uWN.m..o/zmaKknuDvCe7RHb4hSXd63Pwz7SvLseanwU6UY8Wwq',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '12345678' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'a.garcia');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'c.mendez', 'c.mendez@hospitalsangabriel.com', '$2b$10$XFy37U71XdxTRwKxuzyiu.eH9Deve05T/xou/Rc1VhSuD9foKlMR6',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '23456789' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'c.mendez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.quispe', 'm.quispe@hospitalsangabriel.com', '$2b$10$OuikuPA2VBb0PPCbQVSE4OqFMTnhej7bOK1SFucfkxhJImPbXGQ2i',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '34567890' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.quispe');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'l.fernandez', 'l.fernandez@hospitalsangabriel.com', '$2b$10$1u5k/3C/LW7WTqN6tQ4fyuYUS4I2ffk4KGNpFfMtkMJiyHYTjPYcO',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '45678901' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'l.fernandez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'r.vargas', 'r.vargas@hospitalsangabriel.com', '$2b$10$75Gy2b2GS0J.DJI.XjTxe.Gg5xwnhWe/8aU9TBqk6gvBNZtyW85eG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '56789012' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'r.vargas');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'p.ramos', 'p.ramos@hospitalsangabriel.com', '$2b$10$pCphzZWKPbMdem6JtMfX7OhmSX11khgKhQKH/HqWiqFp2xrasmema',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '67890123' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'p.ramos');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'e.castro', 'e.castro@hospitalsangabriel.com', '$2b$10$H5B7AagrBptWHe8p9ytmtuIbwwoSsO6FxMJTw0hF5ZxTgcQop7F/S',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '78901234' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'e.castro');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'j.huaman', 'j.huaman@hospitalsangabriel.com', '$2b$10$M5Y7eof9G0KgKmOsPR1rcuoD5W6utUnoUXJpOG3Iy2GUztYCf1VaG',
       0, false, NULL, false, NULL, false, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '89012345' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'j.huaman');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 's.delgado', 's.delgado@hospitalsangabriel.com', '$2b$10$rXEKDje.fxL6uzdcsuNVDOmq0x6F/fUc1e3113MDuDGy5q1cmikUG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '90123456' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 's.delgado');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.paredes', 'm.paredes@hospitalsangabriel.com', '$2b$10$wYFL5rtKu8XPQ9cZGreYneksRBzVdIPIbZ.nGK2RVOAqkPK1NbJMS',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '01234567' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.paredes');


-- =========================
-- 5 EMPLEADOS RRHH
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Laura',   'Huanca Quispe',    'DNI', '11223300', '1989-03-22', 'F', 'CASADO',  'Av. Universitaria 150, Los Olivos',  'l.huanca@hospital.pe',   '981100001', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Marco',   'Díaz Villanueva',  'DNI', '22334400', '1991-07-14', 'M', 'SOLTERO', 'Jr. Huancavelica 280, Cercado',      'm.diaz@hospital.pe',      '981100002', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Isabel',  'Torres Mamani',    'DNI', '33445500', '1994-11-08', 'F', 'SOLTERO', 'Av. Próceres 440, San Juan de Lurigancho', 'i.torres@hospital.pe', '981100003', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Rodrigo', 'Benítez Lima',     'DNI', '44556600', '1986-05-30', 'M', 'CASADO',  'Calle Las Flores 62, Surco',         'r.benitez@hospital.pe',   '981100004', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Claudia', 'Morales Vásquez',  'DNI', '55667700', '1993-09-17', 'F', 'SOLTERO', 'Av. Angamos 311, Surquillo',         'c.morales@hospital.pe',   '981100005', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- Contratos RRHH → departamento Recursos Humanos

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Especialista RRHH', 'INDEFINIDO', '2021-03-01', NULL, 3500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '11223300' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Analista RRHH', 'INDEFINIDO', '2022-06-15', NULL, 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '22334400' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Asistente RRHH', 'PLAZO FIJO', '2024-01-10', '2026-01-10', 2800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '33445500' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Coordinador RRHH', 'INDEFINIDO', '2019-08-20', NULL, 4200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '44556600' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Auxiliar RRHH', 'PLAZO FIJO', '2023-09-01', '2025-09-01', 2400.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '55667700' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- Usuarios RRHH (rol RRHH)

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'l.huanca', 'l.huanca@hospitalsangabriel.com', '$2b$10$LjhcIkAFh.HV6uBFfWUyIO6iIp5g6vAutYzWsNzAaGuyCtTKI1e1C',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '11223300' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'l.huanca');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.diaz', 'm.diaz@hospitalsangabriel.com', '$2b$10$S5ssXTw0Ta0urpx05Ye/QO13mcrcCJI1niFWEKlwOTq2ybyBp36sm',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '22334400' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.diaz');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'i.torres', 'i.torres@hospitalsangabriel.com', '$2b$10$BWqsqcFtcvrBXr8zI6wsL.PbPTJXR/azq84dwI96feJyLgmB549H6',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '33445500' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'i.torres');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'r.benitez', 'r.benitez@hospitalsangabriel.com', '$2b$10$vq0pu5UX5kwUymfCcnIRmOJT9VB8FptccpPFUliLtv/vhA49z7EyS',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '44556600' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'r.benitez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'c.morales', 'c.morales@hospitalsangabriel.com', '$2b$10$ozVAQTjeu6NKYJrZwtgZNe79s212DeQ8ose7J6PKLkbRUrCUcbutG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '55667700' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'c.morales');


-- =========================
-- 3 EMPLEADOS GERENCIA
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Fernando',   'Castillo Ruiz',   'DNI', '66778800', '1975-04-10', 'M', 'CASADO',  'Av. La Molina 520, La Molina',   'f.castillo@hospital.pe',  '991200001', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Patricia',   'Medina Solís',    'DNI', '77889900', '1980-08-25', 'F', 'CASADO',  'Jr. Monte Bello 89, Surco',      'p.medina@hospital.pe',    '991200002', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Alejandro',  'Vargas Flores',   'DNI', '88990011', '1978-12-03', 'M', 'CASADO',  'Av. El Polo 740, Surco',         'a.vargas@hospital.pe',    '991200003', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- Contratos GERENCIA → departamento Dirección General

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Director General', 'INDEFINIDO', '2015-01-05', NULL, 12000.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '66778800' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Subgerente', 'INDEFINIDO', '2018-03-12', NULL, 9500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '77889900' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Gerente Administrativo', 'INDEFINIDO', '2017-06-20', NULL, 8800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '88990011' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- Usuarios GERENCIA (rol GERENCIA)

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'f.castillo', 'f.castillo@hospitalsangabriel.com', '$2b$10$IzZT1F77HDUn5nYszFXeRO7Sav4RFPcDCnGUSQcpwD4c/SQyJNoqK',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '66778800' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'f.castillo');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'p.medina', 'p.medina@hospitalsangabriel.com', '$2b$10$CAOJiULFFxbiq9bVOGezFOfnmvpl/SaLN2bWWNuX9VuYkS6MG8s/a',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '77889900' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'p.medina');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'a.vargas', 'a.vargas@hospitalsangabriel.com', '$2b$10$XaS9WUSx58M4kYRQnKFrmuLl8BDQMiKmq48jGbT1RIOEPdkgvgLIC',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '88990011' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'a.vargas');


-- =========================
-- USUARIOS DE PRUEBA (SIN 2FA)
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el)
VALUES
    ('Empleado', 'Prueba', 'DNI', '99990001', '1995-01-01', 'M', 'SOLTERO', 'Lima', 'empleado.test@hospitalsangabriel.com', '999900001', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Gerencia', 'Prueba', 'DNI', '99990002', '1980-01-01', 'F', 'SOLTERO', 'Lima', 'gerencia.test@hospitalsangabriel.com', '999900002', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Empleado de Prueba', 'INDEFINIDO', CURRENT_DATE, NULL, 2500.00, 'ACTIVO', e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '99990001' AND d.cod_dpto = 'ADM01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Gerente de Prueba', 'INDEFINIDO', CURRENT_DATE, NULL, 9000.00, 'ACTIVO', e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '99990002' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'empleado_test', 'empleado.test@hospitalsangabriel.com', '$2b$10$8BV18Zhll7.y2H.Vk5FFOuupmmOhpLD9I9iFRpLLoEItIxXfnysIW',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r
WHERE e.numero_di = '99990001' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'empleado_test');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'gerencia_test', 'gerencia.test@hospitalsangabriel.com', '$2b$10$0Zscal2aE32vQcsih1qSiuAJgOIPJPUh3eMBnU6pQD4CuIyqKACyG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r
WHERE e.numero_di = '99990002' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'gerencia_test');


-- ============================================================
-- REGISTROS DE ASISTENCIA — Mayo 2026
-- Para: Ana García, Carlos Méndez, Rosa Vargas, Elena Castro, Sofía Delgado, Miguel Paredes
-- ============================================================

-- Ana García (12345678) — Enfermera Jefe
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:45:00',       '17:45:00',       9.00,          45, 'TARDANZA'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '09:10:00',       '17:00:00',       7.83,          70, 'TARDANZA'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:30:00',       '17:30:00',       9.00,          30, 'TARDANZA'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Carlos Méndez (23456789) — Médico General
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:50:00',       '17:50:00',       9.00,          50, 'TARDANZA'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '09:05:00',       '17:00:00',       7.92,          65, 'TARDANZA'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Rosa Vargas (56789012) — Enfermera
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '09:30:00',       '17:00:00',       7.50,          90, 'TARDANZA'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:15:00',       '17:15:00',       9.00,          15, 'TARDANZA'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '56789012'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Elena Castro (78901234) — Contadora
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:40:00',       '17:40:00',       9.00,          40, 'TARDANZA'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '09:00:00',       '17:00:00',       8.00,          60, 'TARDANZA'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '78901234'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Sofía Delgado (90123456) — Recepcionista
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:20:00',       '17:20:00',       9.00,          20, 'TARDANZA'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '09:45:00',       '17:00:00',       7.25,          105,'TARDANZA'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '90123456'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Miguel Paredes (01234567) — Radiólogo
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:35:00',       '17:35:00',       9.00,          35, 'TARDANZA'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '09:20:00',       '17:00:00',       7.67,          80, 'TARDANZA'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);


-- ============================================================
-- ASISTENCIA — JUNIO 2026 (días laborables 1-12)
-- ============================================================

-- Ana García (12345678)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-03',        '08:25:00',        '17:25:00',        9.00,          25, 'TARDANZA'),
    ('2026-06-04',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-05',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-08',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-09',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '09:05:00',        '17:00:00',        7.92,          65, 'TARDANZA'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Carlos Méndez (23456789)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:40:00',        '17:40:00',        9.00,          40, 'TARDANZA'),
    ('2026-06-03',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-04',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-05',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-08',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-09',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Rosa Vargas (56789012)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-03',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-04',        '08:50:00',        '17:50:00',        9.00,          50, 'TARDANZA'),
    ('2026-06-05',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-08',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-09',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '56789012'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Elena Castro (78901234)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-03',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-04',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-05',        '08:30:00',        '17:30:00',        9.00,          30, 'TARDANZA'),
    ('2026-06-08',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-09',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '78901234'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Sofía Delgado (90123456)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-03',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-04',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-05',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-08',        '09:20:00',        '17:00:00',        7.67,          80, 'TARDANZA'),
    ('2026-06-09',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '90123456'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Miguel Paredes (01234567)
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas, v.tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-06-01'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-06-02',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-03',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-04',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-05',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-08',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-09',        '08:45:00',        '17:45:00',        9.00,          45, 'TARDANZA'),
    ('2026-06-10',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-11',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL'),
    ('2026-06-12',        '08:00:00',        '17:00:00',        9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas, tardanza, estado)
WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);


-- ============================================================
-- JUSTIFICACIONES DE TARDANZA — Mayo 2026
-- ============================================================

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Tuve un accidente de tránsito en la Av. Arequipa que bloqueó el tráfico durante más de una hora. Adjunto la constancia policial.',
    'PENDIENTE', '2026-05-06 08:30:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '12345678' AND ra.fecha = '2026-05-05'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Mi hijo tuvo una emergencia médica en la madrugada y tuve que llevarlo a urgencias. Tengo el parte médico del Hospital Rebagliati.',
    'PENDIENTE', '2026-05-08 07:45:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '12345678' AND ra.fecha = '2026-05-07'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'El servicio de tren eléctrico sufrió una interrupción desde la estación Grau hasta las 08:40. Tengo el comprobante de retraso emitido por el Metro de Lima.',
    'PENDIENTE', '2026-05-07 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '23456789' AND ra.fecha = '2026-05-06'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Corte de luz en mi domicilio desde las 06:00 hasta las 08:50 según Enel. El ascensor del edificio no funcionó y tuve que bajar 12 pisos a pie con mis equipos médicos.',
    'PENDIENTE', '2026-05-13 07:30:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '23456789' AND ra.fecha = '2026-05-12'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Paro de transporte público en el distrito de Breña. Ningún bus pasó por mi zona durante más de dos horas. Tuve que caminar hasta la Av. Brasil para tomar taxi.',
    'PENDIENTE', '2026-05-09 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '56789012' AND ra.fecha = '2026-05-08'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Acudí al RENIEC para renovación urgente de DNI previo a un trámite bancario. Me indicaron el día anterior que debía presentarme a primera hora. Tengo el comprobante del trámite.',
    'PENDIENTE', '2026-05-08 07:55:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '78901234' AND ra.fecha = '2026-05-07'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Cita médica de control pre-programada en el Hospital Loayza a las 07:00. La consulta se extendió más de lo esperado por resultados de laboratorio pendientes. Adjunto el carné de atención médica.',
    'PENDIENTE', '2026-05-22 08:10:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '78901234' AND ra.fecha = '2026-05-21'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Accidente de tránsito en el cruce de Av. Colonial y Av. Universitaria bloqueó completamente la vía durante más de 90 minutos según informó RPP. Tengo capturas de pantalla de las noticias.',
    'PENDIENTE', '2026-05-24 09:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '90123456' AND ra.fecha = '2026-05-23'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Falla en el sistema de agua del edificio donde resido causó inundación en el estacionamiento. Tuve que esperar al técnico de mantenimiento para sacar mi vehículo. Tengo el reporte de la administración del edificio.',
    'PENDIENTE', '2026-05-28 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '01234567' AND ra.fecha = '2026-05-27'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);


-- ============================================================
-- SOLICITUDES DE PERMISOS
-- ============================================================

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-04-07', '2026-04-18', 10,
    'Solicito 10 días de vacaciones anuales para viajar con mi familia a Cusco. Es el único período disponible según coordinación con mi jefa inmediata.',
    'APROBADA', 'Vacaciones aprobadas según programación anual. Disfrute su descanso.',
    e.id_empleado, '2026-03-25 10:00:00'::timestamp, '2026-03-20 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-07');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-20', '2026-06-20', 1,
    'Necesito permiso de un día para atender la graduación de mi hijo en la Universidad Nacional Mayor de San Marcos. Evento el 20 de junio a las 10:00 am.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-20');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-03-10', '2026-03-12', 3,
    'Requiero 3 días de reposo por intervención quirúrgica de rodilla derecha (menisco). Adjunto indicación del traumatólogo Dr. Ramírez del Hospital Rebagliati.',
    'APROBADA', 'Permiso médico aprobado. Preséntese con certificado médico al reincorporarse.',
    e.id_empleado, '2026-03-08 14:00:00'::timestamp, '2026-03-07 11:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-03-10');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'VACACIONES', '2026-07-14', '2026-07-25', 10,
    'Solicito mis vacaciones anuales correspondientes al período julio 2026. Coordiné previamente con el jefe de Medicina para la cobertura durante mi ausencia.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-07-14');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'OTROS', '2026-04-01', '2026-04-02', 2,
    'Solicito 2 días para asistir a un congreso de administración hospitalaria en Arequipa. El evento es organizado por la SUNEDU.',
    'RECHAZADA', 'Solicitud no aprobada. No hay presupuesto asignado para este tipo de eventos en el período. Puede coordinar para el próximo ejercicio.',
    e.id_empleado, '2026-03-28 09:30:00'::timestamp, '2026-03-27 16:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '34567890'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-01');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-25', '2026-06-25', 1,
    'Permiso para trámites de notaría por fallecimiento de familiar. Liquidación de herencia requiere presencia física en notaría.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '34567890'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-25');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-05-19', '2026-05-19', 1,
    'Cita odontológica de urgencia. Absceso dental que no permite trabajar con normalidad.',
    'RECHAZADA', 'La solicitud se recibió extemporáneamente. Para permisos médicos de urgencia debe comunicarse directamente con RRHH. Se recomienda programar citas fuera del horario laboral.',
    e.id_empleado, '2026-05-20 08:00:00'::timestamp, '2026-05-18 17:30:00'::timestamp
FROM empleado e WHERE e.numero_di = '45678901'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-05-19');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-02-02', '2026-02-13', 10,
    'Solicito vacaciones anuales. Tengo 10 días pendientes del período anterior.',
    'APROBADA', 'Vacaciones aprobadas. Coordine entrega de pendientes antes de su salida.',
    e.id_empleado, '2026-01-25 11:00:00'::timestamp, '2026-01-22 10:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '45678901'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-02-02');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-18', '2026-06-18', 1,
    'Solicito un día de permiso para acompañar a mi madre a cirugía programada en el Hospital Guillermo Almenara. No hay nadie más que pueda acompañarla.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '56789012'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-18');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-01-05', '2026-01-16', 10,
    'Vacaciones del período diciembre 2025 - enero 2026 según cronograma establecido por jefatura.',
    'APROBADA', 'Aprobado según programación anual de vacaciones del área de Medicina.',
    e.id_empleado, '2025-12-20 10:00:00'::timestamp, '2025-12-15 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '67890123'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-01-05');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'OTROS', '2026-06-30', '2026-07-01', 2,
    'Solicito 2 días para capacitación externa en manejo de equipos de imagen de última generación. El curso es dictado por Phillips Medical en Lima.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '67890123'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-30');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-04-22', '2026-04-23', 2,
    'Reposo médico por cuadro viral agudo con diagnóstico de faringitis bacteriana. El médico indica 2 días de reposo.',
    'APROBADA', 'Permiso médico aprobado. Adjunte descanso médico al reincorporarse.',
    e.id_empleado, '2026-04-21 20:00:00'::timestamp, '2026-04-21 19:30:00'::timestamp
FROM empleado e WHERE e.numero_di = '78901234'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-22');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'VACACIONES', '2026-07-07', '2026-07-11', 5,
    'Solicito 5 días de vacaciones para la primera quincena de julio. Coordiné con la jefa de recepción para mi reemplazo.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-07-07');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-05-02', '2026-05-02', 1,
    'Necesito un día para asistir a diligencia judicial citada por el Poder Judicial. Tengo que dar declaración como testigo.',
    'RECHAZADA', 'La notificación judicial debe presentarse a RRHH con 5 días de anticipación para procesar el permiso. Presente la documentación para el registro.',
    e.id_empleado, '2026-04-30 16:00:00'::timestamp, '2026-04-29 14:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-05-02');

INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-03-16', '2026-03-27', 10,
    'Solicito mis 10 días de vacaciones anuales para el mes de marzo según cronograma del área de Radiología.',
    'APROBADA', 'Vacaciones aprobadas. Asegúrese de dejar los equipos calibrados antes de salir.',
    e.id_empleado, '2026-03-05 10:00:00'::timestamp, '2026-03-03 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-03-16');


-- ============================================================
-- HISTORIAL DE AUDITORIA
-- ============================================================

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO',
    e.id_empleado,
    'CREAR_EMPLEADO',
    NULL,
    'Empleado registrado: ' || e.nombres || ' ' || e.apellidos ||
        ' | DNI: ' || e.numero_di ||
        ' | Estado: ACTIVO',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    e.creado_el
FROM empleado e
WHERE e.numero_di IN ('12345678','23456789','34567890','45678901','56789012','67890123','78901234','89012345','90123456','01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CREAR_EMPLEADO'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO',
    e.id_empleado,
    'ASIGNACION_CARGO',
    'Sin cargo',
    'Cargo asignado: ' || c.cargo ||
        ' | Tipo contrato: ' || c.tipo_contrato ||
        ' | Sueldo: S/ ' || c.sueldo ||
        ' | Dpto: ' || d.nombre,
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    c.fecha_inicio::timestamp
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado
INNER JOIN departamento d ON d.id_dpto = c.id_dpto
WHERE e.numero_di IN ('12345678','23456789','34567890','45678901','56789012','67890123','78901234','89012345','90123456','01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'ASIGNACION_CARGO'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'ACTUALIZAR_DATOS',
    'Dirección anterior registrada en sistema',
    'Actualización de datos personales: dirección, teléfono y correo actualizados según solicitud del empleado.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '6 months')
FROM empleado e
WHERE e.numero_di IN ('12345678', '34567890', '67890123', '90123456')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'ACTUALIZAR_DATOS'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'REGISTRAR_ASCENSO',
    'Cargo anterior: ' || c.cargo,
    'Nuevo cargo: ' || c.cargo || ' (confirmado en puesto) | Sueldo actualizado a: S/ ' || (c.sueldo * 1.15)::decimal(10,2),
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '18 months')
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
WHERE e.numero_di IN ('12345678', '23456789', '67890123', '01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'REGISTRAR_ASCENSO'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'CAMBIO_SALARIAL',
    'Sueldo anterior: S/ ' || c.sueldo,
    'Nuevo sueldo: S/ ' || (c.sueldo * 1.08)::decimal(10,2) || ' | Motivo: Incremento anual por evaluación de desempeño',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '24 months')
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
WHERE e.numero_di IN ('12345678', '23456789', '34567890', '45678901', '56789012', '67890123', '78901234', '90123456', '01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CAMBIO_SALARIAL'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'DESACTIVAR_EMPLEADO',
    'Estado: ACTIVO | Cargo: Enfermero',
    'Estado: DESVINCULADO | Motivo: Fin de contrato a plazo fijo. Contrato vencido el 08/04/2023. No renovación por reestructuración del área de Enfermería.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2023-04-09 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '89012345'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'DESACTIVAR_EMPLEADO'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'REGISTRAR_SANCION',
    'Sin sanciones previas',
    'Sanción: AMONESTACION_VERBAL | Motivo: Tardanzas reiteradas durante el mes de enero 2026. Se registran 4 tardanzas superiores a 30 minutos.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2026-02-05 10:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'REGISTRAR_SANCION'
  );

INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'CAMBIO_AREA',
    'Área anterior: Administración',
    'Nueva área: Administración (reasignación interna) | Nuevo cargo: Recepcionista Senior | Motivo: Reorganización del área de atención al paciente.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2025-03-01 08:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CAMBIO_AREA'
  );


-- ============================================================
-- NÓMINA — MAYO 2026 (período completo, estado PAGADA)
-- ============================================================

INSERT INTO nomina (
    periodo, fecha_inicio, fecha_fin, fecha_emision,
    sueldo_base, total_horas_trabajadas, total_horas_extra, total_minutos_tardanza,
    bonif_riesgo,
    descuento_tardanzas, descuento_ley,
    sueldo_bruto, sueldo_neto,
    estado_pago, version, id_empleado, creado_el, actualizado_el
)
SELECT
    '2026-05', '2026-05-01', '2026-05-31', '2026-06-01',
    d.sueldo,
    168.00, 0.00, d.min_tardanza,
    ROUND(d.sueldo * 0.10, 2),
    ROUND(d.min_tardanza * d.sueldo / 10080.0, 2),
    ROUND(d.sueldo * 0.09, 2),
    ROUND(d.sueldo + d.sueldo * 0.10, 2),
    ROUND(
        (d.sueldo + ROUND(d.sueldo * 0.10, 2))
        - ROUND(d.sueldo * 0.09, 2)
        - ROUND(d.min_tardanza * d.sueldo / 10080.0, 2),
    2),
    'PAGADA', 1, e.id_empleado, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('00000001'::varchar, 5000.00::numeric,   0::int),
    ('12345678',           3200.00,          145),
    ('23456789',           5500.00,          115),
    ('34567890',           3200.00,           80),
    ('45678901',           2400.00,          100),
    ('56789012',           2800.00,           90),
    ('67890123',           7200.00,           60),
    ('78901234',           3800.00,          100),
    ('90123456',           2200.00,          125),
    ('01234567',           6500.00,           35),
    ('11223300',           3500.00,           70),
    ('22334400',           3200.00,           45),
    ('33445500',           2800.00,           90),
    ('44556600',           4200.00,           55),
    ('55667700',           2400.00,          110),
    ('66778800',          12000.00,            0),
    ('77889900',           9500.00,           20),
    ('88990011',           8800.00,           15)
) AS d(numero_di, sueldo, min_tardanza)
JOIN empleado e ON e.numero_di = d.numero_di
WHERE NOT EXISTS (
    SELECT 1 FROM nomina n WHERE n.id_empleado = e.id_empleado AND n.periodo = '2026-05'
);


-- ============================================================
-- NÓMINA — JUNIO 2026 (10 días laborables, estado CALCULADA)
-- ============================================================

INSERT INTO nomina (
    periodo, fecha_inicio, fecha_fin, fecha_emision,
    sueldo_base, total_horas_trabajadas, total_horas_extra, total_minutos_tardanza,
    bonif_riesgo,
    descuento_tardanzas, descuento_ley,
    sueldo_bruto, sueldo_neto,
    estado_pago, version, id_empleado, creado_el, actualizado_el
)
SELECT
    '2026-06', '2026-06-01', '2026-06-30', CURRENT_DATE,
    d.sueldo,
    80.00, 0.00, 0,
    ROUND(d.sueldo * 0.10, 2),
    0.00,
    ROUND(d.sueldo * 0.09, 2),
    ROUND(d.sueldo + d.sueldo * 0.10, 2),
    ROUND(
        (d.sueldo + ROUND(d.sueldo * 0.10, 2))
        - ROUND(d.sueldo * 0.09, 2),
    2),
    'CALCULADA', 1, e.id_empleado, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('00000001'::varchar, 5000.00::numeric),
    ('12345678',           3200.00),
    ('23456789',           5500.00),
    ('34567890',           3200.00),
    ('45678901',           2400.00),
    ('56789012',           2800.00),
    ('67890123',           7200.00),
    ('78901234',           3800.00),
    ('90123456',           2200.00),
    ('01234567',           6500.00),
    ('11223300',           3500.00),
    ('22334400',           3200.00),
    ('33445500',           2800.00),
    ('44556600',           4200.00),
    ('55667700',           2400.00),
    ('66778800',          12000.00),
    ('77889900',           9500.00),
    ('88990011',           8800.00)
) AS d(numero_di, sueldo)
JOIN empleado e ON e.numero_di = d.numero_di
WHERE NOT EXISTS (
    SELECT 1 FROM nomina n WHERE n.id_empleado = e.id_empleado AND n.periodo = '2026-06'
);


-- =========================
-- HORARIOS BASE
-- =========================

INSERT INTO horario(nombre_turno, hora_entrada, hora_salida, tolerancia, umbral_extra, activo, creado_el, actualizado_el)
SELECT 'Turno Mañana', '07:00', '15:00', 10, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM horario WHERE nombre_turno = 'Turno Mañana');

INSERT INTO horario(nombre_turno, hora_entrada, hora_salida, tolerancia, umbral_extra, activo, creado_el, actualizado_el)
SELECT 'Turno Tarde', '15:00', '23:00', 10, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM horario WHERE nombre_turno = 'Turno Tarde');

INSERT INTO horario(nombre_turno, hora_entrada, hora_salida, tolerancia, umbral_extra, activo, creado_el, actualizado_el)
SELECT 'Turno Noche', '23:00', '07:00', 10, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM horario WHERE nombre_turno = 'Turno Noche');


-- =========================
-- ASIGNACIONES DE HORARIO
-- Nota: fecha_desde = CURRENT_DATE para no interferir con los registros históricos
--       de asistencia del mes de mayo 2026 que ya tienen minutos_tardanza calculados.
-- =========================

-- Turno Mañana: Admin RRHH, Ana García (ENF), Carlos Méndez (MED), María Quispe (ADM),
--               Luis Fernández (LAB), Elena Castro (FIN), Sofía Delgado (ADM),
--               Laura Huanca (RRHH), Marco Díaz (RRHH), Isabel Torres (RRHH),
--               Rodrigo Benítez (RRHH), Claudia Morales (RRHH),
--               Fernando Castillo (DIR), Patricia Medina (DIR), Alejandro Vargas (DIR)

INSERT INTO asignacion_horario(id_empleado, id_horario, fecha_desde, fecha_hasta, es_temporal, activo, creado_el, actualizado_el)
SELECT e.id_empleado, h.id_horario, CURRENT_DATE, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e
CROSS JOIN horario h
WHERE h.nombre_turno = 'Turno Mañana'
  AND e.numero_di IN ('00000001','12345678','23456789','34567890','45678901',
                      '78901234','90123456','11223300','22334400','33445500',
                      '44556600','55667700','66778800','77889900','88990011')
  AND NOT EXISTS (
      SELECT 1 FROM asignacion_horario a
      WHERE a.id_empleado = e.id_empleado AND a.activo = true
  );

-- Turno Tarde: Rosa Vargas (ENF), Pedro Ramos (MED), Miguel Paredes (RAD)

INSERT INTO asignacion_horario(id_empleado, id_horario, fecha_desde, fecha_hasta, es_temporal, activo, creado_el, actualizado_el)
SELECT e.id_empleado, h.id_horario, CURRENT_DATE, NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e
CROSS JOIN horario h
WHERE h.nombre_turno = 'Turno Tarde'
  AND e.numero_di IN ('56789012','67890123','01234567')
  AND NOT EXISTS (
      SELECT 1 FROM asignacion_horario a
      WHERE a.id_empleado = e.id_empleado AND a.activo = true
  );

-- =========================
-- TURNO GENERAL (empleado_test y gerencia_test)
-- =========================

INSERT INTO horario(nombre_turno, hora_entrada, hora_salida, tolerancia, umbral_extra, activo, creado_el, actualizado_el)
SELECT 'Turno General', '08:00', '17:00', 10, 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM horario WHERE nombre_turno = 'Turno General');

-- Asignación con fecha_desde retrocedida 7 días para cubrir los registros históricos de prueba
INSERT INTO asignacion_horario(id_empleado, id_horario, fecha_desde, fecha_hasta, es_temporal, activo, creado_el, actualizado_el)
SELECT e.id_empleado, h.id_horario, CURRENT_DATE - INTERVAL '7 days', NULL, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e
CROSS JOIN horario h
WHERE h.nombre_turno = 'Turno General'
  AND e.numero_di IN ('99990001', '99990002')
  AND NOT EXISTS (
      SELECT 1 FROM asignacion_horario a
      WHERE a.id_empleado = e.id_empleado AND a.activo = true
  );

-- =========================
-- REGISTROS DE ASISTENCIA DE PRUEBA (empleado_test)
-- Tardanza >= 60 min para poder probar el flujo de justificación
-- =========================

-- Ayer: entró 2 horas tarde (10:00), completó jornada hasta las 17:00
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, minutos_extra, estado, tipo_ultimo_registro, tipo_registro, observacion, id_empleado)
SELECT
    CURRENT_DATE - INTERVAL '1 day',
    '10:00:00', '17:00:00', 7.00, 120, 0,
    'TARDANZA', 'SALIDA', 'ORIGINAL',
    'Tardanza de prueba - pendiente justificación',
    e.id_empleado
FROM empleado e
WHERE e.numero_di = '99990001'
  AND NOT EXISTS (
      SELECT 1 FROM registro_asistencia r
      WHERE r.id_empleado = e.id_empleado
        AND r.fecha = CURRENT_DATE - INTERVAL '1 day'
        AND r.tipo_registro = 'ORIGINAL'
  );

-- Hoy: entró 90 minutos tarde (09:30), aún sin salida
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, minutos_extra, estado, tipo_ultimo_registro, tipo_registro, observacion, id_empleado)
SELECT
    CURRENT_DATE,
    '09:30:00', NULL, 0.00, 90, 0,
    'TARDANZA', 'ENTRADA', 'ORIGINAL',
    'Tardanza de prueba - pendiente justificación',
    e.id_empleado
FROM empleado e
WHERE e.numero_di = '99990001'
  AND NOT EXISTS (
      SELECT 1 FROM registro_asistencia r
      WHERE r.id_empleado = e.id_empleado
        AND r.fecha = CURRENT_DATE
        AND r.tipo_registro = 'ORIGINAL'
  );


-- =============================================================
-- REGISTROS DE ASISTENCIA Y NÓMINA HISTÓRICA Enero-Mayo 2026
-- Empleados activos con contratos activos
-- =============================================================

-- =========================
-- 1. REGISTRO DE ASISTENCIA (días hábiles Ene–May 2026)
--    Genera ~22 días/mes × 9 empleados = ~990 registros
-- =========================
INSERT INTO registro_asistencia (
    fecha, hora_entrada, hora_salida, horas_trabajadas,
    minutos_tardanza, minutos_extra, estado,
    tipo_registro, tipo_ultimo_registro, observacion, id_empleado
)
SELECT
    d::date,
    '08:00:00'::time,
    '17:00:00'::time,
    8.00,
    0,
    0,
    'PUNTUAL',
    'ORIGINAL',
    'SALIDA',
    'Registro histórico',
    e.id_empleado
FROM generate_series('2026-01-02'::date, '2026-05-31'::date, '1 day'::interval) d
CROSS JOIN empleado e
WHERE e.estado = 'ACTIVO'
  AND EXTRACT(DOW FROM d::date) NOT IN (0, 6)
  AND NOT EXISTS (
      SELECT 1 FROM registro_asistencia ra
      WHERE ra.id_empleado = e.id_empleado
        AND ra.fecha = d::date
        AND ra.tipo_registro = 'ORIGINAL'
  );

UPDATE registro_asistencia SET
    hora_entrada       = '08:47:00',
    minutos_tardanza   = 47,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-01-14'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('12345678','56789012','90123456'));

UPDATE registro_asistencia SET
    hora_entrada       = '09:15:00',
    minutos_tardanza   = 75,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-02-18'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('23456789','78901234'));

UPDATE registro_asistencia SET
    hora_entrada       = '08:32:00',
    minutos_tardanza   = 32,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-03-05'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('34567890','67890123','01234567'));

UPDATE registro_asistencia SET
    hora_entrada       = '08:55:00',
    minutos_tardanza   = 55,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-04-22'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('45678901','23456789'));

UPDATE registro_asistencia SET
    hora_entrada       = '09:02:00',
    minutos_tardanza   = 62,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-05-07'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('56789012','01234567'));

-- =========================
-- 2. NÓMINA Ene–May 2026
-- =========================
WITH empleados_activos AS (
    SELECT e.id_empleado, c.sueldo
    FROM empleado e
    JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
    WHERE e.estado = 'ACTIVO'
),
meses AS (
    SELECT * FROM (VALUES
        ('2026-01', '2026-01-02'::date, '2026-01-31'::date, 21),
        ('2026-02', '2026-02-01'::date, '2026-02-28'::date, 20),
        ('2026-03', '2026-03-01'::date, '2026-03-31'::date, 21),
        ('2026-04', '2026-04-01'::date, '2026-04-30'::date, 22),
        ('2026-05', '2026-05-01'::date, '2026-05-31'::date, 22)
    ) AS t(periodo, fecha_inicio, fecha_fin, dias_lab)
),
calculo AS (
    SELECT
        m.periodo, m.fecha_inicio, m.fecha_fin, m.dias_lab, ea.id_empleado,
        ea.sueldo                                                      AS sueldo_base,
        CAST(m.dias_lab * 8 AS DECIMAL(10,2))                         AS total_horas,
        ROUND(ea.sueldo * 0.15, 2)                                    AS bonif_noct,
        ROUND(50.00 * m.dias_lab, 2)                                  AS bonif_guard,
        ROUND(ea.sueldo * 0.10, 2)                                    AS bonif_riesgo,
        ROUND(ea.sueldo * 0.20, 2)                                    AS bonif_cargo,
        COALESCE((
            SELECT SUM(ra.minutos_tardanza)
            FROM registro_asistencia ra
            WHERE ra.id_empleado = ea.id_empleado
              AND ra.fecha BETWEEN m.fecha_inicio AND m.fecha_fin
              AND ra.tipo_registro = 'ORIGINAL'
        ), 0)                                                          AS total_min_tard
    FROM meses m CROSS JOIN empleados_activos ea
),
con_bruto AS (
    SELECT *,
        ROUND(sueldo_base + bonif_noct + bonif_guard + bonif_riesgo + bonif_cargo, 2) AS bruto,
        ROUND((total_min_tard * sueldo_base) / NULLIF((30.0 * 8.0 * 60.0), 0), 2)    AS desc_tardanzas
    FROM calculo
)
INSERT INTO nomina (
    periodo, fecha_inicio, fecha_fin, fecha_emision,
    sueldo_base, total_horas_trabajadas, total_horas_extra, total_minutos_tardanza,
    bonif_familiar, bonif_turno_nocturno, bonif_guardia, bonif_riesgo, bonif_cargo,
    descuento_tardanzas, descuento_ley, asignacion_familiar,
    sueldo_bruto, sueldo_neto,
    cantidad_hijos, tiene_hijos, cantidad_guardias, total_horas_nocturnas,
    tipo_pension_aplicada, estado_pago, version,
    id_empleado, calculado_por
)
SELECT
    cb.periodo, cb.fecha_inicio, cb.fecha_fin, cb.fecha_fin,
    cb.sueldo_base,
    cb.total_horas,
    0.00, cb.total_min_tard,
    0.00, cb.bonif_noct, cb.bonif_guard, cb.bonif_riesgo, cb.bonif_cargo,
    cb.desc_tardanzas,
    ROUND(cb.bruto * 0.13, 2),
    0.00, cb.bruto,
    ROUND(cb.bruto - cb.desc_tardanzas - ROUND(cb.bruto * 0.13, 2), 2),
    0, false, cb.dias_lab, 0.00,
    'ONP', 'PAGADA', 1,
    cb.id_empleado,
    (SELECT u.id_usuario FROM usuario u
     JOIN rol r ON u.id_rol = r.id_rol
     WHERE r.nombre_rol = 'RRHH' AND u.activo = true
     LIMIT 1)
FROM con_bruto cb
WHERE NOT EXISTS (
    SELECT 1 FROM nomina n
    WHERE n.id_empleado = cb.id_empleado AND n.periodo = cb.periodo
);
