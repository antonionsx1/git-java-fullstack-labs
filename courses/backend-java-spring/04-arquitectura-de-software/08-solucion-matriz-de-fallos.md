# Solución 08: matriz de fallos para la API de cursos

Esta propuesta toma el diseño de Catálogo separado del [ADR 002](adr/002-extraccion-catalogo.md). Los valores son criterios de aceptación ilustrativos, no mediciones. No se desplegaron servicios ni se inyectaron fallos en el [laboratorio](../../../labs/09-backend-cupos/README.md).

## Política de lectura

- **Presupuesto:** 500 ms de extremo a extremo. Máximo dos intentos de 180 ms, con 40 ms de espera antes del segundo y 100 ms reservados para el resto del flujo. No se reintenta si ya no queda tiempo o el circuito se abrió.
- **Reintento:** solo para fallos transitorios de transporte, timeout o HTTP 502, 503 y 504 en la lectura. No se reintentan errores 4xx ni el POST de inscripción con resultado incierto.
- **Circuit breaker:** contar los fallos por intento remoto. Abrir tras cinco fallos consecutivos, esperar 10 s y permitir una sola llamada de prueba. Si responde bien, cerrar y reiniciar el contador; si falla, volver a abrir. Probar estas transiciones con un reloj controlable.
- **Degradación:** devolver la última descripción conocida si no supera 60 s de antigüedad y marcarla como desactualizada. Sin una copia vigente dentro de ese límite, responder 503. La caché no autoriza inscripciones.

## Matriz de fallos

| Escenario | Fallo inyectado | Qué medir | Resultado esperado |
| --- | --- | --- | --- |
| Catálogo lento | La respuesta tarda 700 ms en cada intento. | Duración total, intentos por petición y tiempo consumido. | Con circuito cerrado, como máximo dos intentos; respuesta en 500 ms o menos con datos marcados como desactualizados o 503, según la caché. |
| Catálogo caído | Todas las conexiones fallan; hacer cinco intentos remotos fallidos. | Apertura del circuito y llamadas al origen antes y durante la ventana abierta. | Tras abrir, no se llama al origen durante 10 s. El cliente usa caché elegible o recibe 503; un curso ya habilitado sigue admitiendo altas. |
| Catálogo se recupera | Restablecer el origen antes de que termine la ventana abierta. | Número de llamadas de prueba y estado del circuito. | Antes del plazo no hay llamadas remotas. Al vencer, una prueba exitosa cierra el circuito y devuelve datos frescos; una prueba fallida lo abre otra vez. |
| Se pierde la respuesta del POST | Interrumpir la respuesta después del commit de una inscripción con un cupo inicial. | Resultado del cliente y estado persistido del curso. | El cliente observa un timeout o desconexión, no un rechazo confirmado. La base conserva una inscripción y cero cupos; el cliente no reintenta automáticamente. |
| PostgreSQL rechaza la escritura antes del commit | Forzar un error de base de datos que revierta la transacción. | Código HTTP, filas de inscripción y cupos. | No se responde 201; queda el cupo inicial y ninguna inscripción nueva. |
| Saturación de lecturas | Mantener ocupados los permisos de lectura de Catálogo mientras se envían dos POST por el último cupo de un curso habilitado. | Concurrencia de lecturas, latencia de los POST y estado final. | Con presupuesto propio para Inscripciones y su base sana, ambos POST terminan dentro del límite acordado para escritura (por ejemplo, 1 s en la prueba): uno devuelve 201 y el otro 409. Queda una inscripción y cero cupos. |

En una prueba de integración, inyectar la pérdida de respuesta **después** de observar el commit, no simularla con una excepción antes de persistir. Distinguir timeout de lectura, circuito abierto y respuesta desde caché en las señales de operación: pueden producir el mismo resultado visible, pero requieren diagnósticos distintos.
