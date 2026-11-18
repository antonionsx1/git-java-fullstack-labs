package dev.learning.cursos.inscripciones;

import static dev.learning.cursos.inscripciones.ResultadosInscripcion.Resultado.CONFIRMADA;
import static dev.learning.cursos.inscripciones.ResultadosInscripcion.Resultado.CUPO_AGOTADO;
import static dev.learning.cursos.inscripciones.ResultadosInscripcion.Resultado.DUPLICADA;
import static dev.learning.cursos.inscripciones.ResultadosInscripcion.Resultado.FALLO_PERSISTENCIA;

import dev.learning.cursos.inscripciones.aplicacion.InscribirAlumno;
import dev.learning.cursos.inscripciones.aplicacion.InscripcionDuplicadaException;
import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;
import dev.learning.cursos.seguridad.IdentidadesAlumnos;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class InscripcionesController {
    private final InscribirAlumno inscripciones;
    private final IdentidadesAlumnos identidades;
    private final ResultadosInscripcion resultados;

    InscripcionesController(InscribirAlumno inscripciones, IdentidadesAlumnos identidades,
            ResultadosInscripcion resultados) {
        this.inscripciones = inscripciones;
        this.identidades = identidades;
        this.resultados = resultados;
    }

    @PostMapping("/cursos/{cursoId}/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    void inscribir(@PathVariable long cursoId, @AuthenticationPrincipal Jwt identidad,
            @RequestParam(name = "alumnoId", required = false) String alumnoId) {
        Long alumnoVinculado = identidades.alumnos().get(identidad.getSubject());
        if (alumnoVinculado == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (alumnoId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El alumno se obtiene de la identidad");
        }
        CursoId curso;
        try {
            curso = new CursoId(cursoId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, ex);
        }
        long inicio = System.nanoTime();
        try {
            if (!inscripciones.inscribir(curso, new AlumnoId(alumnoVinculado))) {
                resultados.registrar(CUPO_AGOTADO, inicio);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cupo no disponible");
            }
        } catch (InscripcionDuplicadaException ex) {
            resultados.registrar(DUPLICADA, inicio);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alumno ya inscrito", ex);
        } catch (DataAccessException | TransactionException ex) {
            resultados.registrar(FALLO_PERSISTENCIA, inicio);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Inscripciones no disponibles");
        }
        resultados.registrar(CONFIRMADA, inicio);
    }
}
