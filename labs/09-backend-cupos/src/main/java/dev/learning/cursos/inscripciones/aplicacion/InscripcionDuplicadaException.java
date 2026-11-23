package dev.learning.cursos.inscripciones.aplicacion;

public class InscripcionDuplicadaException extends RuntimeException {
    public InscripcionDuplicadaException(Throwable causa) {
        super("Alumno ya inscrito", causa);
    }
}
