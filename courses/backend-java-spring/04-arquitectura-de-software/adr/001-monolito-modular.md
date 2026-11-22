# ADR 001: monolito modular para la API de cursos

**Estado:** aceptado para el laboratorio.

## Contexto

Un equipo pequeño mantiene el catálogo y las inscripciones. Una campaña puede aumentar las lecturas del catálogo. El cupo, en cambio, tiene una regla estricta: no debe confirmarse una inscripción si ya no quedan lugares.

## Decisión

Desplegar una aplicación Spring Boot con dos módulos: catálogo e inscripciones. Catálogo publica la información de los cursos; inscripciones es dueño del cupo y del registro de alumnos. Ambos usan PostgreSQL. Inscripciones decrementa el cupo mediante una actualización condicional y guarda la inscripción en la misma transacción; si la inserción falla, se revierte el decremento.

## Alternativas

- **Monolito por capas técnicas sin límites funcionales:** ofrece la misma simplicidad de despliegue, pero no deja tan claro qué parte del sistema puede decidir una inscripción.
- **Servicios separados:** permiten desplegar y escalar el catálogo sin desplegar inscripciones, pero añaden contratos de red y operación de dos aplicaciones. Aún no hay evidencia de que ese costo sea necesario.

## Consecuencias

Se priorizan la consistencia del cupo y una operación sencilla. A cambio, los módulos comparten despliegue y capacidad: no se puede escalar solo el código del catálogo agregando instancias de la aplicación. Los límites de acceso a datos se conservan por convención de código, no por credenciales separadas.

## Verificación y revisión

El [laboratorio](../../../../labs/09-backend-cupos/README.md) verifica que dos solicitudes simultáneas con un cupo produzcan un HTTP 201, un HTTP 409 y una sola inscripción. También comprueba que una inscripción duplicada no consuma otro lugar.

Como objetivo de ejemplo para una campaña, medir el percentil 95 de latencia del catálogo a 50 solicitudes por segundo y buscar que sea inferior a 300 ms. Si supera ese objetivo de forma sostenida después de revisar las consultas, y medir por separado demuestra que el catálogo necesita capacidad independiente, reevaluar su extracción. El laboratorio no mide ni da por cumplido este objetivo de carga.
