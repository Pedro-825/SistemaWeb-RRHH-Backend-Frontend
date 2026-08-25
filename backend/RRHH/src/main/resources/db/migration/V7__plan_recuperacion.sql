CREATE TABLE plan_recuperacion (
    id_plan SERIAL PRIMARY KEY,
    id_justificacion INTEGER NOT NULL REFERENCES justificacion_tardanza(id_justificacion),
    id_empleado BIGINT NOT NULL REFERENCES empleado(id_empleado),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TARDANZA','INASISTENCIA')),
    fecha DATE NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida_original TIME NOT NULL,
    minutos_recuperacion INTEGER NOT NULL,
    hora_salida_modificada TIME NOT NULL,
    creado_el TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plan_recuperacion_justificacion ON plan_recuperacion(id_justificacion);
