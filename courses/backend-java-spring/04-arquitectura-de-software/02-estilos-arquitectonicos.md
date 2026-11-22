# 02. Estilos arquitectónicos

En [Fundamentos de arquitectura](01-fundamentos.md) planteamos una API de cursos: un equipo pequeño mantiene el catálogo y las inscripciones. Durante una campaña aumentan las consultas, pero no se debe asignar el último cupo a dos alumnos. Ahora toca elegir una estructura inicial y explicar qué evidencia justificaría cambiarla.

## Tres opciones para la misma API

| Estilo | Cómo se organizaría en Spring Boot | Ventaja | Costo |
| --- | --- | --- | --- |
| Monolito tradicional | Una aplicación organizada principalmente por capas técnicas: controladores, servicios y repositorios. | Un despliegue y transacciones locales; poco trabajo operativo inicial. | Un cambio de negocio puede recorrer varias capas y tocar componentes compartidos; los límites funcionales no quedan implícitamente protegidos. |
| Monolito modular | Una aplicación con módulos de catálogo e inscripciones; cada módulo expone una interfaz y mantiene privados sus detalles. | Límites de negocio claros con un solo despliegue y sin llamadas de red entre módulos. | Los módulos se despliegan y se escalan juntos; hay que vigilar que no accedan a los repositorios internos del otro. |
| Microservicios | Catálogo e inscripciones son aplicaciones desplegables por separado y cada una controla sus datos. | Cada servicio puede desplegarse y escalar de forma independiente si se respetan sus contratos. | Aparecen fallos de red, compatibilidad de contratos y más trabajo de observabilidad y operación. |

Un monolito puede ejecutarse en varias instancias. La diferencia es la **unidad de despliegue**, no la cantidad de instancias. Y separar paquetes por funcionalidad no basta para lograr modularidad: importa qué dependencias se permiten entre ellos.

## Decisión inicial: monolito modular

- **Catálogo** publica la descripción de los cursos. Puede usar caché si el negocio tolera descripciones temporalmente desactualizadas.
- **Inscripciones** es dueño del cupo y decide si una solicitud se confirma. El contador no se modifica desde catálogo ni se toma de la caché para autorizar una inscripción.
- **Concurrencia:** en una transacción de PostgreSQL, inscripciones decrementa el cupo solo si es mayor que cero. Si se actualiza una fila, registra la inscripción en esa misma transacción; si no, responde que no hay cupo. Si falla el registro, también se revierte el decremento.

La prueba clave inicia con un cupo y envía dos solicitudes concurrentes de alumnos distintos: debe confirmarse exactamente una inscripción y el cupo final debe ser cero. La prueba necesita la base real o una instancia de prueba de PostgreSQL, no un repositorio simulado.

## Cuándo reconsiderarla

Mide la latencia del catálogo durante una campaña y localiza el cuello de botella. Si el catálogo incumple de manera sostenida su objetivo (por ejemplo, p95 inferior a 300 ms bajo la carga acordada), evalúa primero consultas, caché y capacidad. Si el cuello está en un recurso compartido y escalar solo el catálogo resuelve el problema a un costo aceptable, considera extraerlo. Separar servicios no arregla por sí solo una consulta lenta ni elimina la necesidad de proteger el cupo.

## Entregable: ADR

Completa un registro breve de decisión arquitectónica (ADR). Usa la decisión anterior como ejemplo o defiende una alternativa con evidencia.

| Campo | Qué documentar |
| --- | --- |
| Contexto | Equipo, carga esperada y regla de no sobreinscripción. |
| Decisión | Estilo elegido, unidades de despliegue y dueño del cupo. |
| Alternativas | Por qué las otras dos opciones no son la mejor elección en este escenario. |
| Consecuencias | Qué se gana y qué costo operativo o técnico se acepta. |
| Verificación | Resultado esperado de la prueba concurrente y objetivo de latencia con una carga definida. |
| Revisión | Qué señal medible justificaría reconsiderar la decisión. |

El ADR es el entregable de esta lección. Después de redactar el tuyo, compáralo con el [ADR de ejemplo](adr/001-monolito-modular.md) y comprueba la decisión en el [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md).
