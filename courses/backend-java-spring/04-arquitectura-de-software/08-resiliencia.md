# 08. Resiliencia

Un sistema resiliente limita el impacto de un fallo, conserva sus reglas de negocio y se recupera cuando vuelve la dependencia. En el [diseño de extracción](adr/002-extraccion-catalogo.md), Inscripciones trabaja con datos locales: si Catálogo deja de responder, los cursos ya aprovisionados siguen admitiendo altas. El [laboratorio actual](../../../labs/09-backend-cupos/README.md) sigue siendo un monolito; aquí diseñaremos el comportamiento ante fallos sin dividirlo.

## Elegir la protección según el fallo

| Mecanismo | Qué protege | Aplicación a la API de cursos |
| --- | --- | --- |
| Tiempo de espera | Evita que una operación retenga recursos indefinidamente. | Limitar la espera de un cliente que consulta Catálogo, dentro del presupuesto total de la petición. |
| Reintento acotado | Permite recuperarse de un fallo transitorio. | Reintentar una lectura una vez si queda tiempo; usar espera creciente con variación aleatoria cuando haya varios reintentos. No reintentar un 409 de cupo agotado. |
| Circuit breaker | Evita repetir llamadas a una dependencia que falla de forma sostenida. | Tras superar un umbral de fallos, el cliente rechaza llamadas sin esperar; después permite pocas llamadas de prueba antes de volver a abrir el tráfico. |
| Aislamiento de recursos (*bulkhead*) | Impide que un flujo acapare los recursos de otro. | Limitar la concurrencia de las consultas al catálogo para reservar capacidad al alta. Un límite de concurrencia no elimina la contención si ambos flujos dependen del mismo recurso saturado. |
| Degradación controlada | Mantiene una función parcial sin prometer un resultado incorrecto. | Mostrar una copia reciente de descripciones si el negocio lo tolera. Nunca confirmar un cupo a partir de una copia en caché. |

## Diseñar el presupuesto y el resultado

Para una lectura con 500 ms de presupuesto, se podrían asignar hasta 180 ms al primer intento, 40 ms de espera y hasta 180 ms a un segundo intento. Quedan 100 ms para el resto del flujo. Cada intento debe respetar el plazo restante; no se inicia otro si ya no hay tiempo. Las cifras son ilustrativas: ajústalas con latencia medida y evita reintentos en múltiples capas para no multiplicar la carga.

Una escritura es distinta. Si el cliente agota su tiempo justo después del commit, el resultado es **desconocido para el cliente**, no una inscripción fallida. La unicidad curso-alumno protege el cupo y evita otra fila, pero un 409 en el segundo intento no reproduce el 201 original. Para reintentar de forma transparente, habría que diseñar un contrato de idempotencia que vincule la misma clave a la misma solicitud y a su resultado confirmado.

## Ejercicio: verificar la degradación

1. Define un presupuesto de latencia y decide si el cliente de Catálogo debe devolver un error acotado o servir descripciones en caché. Indica el máximo de intentos y qué errores no se reintentan.
2. Diseña cuatro pruebas: Catálogo lento, Catálogo caído, recuperación de Catálogo y respuesta del POST perdida después del commit. Para cada una, indica el fallo inyectado, la señal a medir y el estado esperado de los datos.
3. Explica cómo verificarías que la saturación de lecturas no bloquea las altas. Si PostgreSQL no permite confirmar una escritura, el POST no debe responder 201; si ya hizo commit pero se perdió la respuesta, debe quedar una sola inscripción y un solo descuento.

**Entregable:** una matriz de fallos con límites medibles y resultados esperados. No hace falta desplegar servicios ni instalar una biblioteca de resiliencia para esta lección.
