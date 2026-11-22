package dev.learning.cursos.inscripciones;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class InscripcionesRepository {
    private final JdbcTemplate jdbc;

    InscripcionesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    boolean consumirCupo(long cursoId) {
        return jdbc.update(
                "UPDATE cupo SET disponibles = disponibles - 1 WHERE curso_id = ? AND disponibles > 0",
                cursoId) == 1;
    }

    void guardarInscripcion(long cursoId, long alumnoId) {
        jdbc.update("INSERT INTO inscripcion (curso_id, alumno_id) VALUES (?, ?)", cursoId, alumnoId);
    }
}
