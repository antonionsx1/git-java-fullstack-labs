package dev.learning.cursos.inscripciones.dominio;

public record CursoId(long valor) {
    public CursoId {
        if (valor <= 0) {
            throw new IllegalArgumentException("CursoId debe ser positivo");
        }
    }
}
