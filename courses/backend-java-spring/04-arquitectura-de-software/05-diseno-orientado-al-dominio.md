# 05. Diseño orientado al dominio (DDD)

En la [lección anterior](04-arquitectura-limpia-y-hexagonal.md) aislamos el caso de uso de Spring y JDBC. DDD plantea otra pregunta: **¿qué reglas del negocio debe expresar ese caso de uso y en qué parte del sistema tienen sentido?** Puedes responderla dentro del mismo monolito; DDD no exige microservicios.

## Diseño estratégico: lenguaje y límites

El **lenguaje ubicuo** es el vocabulario que acuerdan quienes conocen el negocio y quienes escriben el software. Úsalo en conversaciones, código y pruebas. Para el [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) podemos acordar:

| Término | Significado |
| --- | --- |
| Curso publicado | Entrada visible en el catálogo. Que se pueda consultar no implica que queden cupos. |
| Cupo disponible | Lugar que Inscripciones aún puede asignar. |
| Inscripción confirmada | Registro de un alumno en un curso después de consumir un cupo. |

Un **contexto delimitado** fija dónde se aplica un modelo y qué significan sus términos. En este ejemplo, **Catálogo** describe cursos e **Inscripciones** decide quién puede ocupar un lugar. Inscripciones usa el identificador del curso como referencia; no necesita copiar su título ni usar el modelo de Catálogo para decidir si hay un cupo. Son límites propuestos para este caso: tener dos paquetes o dos tablas no demuestra por sí solo que existan dos contextos.

## Diseño táctico: identidad, valores e invariantes

- **Entidad:** importa su identidad a través del tiempo. En este laboratorio se identifica una inscripción por el par curso-alumno.
- **Objeto de valor:** importa lo que representa, no una identidad propia. Un `CursoId` puede ser un valor inmutable que rechace identificadores no positivos y evite confundirlos con un `AlumnoId`. Los tipos aportan claridad aunque ambos se persistan como números.
- **Agregado:** agrupa el estado que debe protegerse bajo un mismo límite de consistencia y tiene una raíz que gobierna sus cambios. Un modelo candidato es `CupoDelCurso`, identificado por el curso, que controla la asignación de lugares. Eso no obliga a cargar todas las inscripciones ni a representar cada tabla como un agregado.

Una **invariante** es una regla que debe seguir siendo cierta tras cada operación confirmada. Aquí el cupo no puede quedar negativo y no puede haber dos inscripciones para el mismo alumno en el mismo curso. El laboratorio actual protege esas reglas mediante el decremento condicional y la unicidad en PostgreSQL; la transacción une el decremento con la inserción. Un modelo Java puede expresar intención y validar entradas, pero no sustituye esas garantías cuando llegan solicitudes simultáneas.

## Ejercicio

1. Define un glosario breve y dibuja los límites de Catálogo e Inscripciones. Identifica quién es dueño del cupo y qué referencia cruza el límite.
2. Introduce `CursoId` y `AlumnoId` dentro de Inscripciones. Convierte los números en el borde HTTP, conserva la respuesta 400 para valores inválidos y evita llevar tipos HTTP o JDBC al caso de uso.
3. Añade pruebas unitarias de los valores y ejecuta `mvn verify` contra PostgreSQL. Deben conservarse la inscripción única bajo concurrencia y la devolución del cupo cuando falla un duplicado.

**Entregable:** glosario, mapa de contextos y un diff que muestre qué error evita el modelo Java y qué regla sigue protegiendo PostgreSQL. No se requiere crear otro servicio ni cambiar el esquema.

## Solución de referencia

```text
Catálogo                         Inscripciones
Curso publicado                  Cupo del curso e inscripción
                  cursoId  --->  Referencia al curso, sin copiar su modelo
```

En el laboratorio, el controlador construye `CursoId` y `AlumnoId` y convierte un valor no positivo en HTTP 400. Los puertos usan los tipos del dominio, y el adaptador JDBC recupera el número solo al preparar el SQL. El compilador impide intercambiar ambos identificadores; PostgreSQL y la transacción siguen garantizando que una inscripción no duplique el registro ni consuma un cupo adicional.
