package dev.learning.cursos.catalogo;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CatalogoController {
    private final JdbcTemplate jdbc;

    CatalogoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/cursos")
    List<Curso> listar() {
        return jdbc.query("SELECT id, titulo FROM curso ORDER BY id",
                (row, index) -> new Curso(row.getLong("id"), row.getString("titulo")));
    }

    record Curso(long id, String titulo) {}
}
