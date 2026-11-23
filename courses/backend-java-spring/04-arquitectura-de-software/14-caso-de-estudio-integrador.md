# 14. Caso de estudio integrador: campaña de inscripciones

Un equipo pequeño prepara una campaña para un catálogo de 5 000 cursos. Durante la apertura, muchas personas consultarán la oferta y varias intentarán inscribirse en el mismo curso. Tu tarea no es diseñar el sistema más complejo, sino recomendar **qué arquitectura conservar, qué riesgos atender y qué evidencia falta antes de comprometer capacidad**.

## Punto de partida

El [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) implementa un monolito modular con Spring Boot y PostgreSQL. Catálogo publica los cursos; Inscripciones controla el cupo y el registro de alumnos. El catálogo es público y el alta exige una identidad verificada. La [guía de arquitectura](../../../labs/09-backend-cupos/ARQUITECTURA.md) documenta el sistema actual y enlaza las pruebas.

Los [ADR vigentes](adr/002-extraccion-catalogo.md) conservan el monolito y dejan la extracción de Catálogo como opción. El [benchmark local](../../../labs/09-backend-cupos/benchmark/README.md) solo midió lecturas breves y comparó compresión: es una referencia, no una validación de capacidad bajo tráfico mixto.

## Criterios para la campaña

| Criterio | Qué debe comprobar la revisión |
| --- | --- |
| Integridad | Con un cupo y dos alumnos autorizados, exactamente uno se inscribe. Si falla la inserción, se revierte el descuento. |
| Seguridad | El POST obtiene el alumno de la identidad autenticada. Un rechazo no consume cupo y el permiso de métricas no permite inscribir. |
| Rendimiento | Como objetivo de ejercicio, sostener 50 lecturas/s con p95 menor a 300 ms y al menos 99 % de respuestas exitosas, mientras llegan 5 intentos de alta/s. Declara qué datos, duración y mezcla usas; no cuentes un 409 de negocio como caída de infraestructura. |
| Fallos y diagnóstico | Distinguir cupo agotado, duplicado, rechazo de seguridad y error de persistencia. Si se pierde la respuesta después del commit, el cliente no debe asumir que el alta falló. |
| Evolución | Una base existente se puede actualizar conservando sus inscripciones y fechas desconocidas. La propuesta de extraer Catálogo debe separar datos y mantener la transacción de Inscripciones local. |

Las cifras son criterios del caso, no resultados actuales. Si Catálogo e Inscripciones se despliegan juntos, no afirmes que uno puede sobrevivir a la caída de todo el proceso o de la base compartida.

## Revisión final

1. **Describe el sistema actual.** Usa la guía y señala dónde se aplica la autorización, quién escribe el cupo y qué ocurre al terminar la transacción. Distingue el diseño actual de la separación propuesta.
2. **Ejecuta la verificación funcional.** Corre `mvn verify` con una base de prueba. Relaciona cada garantía con el resultado de la prueba correspondiente. Separa de esa evidencia los fallos que solo están descritos en la [matriz de resiliencia](08-solucion-matriz-de-fallos.md).
3. **Compara capacidad con el objetivo.** Presenta el benchmark existente con sus límites y diseña o ejecuta una prueba mixta reproducible. Mide latencia por ruta, carga ofrecida y completada, errores y saturación. Si no ejecutas la prueba, deja el objetivo como pendiente; no extrapoles desde la prueba de lecturas.
4. **Emite una decisión.** Recomienda mantener el monolito, optimizarlo o evaluar la extracción de Catálogo. Registra el costo aceptado, la evidencia que falta y la señal que revisaría la decisión. Si propones separar los servicios, explica cómo aprovisionar el cupo sin depender de Catálogo en cada POST ni reponer lugares al repetir un evento.

**Entregable:** informe breve con decisión, estado de cada criterio (verificado, pendiente o no aplicable), enlaces a la evidencia y próximos pasos. El caso no exige crear otro servicio ni cambiar la API solo para demostrar una técnica.
