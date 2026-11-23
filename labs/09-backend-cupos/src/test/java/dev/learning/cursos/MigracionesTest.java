package dev.learning.cursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MigracionesTest {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;

    @Test
    void migraDesdeUnEsquemaVacio() {
        enEsquemaTemporal(esquema -> {
            assertThat(flyway(esquema, false).migrate().migrationsExecuted).isEqualTo(3);
            assertThat(jdbc.queryForObject("SELECT disponibles FROM " + esquema + ".cupo WHERE curso_id = 1",
                    Integer.class)).isEqualTo(1);
            assertThat(flyway(esquema, false).migrate().migrationsExecuted).isZero();
        });
    }

    @Test
    void conservaLaInscripcionHistoricaAlAdoptarV1() {
        enEsquemaTemporal(esquema -> {
            jdbc.execute((ConnectionCallback<Void>) connection -> {
                String anterior = connection.getSchema();
                connection.setSchema(esquema);
                try {
                    ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/migration/V1__esquema_inicial.sql"));
                } finally {
                    connection.setSchema(anterior);
                }
                return null;
            });
            jdbc.update("INSERT INTO " + esquema + ".curso VALUES (1, 'Curso existente')");
            jdbc.update("INSERT INTO " + esquema + ".cupo VALUES (1, 0)");
            jdbc.update("INSERT INTO " + esquema + ".inscripcion VALUES (1, 101)");

            assertThatThrownBy(() -> flyway(esquema, false).migrate()).isInstanceOf(FlywayException.class)
                    .hasMessageContaining("baseline");
            assertThat(flyway(esquema, true).migrate().migrationsExecuted).isEqualTo(2);
            assertThat(jdbc.queryForObject("SELECT titulo FROM " + esquema + ".curso WHERE id = 1", String.class))
                    .isEqualTo("Curso existente");
            assertThat(jdbc.queryForObject("SELECT disponibles FROM " + esquema + ".cupo WHERE curso_id = 1",
                    Integer.class)).isZero();
            assertThat(jdbc.queryForList("SELECT alumno_id FROM " + esquema + ".inscripcion"
                    + " WHERE curso_id = 1 AND confirmada_en IS NULL", Long.class)).containsExactly(101L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + esquema + ".inscripcion", Long.class))
                    .isEqualTo(1L);
            assertThat(flyway(esquema, false).migrate().migrationsExecuted).isZero();
        });
    }

    private Flyway flyway(String esquema, boolean adoptarV1) {
        return Flyway.configure().dataSource(dataSource).schemas(esquema).baselineVersion("1")
                .baselineOnMigrate(adoptarV1).load();
    }

    private void enEsquemaTemporal(java.util.function.Consumer<String> prueba) {
        String esquema = "prueba_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA " + esquema);
        try {
            prueba.accept(esquema);
        } finally {
            jdbc.execute("DROP SCHEMA " + esquema + " CASCADE");
        }
    }
}
