# 10. Arquitectura de datos

La arquitectura de datos define quién puede cambiar un dato, qué garantías deben conservarse y cómo evoluciona el esquema sin perderlas. El [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) usa PostgreSQL porque una inscripción exige actualizar dos datos como una sola operación.

## Propiedad y fuentes de verdad

| Dato | Dueño | Regla |
| --- | --- | --- |
| `curso` | Catálogo | Publica el título y los datos que se muestran al usuario. No autoriza inscripciones. |
| `cupo` | Inscripciones | Mantiene los lugares disponibles. Es la fuente de verdad para aceptar nuevas inscripciones. |
| `inscripcion` | Inscripciones | Guarda el registro por curso y alumno. No se deduce de una copia del catálogo. |

Que ambos módulos usen la misma base no permite que cualquiera escriba cualquier tabla. El dueño decide cómo cambia el dato. Si Catálogo se extrae, las referencias entre bases pasarían a un contrato explícito, como describe el [ADR 002](adr/002-extraccion-catalogo.md). Una copia asíncrona puede servir para mostrar datos, pero no para decidir si queda el último lugar.

## Qué protege cada mecanismo

- **Restricciones:** la clave compuesta de `inscripcion` impide duplicar el par curso-alumno. Las claves foráneas evitan registros sin su referencia local y `CHECK (disponibles >= 0)` rechaza valores negativos. Ninguna de ellas, por sí sola, comprueba que cada alta haya consumido un cupo.
- **Actualización condicional:** ante dos solicitudes concurrentes, el `UPDATE ... WHERE disponibles > 0` decide qué transacción puede consumir el lugar. En el aislamiento habitual de PostgreSQL, `READ COMMITTED`, una actualización que espera por otra sobre la misma fila vuelve a evaluar la condición tras el commit de la primera.
- **Transacción:** el decremento y la inserción se confirman juntos. Si la inserción falla por duplicado, el decremento se revierte. Leer el contador y luego restar sin una condición no ofrece la misma garantía.
- **Índices:** la clave de `inscripcion` comienza por `curso_id` y es útil para consultas por curso. Una consulta frecuente por `alumno_id` puede requerir un índice distinto; compruébalo con el plan de ejecución y considera el costo de mantenerlo en cada escritura.

## Evolución del esquema

El `schema.sql` inicializa el laboratorio; `CREATE TABLE IF NOT EXISTS` no actualiza una tabla ya creada. En una aplicación con datos persistentes, registra cambios en migraciones versionadas y comprueba tanto una instalación nueva como la actualización desde el esquema anterior.

Para añadir una columna obligatoria a una tabla en uso, empieza con un cambio compatible, despliega los escritores que ya llenan el campo, completa y verifica las filas previas y solo después aplica la restricción. No inventes valores históricos: si no se conoce la fecha de una inscripción, no la marques como confirmada hoy para satisfacer un `NOT NULL`.

## Ejercicio: introducir migraciones versionadas

1. Diseña una migración inicial que reproduzca el esquema actual. Define cómo identificar una base existente que coincida con ese esquema antes de marcar su versión inicial, sin eliminar filas ni volver a ejecutar la creación de tablas. Si el esquema no coincide, detén la actualización y resuelve la diferencia.
2. Define una segunda migración que agregue `confirmada_en` como `TIMESTAMPTZ` anulable. Conserva `NULL` cuando se desconozca la fecha histórica y haz que las nuevas altas registren su fecha en la misma transacción. Decide qué antiguos escritores pueden coexistir durante el despliegue.
3. Prueba ambos caminos: base vacía y base con una inscripción anterior. Comprueba que migrar no duplica ni pierde filas, que una alta nueva tiene fecha y que siguen pasando las pruebas de concurrencia y rollback.

**Entregable:** migraciones, plan de adopción y evidencia de los dos caminos de actualización. Esta lección no modifica aún el esquema del laboratorio.
