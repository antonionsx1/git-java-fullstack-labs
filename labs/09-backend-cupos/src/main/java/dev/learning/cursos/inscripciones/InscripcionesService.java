package dev.learning.cursos.inscripciones;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InscripcionesService {
    private final JdbcTemplate jdbc;

    InscripcionesService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean inscribir(long cursoId, long alumnoId) {
        int actualizados = jdbc.update(
                "UPDATE cupo SET disponibles = disponibles - 1 WHERE curso_id = ? AND disponibles > 0",
                cursoId);
        if (actualizados == 0) {
            return false;
        }
        jdbc.update("INSERT INTO inscripcion (curso_id, alumno_id) VALUES (?, ?)", cursoId, alumnoId);
        return true;
    }
}
