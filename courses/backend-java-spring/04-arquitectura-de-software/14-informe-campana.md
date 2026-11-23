# Informe de arquitectura: campaña de inscripciones

**Decisión:** mantener el monolito modular y no activar gzip por defecto. La [guía actual](../../../labs/09-backend-cupos/ARQUITECTURA.md) muestra una sola aplicación Spring Boot con PostgreSQL. Inscripciones es dueño del cupo y mantiene el decremento y el registro en una transacción local. Se acepta compartir despliegue y capacidad a cambio de evitar otra aplicación y un contrato de aprovisionamiento entre servicios. Se mantiene el [ADR 002](adr/002-extraccion-catalogo.md).

## Evidencia funcional

Verificación local sobre la revisión base `91f0427`, con JDK 17.0.12 y PostgreSQL 17.12 en un contenedor temporal: `mvn -B -ntp clean verify` completó **15 pruebas, 0 fallos y 0 omitidas**. La base habitual no se usó.

| Criterio | Estado | Evidencia y alcance |
| --- | --- | --- |
| Integridad | Verificado | [Pruebas HTTP](../../../labs/09-backend-cupos/src/test/java/dev/learning/cursos/InscripcionesTest.java): ante dos altas y un cupo queda una sola inscripción. Un duplicado o un rechazo de escritura revierte el descuento. |
| Seguridad | Verificado en el laboratorio | La misma suite rechaza tokens inválidos, permisos insuficientes e identificadores ajenos sin consumir cupo. Las claves son efímeras; no se validó un emisor externo de campaña. |
| Diagnóstico | Verificado en el laboratorio | La misma suite distingue ambos HTTP 409, un 401 y el 503 de persistencia; comprueba métricas protegidas y la correlación entre respuesta y log. No valida un exportador de trazas. |
| Fallos operativos | Pendiente | La [matriz de resiliencia](08-solucion-matriz-de-fallos.md) describe, entre otros, la pérdida de respuesta después del commit. No se inyectó ese fallo ni se simuló la recuperación de un Catálogo separado. |
| Evolución | Verificado para el esquema anterior del laboratorio | Las [pruebas de migración](../../../labs/09-backend-cupos/src/test/java/dev/learning/cursos/MigracionesTest.java) cubren esquema vacío y adopción del esquema anterior. La fila histórica conserva la fecha nula y el cupo no se repone. |
| Capacidad de campaña | Pendiente | No se midieron 50 lecturas/s y 5 altas/s simultáneas. La extracción de Catálogo no aplica al despliegue actual. |

## Qué aporta el benchmark y qué falta

La [prueba local de Catálogo](../../../labs/09-backend-cupos/benchmark/README.md) usó 5 000 cursos y solo lecturas. A 50 solicitudes/s, en dos rondas cortas, ambas variantes tuvieron 100 % de respuestas exitosas; el p95 pasó de 20,83–21,60 ms sin compresión a 26,96–29,94 ms con gzip. El cuerpo se redujo un 76,3 %. A 100 solicitudes/s, una ronda sin compresión falló de forma marcada y la otra no. Es evidencia de ahorro de bytes, no de mejora de latencia ni de capacidad bajo tráfico mixto.

Para medir la campaña:

1. Preparar 5 000 cursos y un conjunto de alumnos de prueba con tokens válidos durante toda la ejecución. Restaurar los mismos datos antes de cada ronda. Aprovisionar cupos suficientes para la medición sostenida y ensayar el último cupo en una fase separada, para no convertir toda la carga de escritura en rechazos tempranos.
2. Usar un generador separado con tasas de llegada de 50 GET/s y 5 POST/s. Calentar dos minutos, medir cinco minutos y repetir tres veces. Documentar qué proporción de altas se concentra en un mismo curso y mantenerla al comparar cambios.
3. Registrar solicitudes ofrecidas y completadas, p95 por ruta, porcentaje de lecturas exitosas, 409 por resultado de negocio, 5xx, timeouts, CPU, espera del pool y bloqueos de PostgreSQL. Verificar que el descenso de cupos coincida con las inscripciones confirmadas. Las lecturas deben lograr p95 menor a 300 ms y al menos 99 % de éxito sobre la carga ofrecida; las escrituras deben mantener la integridad.

## Recomendación de cierre

No dar por aprobada la capacidad de campaña hasta ejecutar la prueba mixta. Si el objetivo falla, optimizar primero el recurso que muestran las mediciones y repetir con el mismo protocolo. Evaluar la extracción solo si aislar capacidad de Catálogo cumple el objetivo a un costo aceptable y se preservan el aprovisionamiento idempotente y la transacción local de Inscripciones.
