package dev.learning.cursos.inscripciones;

import dev.learning.cursos.inscripciones.aplicacion.InscribirAlumno;
import dev.learning.cursos.inscripciones.aplicacion.InscribirAlumnoCasoDeUso;
import dev.learning.cursos.inscripciones.aplicacion.PersistenciaInscripciones;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InscripcionTransaccional implements InscribirAlumno {
    private final InscribirAlumno casoDeUso;

    InscripcionTransaccional(PersistenciaInscripciones persistencia) {
        this.casoDeUso = new InscribirAlumnoCasoDeUso(persistencia);
    }

    @Override
    @Transactional
    public boolean inscribir(long cursoId, long alumnoId) {
        return casoDeUso.inscribir(cursoId, alumnoId);
    }
}
