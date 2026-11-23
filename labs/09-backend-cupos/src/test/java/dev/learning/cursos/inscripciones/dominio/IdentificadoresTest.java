package dev.learning.cursos.inscripciones.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdentificadoresTest {
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void rechazanValoresNoPositivos(long valor) {
        assertThatIllegalArgumentException().isThrownBy(() -> new CursoId(valor));
        assertThatIllegalArgumentException().isThrownBy(() -> new AlumnoId(valor));
    }

    @Test
    void laIgualdadDependeDelValorYDelTipo() {
        assertThat(new CursoId(7)).isEqualTo(new CursoId(7)).isNotEqualTo(new CursoId(8));
        assertThat(new AlumnoId(7)).isEqualTo(new AlumnoId(7)).isNotEqualTo(new CursoId(7));
    }
}
