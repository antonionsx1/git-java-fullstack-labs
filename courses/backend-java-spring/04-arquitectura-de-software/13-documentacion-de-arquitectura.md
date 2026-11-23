# 13. Documentación de arquitectura

Una persona que llega al [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) debe poder responder tres preguntas: qué existe hoy, por qué se tomó una decisión y cómo comprobar que una garantía se conserva. Un diagrama, un ADR y una prueba responden preguntas distintas.

## Elige la vista según la pregunta

El [modelo C4](https://c4model.com/diagrams) propone cuatro niveles de detalle: contexto, contenedores, componentes y código. No es necesario dibujarlos todos. Para este laboratorio, las dos primeras vistas bastan para orientar a una persona nueva; una secuencia ayuda a explicar la transacción.

| Vista | Qué mostrar en la API de cursos | Qué no confundir |
| --- | --- | --- |
| Contexto | Alumno y operador, API de cursos y emisor externo de tokens, cuando está configurado. | El emisor no forma parte del laboratorio ni se configura por defecto. |
| Contenedores | Una aplicación Spring Boot y una base PostgreSQL. La aplicación sirve Catálogo e Inscripciones y accede a la base por JDBC. | Un contenedor en C4 no implica Docker. Catálogo e Inscripciones no son dos aplicaciones desplegables. |
| Secuencia | POST autenticado, decremento condicional, inserción, commit y HTTP 201. Mostrar la ruta alternativa de rollback si falla la inserción. | Una secuencia documenta el comportamiento esperado; no sustituye una prueba ni es una traza capturada. |

Etiqueta cada relación con lo que atraviesa el límite (por ejemplo, HTTP con Bearer o SQL vía JDBC). Marca las vistas como **actual** o **propuesta**. No dibujes un intermediario de mensajes como parte del sistema actual: pertenece a la [propuesta de comunicación](07-comunicacion-entre-servicios.md).

## Registra el porqué en un ADR

Un [ADR](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) registra una decisión significativa y su contexto. Puedes usar un título y un estado claros, describir las alternativas y sus consecuencias, y añadir evidencia y condiciones de revisión. Si la decisión se reemplaza, conserva el registro anterior y enlaza su sucesor.

En este repositorio, el [ADR 001](adr/001-monolito-modular.md) explica por qué se eligió un monolito modular. El [ADR 002](adr/002-extraccion-catalogo.md) conserva esa decisión y define cuándo evaluar la extracción de Catálogo. Describir cómo se extraería no significa que exista un segundo despliegue.

## Vincula las garantías con su evidencia

| Afirmación | Evidencia verificable |
| --- | --- |
| Solo se confirma una inscripción por el último cupo. | La prueba de concurrencia envía dos POST y comprueba un 201, un 409 y una sola fila. |
| Una identidad sin permiso no puede inscribir. | Una prueba HTTP recibe 403 y comprueba que el cupo no cambia. |
| Se puede correlacionar una inscripción con el log. | La prueba compara `X-Request-Id` con el ID registrado. No afirma que se exporten trazas. |

Enlaza las pruebas, el código o la guía de arranque, en lugar de repetir instrucciones en cada diagrama. En el PR que cambie un contrato, un límite transaccional o un despliegue, actualiza las vistas afectadas. Mantén los objetivos no medidos y las propuestas en estado explícito.

## Ejercicio: guía de arquitectura del último cupo

1. Dibuja las vistas de contexto y contenedores del sistema actual. Incluye límites, relaciones y la condición bajo la cual se usa un emisor externo.
2. Añade una secuencia con el caso exitoso y el rollback. Enlaza los ADR vigentes en vez de reescribirlos, e indica qué partes de la extracción no existen todavía.
3. Enlaza al menos tres garantías con pruebas del laboratorio. Pide a otra persona que encuentre dónde se valida la identidad, dónde se confirma el alta y cómo consultar las métricas sin adivinar.

**Entregable:** una guía breve y versionada junto al laboratorio, con diagramas y enlaces a evidencia. Esta lección no modifica la API.

**Solución de referencia:** [Guía de arquitectura del último cupo](../../../labs/09-backend-cupos/ARQUITECTURA.md).
