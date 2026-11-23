package dev.learning.cursos;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InscripcionesTest {
    private static final String EMISOR = "https://issuer.example.test";
    private static final String AUDIENCIA = "cursos-lab";
    private static final KeyPair CLAVES = clavesDePrueba();
    private static final KeyPair CLAVES_AJENAS = clavesDePrueba();

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @DynamicPropertySource
    static void configJwt(DynamicPropertyRegistry propiedades) throws Exception {
        var archivo = Files.createTempFile("cursos-jwt-publica-", ".pem");
        archivo.toFile().deleteOnExit();
        var pem = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(CLAVES.getPublic().getEncoded());
        Files.writeString(archivo, "-----BEGIN PUBLIC KEY-----\n" + pem + "\n-----END PUBLIC KEY-----\n");
        propiedades.add("cursos.seguridad.clave-publica", () -> archivo.toUri().toString());
        propiedades.add("cursos.seguridad.emisor", () -> EMISOR);
        propiedades.add("cursos.seguridad.audiencia", () -> AUDIENCIA);
        propiedades.add("cursos.seguridad.alumnos[alumno-101]", () -> 101);
        propiedades.add("cursos.seguridad.alumnos[alumno-102]", () -> 102);
    }

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
            assertThat(jdbc.queryForObject(
                    "SELECT confirmada_en IS NOT NULL FROM inscripcion WHERE curso_id = 1", Boolean.class))
                    .isTrue();
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
    void entradaInvalidaOIdentificadorAjenoNoConsumeCupo() throws Exception {
        String token = tokenDeAlumno(101);
        assertThat(post("/cursos/0/inscripciones", token)).isEqualTo(400);
        assertThat(post("/cursos/1/inscripciones?alumnoId=102", token)).isEqualTo(400);
        verificarEstado(1, 0);

        assertThat(post("/cursos/1/inscripciones", token)).isEqualTo(201);
        assertThat(jdbc.queryForList("SELECT alumno_id FROM inscripcion WHERE curso_id = 1", Long.class))
                .containsExactly(101L);
    }

    @Test
    void catalogoPublicoYAltaSinCredencialesRechazada() throws Exception {
        var catalogo = HttpRequest.newBuilder(uri("/cursos")).timeout(Duration.ofSeconds(10)).build();
        assertThat(http.send(catalogo, HttpResponse.BodyHandlers.discarding()).statusCode()).isEqualTo(200);
        assertThat(post("/cursos/1/inscripciones", null)).isEqualTo(401);
        verificarEstado(1, 0);
    }

    @Test
    void rechazaAutenticacionInvalidaSinConsumirCupo() throws Exception {
        String ruta = "/cursos/1/inscripciones";
        String permiso = "inscripciones:crear";
        Instant futuro = Instant.now().plusSeconds(300);
        assertThat(post(ruta, token(CLAVES_AJENAS, EMISOR, AUDIENCIA, "alumno-101", permiso, futuro))).isEqualTo(401);
        assertThat(post(ruta, token(CLAVES, "https://otro.example.test", AUDIENCIA, "alumno-101", permiso, futuro)))
                .isEqualTo(401);
        assertThat(post(ruta, token(CLAVES, EMISOR, "otra-api", "alumno-101", permiso, futuro))).isEqualTo(401);
        assertThat(post(ruta, token(CLAVES, EMISOR, AUDIENCIA, "alumno-101", permiso, Instant.now().minusSeconds(300))))
                .isEqualTo(401);
        verificarEstado(1, 0);
    }

    @Test
    void rechazaIdentidadSinPermisoOSinVinculoSinConsumirCupo() throws Exception {
        String ruta = "/cursos/1/inscripciones";
        Instant futuro = Instant.now().plusSeconds(300);
        assertThat(post(ruta, token(CLAVES, EMISOR, AUDIENCIA, "alumno-101", "catalogo:leer", futuro)))
                .isEqualTo(403);
        assertThat(post(ruta, token(CLAVES, EMISOR, AUDIENCIA, "sin-vinculo", "inscripciones:crear", futuro)))
                .isEqualTo(403);
        verificarEstado(1, 0);
    }

    private int inscribir(long alumno) throws Exception {
        return post("/cursos/1/inscripciones", tokenDeAlumno(alumno));
    }

    private int post(String ruta, String token) throws Exception {
        var request = HttpRequest.newBuilder(uri(ruta)).timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private URI uri(String ruta) {
        return URI.create("http://127.0.0.1:" + port + ruta);
    }

    private static String tokenDeAlumno(long alumno) throws Exception {
        return token(CLAVES, EMISOR, AUDIENCIA, "alumno-" + alumno, "inscripciones:crear",
                Instant.now().plusSeconds(300));
    }

    private static String token(KeyPair claves, String emisor, String audiencia, String sujeto, String scope,
            Instant expiracion) throws Exception {
        var claims = new JWTClaimsSet.Builder().issuer(emisor).audience(audiencia).subject(sujeto)
                .claim("scope", scope).expirationTime(Date.from(expiracion)).build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner((RSAPrivateKey) claves.getPrivate()));
        return jwt.serialize();
    }

    private static KeyPair clavesDePrueba() {
        try {
            var generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048);
            return generador.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo crear la clave de prueba", ex);
        }
    }

    private void verificarEstado(int disponibles, int inscritos) {
        assertThat(jdbc.queryForObject("SELECT disponibles FROM cupo WHERE curso_id = 1", Integer.class))
                .isEqualTo(disponibles);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM inscripcion WHERE curso_id = 1", Long.class))
                .isEqualTo((long) inscritos);
    }
}
