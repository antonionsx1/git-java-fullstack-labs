package dev.learning.cursos.seguridad;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cursos.seguridad")
public record IdentidadesAlumnos(String emisor, String audiencia, String clavePublica, Map<String, Long> alumnos) {
    public IdentidadesAlumnos {
        alumnos = alumnos == null ? Map.of() : Map.copyOf(alumnos);
        if (alumnos.values().stream().anyMatch(id -> id <= 0)) {
            throw new IllegalArgumentException("Los identificadores de alumnos deben ser positivos");
        }
    }
}
