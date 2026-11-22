package dev.learning.cursos.inscripciones;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InscripcionesService {
    private final InscripcionesRepository repositorio;

    InscripcionesService(InscripcionesRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public boolean inscribir(long cursoId, long alumnoId) {
        if (!repositorio.consumirCupo(cursoId)) {
            return false;
        }
        repositorio.guardarInscripcion(cursoId, alumnoId);
        return true;
    }
}
