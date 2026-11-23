# 09. Escalabilidad y rendimiento

El rendimiento describe cómo responde el sistema bajo una carga definida. La escalabilidad describe qué ocurre al aumentar la carga y los recursos disponibles. Una API puede responder rápido con pocos clientes y saturarse al duplicar el tráfico. El [ADR 002](adr/002-extraccion-catalogo.md) propone medir antes de separar Catálogo: aquí diseñamos esa medición.

## Qué medir

- **Latencia:** tiempo observado por el cliente. Usa p50 para el caso típico y p95 o p99 para la cola. Un p95 de 300 ms indica que aproximadamente el 95 % de las observaciones no supera ese tiempo; no dice cuánto tardó la peor petición.
- **Carga y capacidad efectiva:** registra solicitudes ofrecidas y completadas por segundo. Cuenta éxitos, errores y timeouts por separado; una API que rechaza rápido puede tener baja latencia sin atender la carga.
- **Saturación:** mide CPU, memoria y pausas de JVM, espera por conexiones del pool, tiempos de consulta y bloqueos en PostgreSQL. Busca qué recurso se agota al subir la carga.

Separa las métricas por endpoint: mezclar lecturas rápidas con inscripciones lentas puede ocultar el problema. En escrituras, distingue un 409 esperado por cupo agotado de un fallo de infraestructura, pero registra ambos.

## Del síntoma al cambio

| Señal observada | Qué probar | Qué no asumir |
| --- | --- | --- |
| El tiempo de consulta o el tamaño de respuesta crece con el catálogo. | Revisar el plan de PostgreSQL, limitar campos y paginar. Si se cambia el contrato de lectura, comparar el antes y el después con una carga equivalente y documentar qué devuelve cada versión. | Que añadir un índice acelera una consulta que tiene que leer y devolver todos los cursos. |
| Se satura la CPU de la aplicación con margen en PostgreSQL. | Comparar escalado vertical y horizontal: más recursos por instancia frente a más instancias tras un balanceador. | Que duplicar instancias duplica la capacidad; cada instancia también aporta conexiones al mismo PostgreSQL. |
| Muchas lecturas repiten datos que cambian poco. | Medir una caché con un límite de antigüedad acordado y comparar aciertos, fallos y consumo. | Que una descripción en caché autoriza una inscripción. El cupo se decide en la base. |
| Las altas concurrentes esperan por la misma fila de cupo. | Comparar un curso muy solicitado con la misma carga repartida entre varios cursos. | Que añadir instancias elimina la contención de una fila compartida. No relajar la regla de no sobreinscripción para mejorar el p95. |

## Ejercicio: un experimento reproducible

1. Prepara una base de prueba con un volumen de cursos declarado. Documenta recursos, versiones, tamaño del pool y si la caché está fría o caliente. Define la mezcla de tráfico; empieza con `GET /cursos` aislado y usa después un escenario mixto para observar el impacto sobre las altas.
2. Calienta la aplicación durante dos minutos y mide cinco minutos a 25, 50 y 100 solicitudes por segundo ofrecidas, con cada nivel en una ejecución separada. Usa una tasa de llegada controlada, registra si el generador logra sostenerla y repite cada nivel tres veces. Para lecturas, usa como objetivo ilustrativo p95 menor a 300 ms a 50 solicitudes por segundo y al menos 99 % de respuestas exitosas sobre las solicitudes ofrecidas.
3. Escoge **un** cambio según el cuello observado. Repite con los mismos datos y carga, y compara p95, tasa de éxito y uso del recurso limitante. Si modificas el laboratorio, conserva aparte las pruebas de concurrencia y rollback con PostgreSQL.

**Entregable:** protocolo y tabla de antes y después con carga ofrecida, carga completada, p95, tasa de éxito y recurso limitante. Justifica si conviene optimizar, añadir capacidad o probar la extracción de Catálogo. No reportes mejoras sin mediciones.

**Práctica de referencia:** [comparación local de GET /cursos con y sin gzip](../../../labs/09-backend-cupos/benchmark/README.md).
