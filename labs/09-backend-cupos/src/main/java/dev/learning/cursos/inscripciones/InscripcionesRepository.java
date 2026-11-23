package dev.learning.cursos.inscripciones;

import dev.learning.cursos.inscripciones.aplicacion.InscripcionDuplicadaException;
import dev.learning.cursos.inscripciones.aplicacion.PersistenciaInscripciones;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class InscripcionesRepository implements PersistenciaInscripciones {
    private final JdbcTemplate jdbc;

    InscripcionesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean consumirCupo(long cursoId) {
        return jdbc.update(
                "UPDATE cupo SET disponibles = disponibles - 1 WHERE curso_id = ? AND disponibles > 0",
                cursoId) == 1;
    }

    @Override
    public void guardarInscripcion(long cursoId, long alumnoId) {
        try {
            jdbc.update("INSERT INTO inscripcion (curso_id, alumno_id) VALUES (?, ?)", cursoId, alumnoId);
        } catch (DuplicateKeyException ex) {
            throw new InscripcionDuplicadaException(ex);
        }
    }
}
