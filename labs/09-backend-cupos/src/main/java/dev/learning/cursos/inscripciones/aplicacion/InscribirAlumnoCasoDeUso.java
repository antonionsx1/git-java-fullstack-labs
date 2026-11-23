package dev.learning.cursos.inscripciones.aplicacion;

import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;

public class InscribirAlumnoCasoDeUso implements InscribirAlumno {
    private final PersistenciaInscripciones persistencia;

    public InscribirAlumnoCasoDeUso(PersistenciaInscripciones persistencia) {
        this.persistencia = persistencia;
    }

    @Override
    public boolean inscribir(CursoId cursoId, AlumnoId alumnoId) {
        if (!persistencia.consumirCupo(cursoId)) {
            return false;
        }
        persistencia.guardarInscripcion(cursoId, alumnoId);
        return true;
    }
}
