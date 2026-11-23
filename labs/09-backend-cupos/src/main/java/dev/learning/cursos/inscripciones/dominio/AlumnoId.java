package dev.learning.cursos.inscripciones.dominio;

public record AlumnoId(long valor) {
    public AlumnoId {
        if (valor <= 0) {
            throw new IllegalArgumentException("AlumnoId debe ser positivo");
        }
    }
}
