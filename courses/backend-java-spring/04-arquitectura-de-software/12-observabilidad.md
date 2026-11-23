# 12. Observabilidad

Dos solicitudes al [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) pueden devolver HTTP 409 por razones distintas: cupo agotado o alumno ya inscrito. Contar los 409 no basta para distinguirlas. La observabilidad consiste en poder explicar qué ocurrió a partir de las señales del sistema, sin exponer las credenciales ni los datos de quienes lo usan.

## Elegir la señal según la pregunta

| Señal | Pregunta | Ejemplo |
| --- | --- | --- |
| Métricas | ¿Cuántas veces ocurre y desde cuándo? | Tasa de peticiones, p95 de latencia y tasa de 5xx por ruta normalizada. Contador de resultados de inscripción para distinguir los dos tipos de 409. |
| Logs estructurados | ¿Qué pasó en esta solicitud? | Un evento con `requestId`, ruta normalizada, estado, duración y código estable de resultado, por ejemplo `cupo_agotado`. |
| Trazas | ¿En qué parte del recorrido se gastó el tiempo? | Un tramo HTTP que enlaza con otro para el trabajo de Inscripciones y, si se instrumenta, con el acceso a PostgreSQL. Un ID de correlación ayuda a buscar logs, pero por sí solo no crea una traza. |

Para detectar saturación, compara el tiempo de respuesta con la espera por conexiones del pool. Un p95 alto en HTTP no demuestra que PostgreSQL sea el cuello de botella. Una traza lenta permite investigar un caso; las métricas indican si es representativo. La práctica de [rendimiento](09-escalabilidad-y-rendimiento.md) dejó esa causa abierta porque no midió recursos del servidor.

## Instrumentar sin generar otro problema

- Usa etiquetas acotadas: `ruta=/cursos/{cursoId}/inscripciones` y un conjunto fijo de resultados como `confirmada`, `cupo_agotado` y `duplicada`. No etiquetes métricas con IDs de cursos, alumnos, peticiones o con mensajes de error arbitrarios.
- Registra el resultado de negocio después de que la llamada transaccional retorna o falla. Un alta aún sin commit no se cuenta como confirmada. Los 401 y 403 rechazados antes del controlador siguen visibles en las métricas HTTP, sin inventar un resultado de inscripción.
- Correlaciona con un ID generado por el servidor y devuélvelo al cliente. No registres tokens, cabeceras completas, parámetros libres, identidades de alumnos ni valores SQL. Protege los endpoints de gestión; un catálogo público no implica métricas públicas.
- Define el denominador de cada indicador. Por ejemplo, `5xx / solicitudes del POST` mide errores del servidor; `409 / intentos autorizados procesados` responde otra pregunta. Pon una ventana y un volumen mínimo antes de alertar para que un error aislado no se interprete como una tendencia.

## Ejercicio: seguir una inscripción de extremo a extremo

1. Añade métricas HTTP con Actuator y Micrometer, y un contador de resultados de inscripción con etiquetas acotadas. Define qué credencial puede consultar métricas sin concederle permiso para inscribir.
2. Añade un registro estructurado con ID de correlación y código de resultado. Envía una inscripción exitosa, un duplicado, una solicitud sin credenciales y un intento sin cupo. Comprueba qué registros y contadores cambian y que solo los éxitos corresponden a registros confirmados en la base.
3. Diseña el tramo HTTP → Inscripciones → PostgreSQL. Si no hay un exportador instalado, documenta qué tramos esperas observar; no presentes un diagrama como evidencia de una traza capturada. Define también qué señal diferenciaría un conflicto de negocio de un fallo de PostgreSQL.

**Entregable:** evidencia de métricas y logs con datos de prueba, un esquema de traza y una nota de diagnóstico que explique cómo distinguir los resultados.

## Solución de referencia

La [práctica del laboratorio](../../../labs/09-backend-cupos/README.md#consultar-señales) comprueba que los 401 no incrementan el contador de negocio, que un duplicado y un cupo agotado tienen códigos distintos a pesar de compartir HTTP 409, y que un fallo de PostgreSQL durante la inserción devuelve 503 y revierte el decremento.

El recorrido propuesto para una futura traza es `POST /cursos/{cursoId}/inscripciones → caso de uso transaccional → UPDATE cupo e INSERT inscripcion`. El tramo de aplicación debería cubrir el commit o rollback y los tramos SQL no deberían capturar parámetros. Hoy se verifica el ID de correlación en el log; no se configuró un exportador ni se capturó esa traza.
