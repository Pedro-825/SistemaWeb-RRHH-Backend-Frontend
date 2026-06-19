
INSERT INTO rol(nombre_rol)
VALUES ('RRHH');

INSERT INTO rol(nombre_rol)
VALUES ('GERENCIA');

INSERT INTO rol(nombre_rol)
VALUES ('EMPLEADO');
INSERT INTO departamento(
    nombre,
    cod_dpto,
    ubicacion
)
VALUES (
    'Recursos Humanos',
    'RRHH01',
    'PISO 2'
);
INSERT INTO empleado(
    nombres,
    apellidos,
    doc_identidad,
    numero_di,
    fecha_nac,
    sexo,
    direccion,
    correo,
    telefono,
    id_dpto
)
VALUES (
    'Juan',
    'Perez',
    'DNI',
    '12345678',
    '1999-01-10',
    'H',
    'Lima',
    'juan@hospital.com',
    '999999999',
    1
);
INSERT INTO usuario(
    nombre_usuario,
    correo_inst,
    contrasenia,
    id_empleado,
    id_rol,
    dosfa_activo,
    activo
)
VALUES (
    'admin',
    'admin@hospital.com',
    '$2a$10$OnEldLI10DeBsHBMh7LObulVykdqqx7My28h.Kqm/Cv80RFTKK8RS',
    1,
    1,
    false,
    true
);