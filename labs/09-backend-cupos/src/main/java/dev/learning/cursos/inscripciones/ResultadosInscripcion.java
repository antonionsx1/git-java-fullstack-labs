package dev.learning.cursos.inscripciones;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class ResultadosInscripcion {
    private static final Logger LOG = LoggerFactory.getLogger(ResultadosInscripcion.class);
    private final MeterRegistry metricas;

    ResultadosInscripcion(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    enum Resultado {
        CONFIRMADA("confirmada", 201),
        CUPO_AGOTADO("cupo_agotado", 409),
        DUPLICADA("duplicada", 409),
        FALLO_PERSISTENCIA("fallo_persistencia", 503);

        private final String codigo;
        private final int estado;

        Resultado(String codigo, int estado) {
            this.codigo = codigo;
            this.estado = estado;
        }
    }

    void registrar(Resultado resultado, long inicio) {
        metricas.counter("cursos.inscripciones", "resultado", resultado.codigo).increment();
        var evento = resultado == Resultado.FALLO_PERSISTENCIA ? LOG.atWarn() : LOG.atInfo();
        evento.addKeyValue("ruta", "/cursos/{cursoId}/inscripciones")
                .addKeyValue("resultado", resultado.codigo)
                .addKeyValue("estado", resultado.estado)
                .addKeyValue("duracionMs", (System.nanoTime() - inicio) / 1_000_000)
                .log("resultado_inscripcion");
    }
}
