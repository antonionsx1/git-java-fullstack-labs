package dev.learning.cursos.inscripciones.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InscribirAlumnoCasoDeUsoTest {
    @Test
    void conCupoGuardaLaInscripcion() {
        var persistencia = new PersistenciaDePrueba(true);

        assertThat(new InscribirAlumnoCasoDeUso(persistencia).inscribir(1, 101)).isTrue();
        assertThat(persistencia.operaciones).containsExactly("cupo:1", "guardar:1:101");
    }

    @Test
    void sinCupoNoIntentaGuardar() {
        var persistencia = new PersistenciaDePrueba(false);

        assertThat(new InscribirAlumnoCasoDeUso(persistencia).inscribir(1, 101)).isFalse();
        assertThat(persistencia.operaciones).containsExactly("cupo:1");
    }

    private static class PersistenciaDePrueba implements PersistenciaInscripciones {
        private final boolean hayCupo;
        private final List<String> operaciones = new ArrayList<>();

        PersistenciaDePrueba(boolean hayCupo) {
            this.hayCupo = hayCupo;
        }

        @Override
        public boolean consumirCupo(long cursoId) {
            operaciones.add("cupo:" + cursoId);
            return hayCupo;
        }

        @Override
        public void guardarInscripcion(long cursoId, long alumnoId) {
            operaciones.add("guardar:" + cursoId + ":" + alumnoId);
        }
    }
}
