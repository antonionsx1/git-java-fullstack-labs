package dev.learning.cursos.inscripciones;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class InscripcionesController {
    private final InscripcionesService inscripciones;

    InscripcionesController(InscripcionesService inscripciones) {
        this.inscripciones = inscripciones;
    }

    @PostMapping("/cursos/{cursoId}/inscripciones")
    @ResponseStatus(HttpStatus.CREATED)
    void inscribir(@PathVariable long cursoId, @RequestParam long alumnoId) {
        if (cursoId <= 0 || alumnoId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            if (!inscripciones.inscribir(cursoId, alumnoId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cupo no disponible");
            }
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alumno ya inscrito", ex);
        }
    }
}
