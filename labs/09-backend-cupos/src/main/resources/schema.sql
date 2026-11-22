CREATE TABLE IF NOT EXISTS curso (
    id BIGINT PRIMARY KEY,
    titulo TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cupo (
    curso_id BIGINT PRIMARY KEY REFERENCES curso(id),
    disponibles INTEGER NOT NULL CHECK (disponibles >= 0)
);

CREATE TABLE IF NOT EXISTS inscripcion (
    curso_id BIGINT NOT NULL REFERENCES cupo(curso_id),
    alumno_id BIGINT NOT NULL CHECK (alumno_id > 0),
    PRIMARY KEY (curso_id, alumno_id)
);
