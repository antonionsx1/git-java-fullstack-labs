# 06. Microservicios

En la [lección de DDD](05-diseno-orientado-al-dominio.md) distinguimos Catálogo de Inscripciones. Un límite de dominio ayuda a encontrar un candidato a servicio, pero no obliga a desplegarlo por separado. Aquí evaluaremos qué cambiaría si extrajéramos Catálogo del [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md).

## Qué cambia al separar un servicio

Un microservicio se puede desplegar de forma independiente y ofrece un contrato explícito a sus consumidores. Dos procesos que tienen que actualizarse siempre juntos o que escriben en las mismas tablas siguen acoplados, aunque se comuniquen por HTTP.

| Aspecto | Monolito modular actual | Si se extrae Catálogo |
| --- | --- | --- |
| Despliegue | Una aplicación Spring Boot. | Dos aplicaciones que pueden publicarse y revertirse por separado. |
| Datos | Los módulos comparten PostgreSQL y el esquema contiene una relación entre ambos. | Cada servicio controla el acceso a sus datos y puede evolucionar su esquema sin que el otro consulte sus tablas. |
| Operación | Se monitorea un despliegue. | Hay que observar y operar ambos servicios, incluidos los fallos parciales. |

El [ADR 001](adr/001-monolito-modular.md) mantiene el monolito mientras no haya evidencia de que separar el catálogo resuelva un problema concreto. Si el cuello de botella es una consulta, dividir el despliegue no la vuelve más rápida. Si el catálogo necesita capacidad propia aun después de optimizarlo, la extracción puede merecer una prueba.

## Extraer Catálogo sin partir la transacción

Catálogo sería dueño de `curso` y de `GET /cursos`. Inscripciones conservaría `cupo`, `inscripcion` y el endpoint de alta. En el esquema actual, `cupo.curso_id` referencia `curso.id`; esa clave foránea no puede mantenerse entre bases independientes. Si se elimina, hay que reemplazar explícitamente lo que garantizaba: el alta local de un cupo debe partir de un curso válido.

Para una primera versión, Inscripciones podría aprovisionar el cupo a partir de una publicación confirmada por Catálogo. El aprovisionamiento debe ser idempotente: procesar dos veces el mismo curso no debe reponer lugares consumidos. Hasta que exista un cupo local, Inscripciones no confirma solicitudes. Si se necesitara cerrar inscripciones al retirar un curso, ese cambio requeriría un contrato explícito; ocultar un curso del catálogo no basta.

La inscripción de un curso ya aprovisionado no necesita consultar Catálogo en cada petición. El decremento condicional y el registro del alumno siguen en la **misma transacción local de Inscripciones**. Así, una caída de Catálogo no impide registrar a un alumno en un curso habilitado y la garantía del último cupo no depende de la red.

## Ejercicio: diseñar la extracción

1. Dibuja los dos despliegues. Asigna los endpoints y las tablas a su dueño, y describe el contrato de aprovisionamiento del cupo.
2. Explica cómo migrarías los cursos y verificarías que todos los cupos existentes siguen referenciando cursos válidos antes de retirar la clave foránea. Define cómo evitar escrituras de Catálogo en dos sitios durante el cambio y qué condición te haría revertirlo.
3. Escribe los criterios de aceptación: actualizar Catálogo sin redesplegar Inscripciones, inscribir en un curso habilitado con Catálogo fuera de servicio, no reponer cupos por aprovisionamiento duplicado y conservar las pruebas de concurrencia y rollback.

**Entregable:** un diagrama y un ADR breve que recomiende mantener el monolito o extraer Catálogo, con la evidencia que justificaría esa decisión. No se requiere dividir el laboratorio para esta lección.

**Solución de referencia:** [ADR 002: mantener el monolito y evaluar la extracción de Catálogo](adr/002-extraccion-catalogo.md).
