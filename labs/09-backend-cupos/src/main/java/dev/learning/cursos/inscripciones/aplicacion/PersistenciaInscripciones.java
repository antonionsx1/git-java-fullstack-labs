package dev.learning.cursos.inscripciones.aplicacion;

public interface PersistenciaInscripciones {
    boolean consumirCupo(long cursoId);

    void guardarInscripcion(long cursoId, long alumnoId);
}
