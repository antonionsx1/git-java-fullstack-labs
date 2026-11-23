package dev.learning.cursos.inscripciones.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import dev.learning.cursos.inscripciones.dominio.AlumnoId;
import dev.learning.cursos.inscripciones.dominio.CursoId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InscribirAlumnoCasoDeUsoTest {
    @Test
    void conCupoGuardaLaInscripcion() {
        var persistencia = new PersistenciaDePrueba(true);

        assertThat(new InscribirAlumnoCasoDeUso(persistencia)
                .inscribir(new CursoId(1), new AlumnoId(101))).isTrue();
        assertThat(persistencia.operaciones).containsExactly("cupo:1", "guardar:1:101");
    }

    @Test
    void sinCupoNoIntentaGuardar() {
        var persistencia = new PersistenciaDePrueba(false);

        assertThat(new InscribirAlumnoCasoDeUso(persistencia)
                .inscribir(new CursoId(1), new AlumnoId(101))).isFalse();
        assertThat(persistencia.operaciones).containsExactly("cupo:1");
    }

    private static class PersistenciaDePrueba implements PersistenciaInscripciones {
        private final boolean hayCupo;
        private final List<String> operaciones = new ArrayList<>();

        PersistenciaDePrueba(boolean hayCupo) {
            this.hayCupo = hayCupo;
        }

        @Override
        public boolean consumirCupo(CursoId cursoId) {
            operaciones.add("cupo:" + cursoId.valor());
            return hayCupo;
        }

        @Override
        public void guardarInscripcion(CursoId cursoId, AlumnoId alumnoId) {
            operaciones.add("guardar:" + cursoId.valor() + ":" + alumnoId.valor());
        }
    }
}
