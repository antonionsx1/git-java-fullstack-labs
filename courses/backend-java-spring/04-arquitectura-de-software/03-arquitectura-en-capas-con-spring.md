# 03. Arquitectura en capas con Spring

En el [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md), **catálogo** e **inscripciones** son módulos funcionales: separan capacidades de negocio. Las **capas** separan responsabilidades dentro de una capacidad. Tener controladores, servicios y repositorios no implica crear tres despliegues ni renunciar a los módulos.

## Qué hace cada capa

| Capa | Responsabilidad | Ejemplo en inscripciones |
| --- | --- | --- |
| Controlador | Adaptar la solicitud HTTP, validar la entrada básica y traducir el resultado a una respuesta. | `InscripcionesController` recibe los identificadores, rechaza valores no positivos y responde con HTTP 201 o 409 para solicitudes válidas según el resultado. |
| Servicio | Ejecutar el caso de uso y decidir qué pasos deben completarse como una unidad. | `InscripcionesService` solicita consumir un cupo, registra al alumno y delimita la transacción con `@Transactional`. |
| Persistencia | Traducir las operaciones a consultas y aprovechar las restricciones de la base de datos. | `JdbcTemplate` ejecuta el decremento condicional y la inserción. En el laboratorio, el SQL aún está dentro del servicio. |

El flujo típico es **controlador → servicio → persistencia**. Un controlador no debería decidir por sí mismo si hay cupo. Y el hecho de que el servicio reciba una excepción de PostgreSQL traducida por Spring no significa que deba decidir cómo serializarla a HTTP.

## Dónde termina la transacción

El límite coincide con el caso de uso: **decrementar el cupo y guardar la inscripción**. Si esos pasos confirmaran en transacciones independientes, un fallo de inserción podría dejar un cupo perdido. En el laboratorio, el controlador invoca el servicio gestionado por Spring, y la excepción de una inserción duplicada sale de ese método: Spring revierte la transacción antes de que el controlador convierta la excepción en un HTTP 409.

## Cuándo extraer un repositorio

El laboratorio usa `JdbcTemplate` directamente en dos lugares: el servicio de inscripciones y el controlador de catálogo. Es un punto de partida, no una regla. Si el código de inscripciones empieza a mezclar reglas de negocio con más consultas, extrae operaciones con nombres del dominio, por ejemplo `consumirCupo` y `guardarInscripcion`. Mantén la transacción en el servicio, donde se coordinan ambas. En catálogo, una lectura simple no necesita un servicio que solo reenvíe la llamada si no hay un caso de uso adicional que coordinar.

## Ejercicio

1. Recorre `POST /cursos/1/inscripciones?alumnoId=101` y señala qué parte adapta HTTP, qué parte inicia el caso de uso y qué SQL impide la sobreinscripción.
2. Extrae la persistencia de inscripciones a una clase del mismo módulo, sin cambiar el contrato HTTP ni mover `@Transactional` fuera del servicio.
3. Ejecuta `mvn verify` con PostgreSQL levantado según el README del laboratorio. Deben seguir pasando las pruebas de concurrencia y rollback.

**Entregable:** un diff que separe el SQL sin cambiar el comportamiento, más una explicación de por qué la transacción sigue abarcando las dos operaciones.
