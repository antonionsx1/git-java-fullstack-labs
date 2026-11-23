package dev.learning.cursos;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InscripcionesTest {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @BeforeEach
    void reiniciarCurso() {
        jdbc.update("DELETE FROM inscripcion WHERE curso_id = 1");
        jdbc.update("UPDATE cupo SET disponibles = 1 WHERE curso_id = 1");
    }

    @Test
    void soloConfirmaUnaInscripcionParaElUltimoCupo() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var listos = new CountDownLatch(2);
        var salida = new CountDownLatch(1);
        try {
            var primera = pool.submit(() -> enviarAlEstarListos(listos, salida, 101));
            var segunda = pool.submit(() -> enviarAlEstarListos(listos, salida, 102));
            assertThat(listos.await(5, TimeUnit.SECONDS)).isTrue();
            salida.countDown();

            assertThat(new Integer[] {primera.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(201, 409);
            verificarEstado(0, 1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void unaInscripcionDuplicadaNoConsumeOtroCupo() throws Exception {
        jdbc.update("UPDATE cupo SET disponibles = 2 WHERE curso_id = 1");
        assertThat(inscribir(101)).isEqualTo(201);
        assertThat(inscribir(101)).isEqualTo(409);
        verificarEstado(1, 1);
    }

    private int enviarAlEstarListos(CountDownLatch listos, CountDownLatch salida, long alumno)
            throws Exception {
        listos.countDown();
        if (!salida.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("No se libero el punto de inicio");
        }
        return inscribir(alumno);
    }

    @Test
    void identificadoresInvalidosDevuelven400SinConsumirCupo() throws Exception {
        assertThat(inscribir(0, 101)).isEqualTo(400);
        assertThat(inscribir(1, -1)).isEqualTo(400);
        verificarEstado(1, 0);
    }

    private int inscribir(long alumno) throws Exception {
        return inscribir(1, alumno);
    }

    private int inscribir(long curso, long alumno) throws Exception {
        var uri = URI.create("http://127.0.0.1:" + port + "/cursos/" + curso + "/inscripciones?alumnoId=" + alumno);
        var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private void verificarEstado(int disponibles, int inscritos) {
        assertThat(jdbc.queryForObject("SELECT disponibles FROM cupo WHERE curso_id = 1", Integer.class))
                .isEqualTo(disponibles);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM inscripcion WHERE curso_id = 1", Long.class))
                .isEqualTo((long) inscritos);
    }
}
