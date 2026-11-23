package dev.learning.cursos.inscripciones.aplicacion;

import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;

public interface PersistenciaInscripciones {
    boolean consumirCupo(CursoId cursoId);

    void guardarInscripcion(CursoId cursoId, AlumnoId alumnoId);
}
