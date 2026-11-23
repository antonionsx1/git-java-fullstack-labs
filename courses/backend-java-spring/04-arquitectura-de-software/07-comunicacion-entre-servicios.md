# 07. Comunicación entre servicios

El [ADR 002](adr/002-extraccion-catalogo.md) mantiene el monolito modular. Esta lección explora cómo se comunicarían Catálogo e Inscripciones **si se aprobara la separación**, sin convertir el alta de un alumno en una llamada remota a Catálogo.

## Cuándo esperar una respuesta

| Opción | Cuándo elegirla | Qué ocurre ante un fallo |
| --- | --- | --- |
| Síncrona (HTTP/REST o gRPC) | El llamador necesita el resultado para continuar. Por ejemplo, un cliente consulta `GET /cursos` y espera el catálogo. | Un timeout no dice si el servidor ejecutó la operación. Antes de reintentar una escritura, define cómo evitar efectos duplicados. |
| Asíncrona (mensajería) | El llamador puede continuar tras guardar el cambio local y otro servicio puede reaccionar más tarde. Por ejemplo, publicar un curso y aprovisionar su cupo después. | El receptor puede estar caído o recibir el mismo mensaje más de una vez. Hay que decidir cuándo confirmar el mensaje y cómo procesarlo de nuevo sin duplicar efectos. |

Síncrono y asíncrono describen si el flujo espera el resultado, no un protocolo específico. Un HTTP 202, por ejemplo, puede aceptar trabajo para procesarlo después sin confirmar su resultado.

## Contrato propuesto: curso publicado

```json
{
  "eventoId": "6c63fa70-6d17-4aab-8c13-b29775e01fac",
  "tipo": "CursoPublicado",
  "version": 1,
  "cursoId": 42,
  "cupoInicial": 20
}
```

Catálogo anuncia el hecho y el cupo inicial acordado. Inscripciones es dueño del cupo restante. Mientras el mensaje esté pendiente, el curso puede estar visible sin admitir altas. Una vez aprovisionado, el POST de inscripción solo consulta datos locales y sigue funcionando si Catálogo cae. No uses una nueva publicación para corregir cupos: el ajuste requiere su propio contrato.

## Qué pasa si algo falla

1. **Catálogo confirma el curso, pero no logra publicar.** Guarda el cambio del curso y una fila de salida en la misma transacción local (*outbox*). Un proceso separado publica la fila pendiente y la marca como enviada tras la confirmación del intermediario.
2. **Se publicó el mensaje, pero el proceso cayó antes de marcarlo.** Puede volver a publicarlo con el mismo `eventoId`. Con entrega al menos una vez, el consumidor debe esperar duplicados.
3. **Inscripciones confirma el cambio, pero cae antes de confirmar el mensaje.** Al recibirlo de nuevo, registra o detecta el `eventoId` en la misma transacción local que el alta del cupo. Además, impide reinicializar un cupo existente por `cursoId`, incluso si una republicación llega con otro identificador. Confirma el mensaje después del commit.

El outbox evita perder el trabajo pendiente al confirmar la publicación del curso; no da entrega exactamente una vez. La idempotencia evita repetir el efecto de negocio. La transacción que descuenta el último cupo y registra al alumno sigue siendo local a Inscripciones.

## Ejercicio

1. Dibuja la secuencia desde la publicación del curso hasta el alta del cupo. Señala los commits, la confirmación del mensaje y el periodo en que el curso es visible pero todavía no admite inscripciones.
2. Define qué ocurre si el mensaje se retrasa, si llega dos veces y si el consumidor cae justo después de guardar. Para el duplicado, empieza con 20 cupos, registra un alumno y demuestra que el segundo mensaje debe dejar 19, no 20.
3. Documenta el contrato y las pruebas como propuesta. Explica qué parte debe seguir funcionando durante una caída de Catálogo y cuál queda pendiente.

**Entregable:** un diagrama de secuencia, el contrato del evento y una tabla de fallos con resultados esperados. No se necesita instalar un intermediario ni cambiar el [laboratorio](../../../labs/09-backend-cupos/README.md).
