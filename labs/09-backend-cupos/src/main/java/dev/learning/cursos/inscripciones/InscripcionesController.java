package dev.learning.cursos.inscripciones;

import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;

import dev.learning.cursos.inscripciones.aplicacion.InscribirAlumno;
import dev.learning.cursos.inscripciones.aplicacion.InscripcionDuplicadaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class InscripcionesController {
    private final InscribirAlumno inscripciones;

    InscripcionesController(InscribirAlumno inscripciones) {
        this.inscripciones = inscripciones;
    }

    @PostMapping("/cursos/{cursoId}/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    void inscribir(@PathVariable long cursoId, @RequestParam long alumnoId) {
        CursoId curso;
        AlumnoId alumno;
        try {
            curso = new CursoId(cursoId);
            alumno = new AlumnoId(alumnoId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, ex);
        }
        try {
            if (!inscripciones.inscribir(curso, alumno)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cupo no disponible");
            }
        } catch (InscripcionDuplicadaException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alumno ya inscrito", ex);
        }
    }
}
