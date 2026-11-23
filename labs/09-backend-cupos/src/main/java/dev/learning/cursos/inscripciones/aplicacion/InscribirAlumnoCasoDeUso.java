package dev.learning.cursos.inscripciones.aplicacion;

public class InscribirAlumnoCasoDeUso implements InscribirAlumno {
    private final PersistenciaInscripciones persistencia;

    public InscribirAlumnoCasoDeUso(PersistenciaInscripciones persistencia) {
        this.persistencia = persistencia;
    }

    @Override
    public boolean inscribir(long cursoId, long alumnoId) {
        if (!persistencia.consumirCupo(cursoId)) {
            return false;
        }
        persistencia.guardarInscripcion(cursoId, alumnoId);
        return true;
    }
}
