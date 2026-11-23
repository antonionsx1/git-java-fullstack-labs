package dev.learning.cursos.inscripciones.aplicacion;

import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;

public interface InscribirAlumno {
    boolean inscribir(CursoId cursoId, AlumnoId alumnoId);
}
