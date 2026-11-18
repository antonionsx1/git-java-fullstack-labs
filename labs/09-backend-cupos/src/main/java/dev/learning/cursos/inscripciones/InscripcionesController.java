package dev.learning.cursos.inscripciones;

import dev.learning.cursos.inscripciones.aplicacion.InscribirAlumno;
import dev.learning.cursos.inscripciones.aplicacion.InscripcionDuplicadaException;
import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;
import dev.learning.cursos.seguridad.IdentidadesAlumnos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    InscripcionesController(InscribirAlumno inscripciones, IdentidadesAlumnos identidades) {
        this.inscripciones = inscripciones;
        this.identidades = identidades;
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
        try {
            if (!inscripciones.inscribir(curso, new AlumnoId(alumnoVinculado))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cupo no disponible");
            }
        } catch (InscripcionDuplicadaException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alumno ya inscrito", ex);
        }
    }
}
