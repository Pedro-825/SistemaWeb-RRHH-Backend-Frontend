CREATE TABLE galeria_foto (
    id          BIGSERIAL PRIMARY KEY,
    id_empleado BIGINT NOT NULL REFERENCES empleado(id_empleado),
    url         VARCHAR(500) NOT NULL,
    nombre_archivo VARCHAR(255),
    fecha_subida TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_galeria_empleado ON galeria_foto (id_empleado);
