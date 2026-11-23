# 04. Arquitectura limpia y hexagonal

En la [lección anterior](03-arquitectura-en-capas-con-spring.md), el servicio de inscripciones coordina el caso de uso y el repositorio ejecuta el SQL. Esa separación ya es útil, pero el servicio todavía depende del repositorio concreto y de Spring. La pregunta de esta lección es qué ganamos si podemos ejecutar el mismo caso de uso sin importar Spring, HTTP ni JDBC.

## Dos ideas relacionadas

**Arquitectura limpia** pone el énfasis en la regla de dependencias: el código externo puede conocer al núcleo, pero el núcleo no conoce frameworks, formatos HTTP ni detalles de persistencia.

**Arquitectura hexagonal** pone el énfasis en cómo se comunica ese núcleo: los **puertos** describen las operaciones que ofrece o necesita la aplicación y los **adaptadores** conectan esas operaciones con tecnologías concretas. Son enfoques compatibles, no nombres intercambiables ni una obligación de crear una interfaz por clase.

## Puertos y adaptadores para inscripciones

| Pieza | Ejemplo |
| --- | --- |
| Puerto de entrada | `InscribirAlumno`: ejecuta el caso de uso con los identificadores del curso y del alumno, sin recibir una solicitud HTTP. |
| Adaptador de entrada | El controlador traduce el POST a una llamada al puerto y convierte el resultado en HTTP 201 o 409. |
| Núcleo | El caso de uso decide si guardar la inscripción según el resultado de consumir un cupo. |
| Puerto de salida | `PersistenciaInscripciones`: define `consumirCupo` y `guardarInscripcion` sin exponer JDBC ni excepciones de Spring. |
| Adaptador de salida | Una implementación con `JdbcTemplate` conserva el decremento condicional y la inserción en PostgreSQL. |

En ejecución, el caso de uso llama al adaptador JDBC **a través** del puerto de salida. En el código fuente, el adaptador importa e implementa la interfaz definida por la aplicación. La llamada sale del núcleo, pero la dependencia del código apunta hacia él.

## La transacción sigue siendo parte del contrato

La versión en capas del [laboratorio](../../../labs/09-backend-cupos/README.md) conservaba `@Transactional` en `InscripcionesService`. La solución hexagonal mueve esa anotación a `InscripcionTransaccional`, un decorador gestionado por Spring que implementa el puerto de entrada y envuelve **toda** la ejecución de `InscribirAlumnoCasoDeUso`. Poner una transacción independiente en cada operación del repositorio permitiría descontar un cupo aunque falle la inserción.

Si el adaptador JDBC traduce un duplicado a una excepción propia de la aplicación, deja que esa excepción no comprobada salga del decorador transaccional antes de convertirla en HTTP 409. Si se captura dentro del decorador y se devuelve un resultado normal, se pierde la garantía de rollback automático por esa excepción.

## Ejercicio

1. Introduce los puertos dentro de inscripciones y configura Spring para que el controlador invoque el caso de uso a través del decorador transaccional.
2. Agrega pruebas unitarias del caso de uso con un doble de persistencia: con cupo debe guardar la inscripción; sin cupo no debe intentar guardarla. No levantes Spring para estas pruebas.
3. Ejecuta las pruebas HTTP existentes contra PostgreSQL. Deben seguir confirmando una sola inscripción por el último cupo y devolver el cupo si falla el registro duplicado.

**Entregable:** un diff y una explicación de qué dependencias dejaron de entrar al núcleo. El doble de persistencia comprueba la coordinación del caso de uso; no sustituye las pruebas de concurrencia y transacción con PostgreSQL.

**Tradeoff:** el aislamiento aporta valor cuando hay reglas que probar sin infraestructura o adaptadores que pueden cambiar. Para una lectura simple como el catálogo actual, agregar puertos sin una necesidad concreta puede aumentar el código sin mejorar la separación.
