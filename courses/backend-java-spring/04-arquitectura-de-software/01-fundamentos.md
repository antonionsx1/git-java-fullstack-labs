# 01. Fundamentos de arquitectura

## Qué es arquitectura de software

Es la organización del sistema y el conjunto de decisiones que condicionan cómo se construye, se opera y evoluciona. En una API de cursos, por ejemplo, decidir si el catálogo y las inscripciones se despliegan juntos o por separado cambia la forma de probar, desplegar y diagnosticar el sistema.

## Arquitectura frente a diseño

La diferencia es el alcance y el costo de cambiar una decisión, no el tipo de archivo. Separar catálogo e inscripciones en módulos con dependencias explícitas es una decisión arquitectónica. Elegir cómo convertir una entidad en un DTO dentro del catálogo suele ser diseño local. Si ese DTO forma parte de un contrato público del que dependen otros sistemas, modificarlo ya no es un asunto puramente local.

## Atributos de calidad

Un atributo es más útil cuando se expresa como un escenario que se puede comprobar. Los siguientes objetivos son ilustrativos; no son requisitos universales.

| Atributo | Pregunta | Ejemplo verificable |
| --- | --- | --- |
| Mantenibilidad | ¿Qué tan fácil es cambiar algo sin afectar otras áreas? | Cambiar una regla de inscripción sin modificar el módulo de catálogo. |
| Escalabilidad | ¿Cómo responde el sistema cuando aumenta la carga? | Pasar de 50 a 100 solicitudes por segundo agregando instancias sin superar el objetivo de latencia. |
| Disponibilidad | ¿Cuánto tiempo está utilizable el servicio? | Mantener 99.9 % de solicitudes válidas exitosas en una ventana de 30 días. |
| Seguridad | ¿Quién puede acceder a qué y bajo qué condiciones? | Comprobar que un alumno autenticado no puede leer la inscripción de otro. |
| Observabilidad | ¿Podemos entender qué pasó desde las señales del sistema? | Seguir una inscripción fallida con un identificador de correlación y encontrar dónde falló sin exponer datos personales. |
| Rendimiento (performance) | ¿Cuánto tarda o cuánto procesa con una carga dada? | Mantener el percentil 95 de latencia del catálogo por debajo de 300 ms a 50 solicitudes por segundo. |

## Tradeoffs arquitectónicos

Un tradeoff es lo que aceptas perder o complicar para obtener un beneficio. Un monolito modular permite desplegar catálogo e inscripciones juntos y evita introducir llamadas de red entre ellos. A cambio, ambos módulos comparten el despliegue y no se pueden escalar por separado. Separarlos en servicios permite despliegues independientes, pero añade fallos de comunicación y trabajo operativo.

Otro ejemplo: una caché puede reducir la latencia del catálogo, pero permitir que un usuario vea datos desactualizados. Ese costo podría aceptarse para una descripción de curso y no para el cupo al confirmar una inscripción. La decisión depende de qué dato se consulta y qué pasa si está desactualizado.

## Ejercicio: API de cursos

Un equipo pequeño debe publicar un catálogo y permitir inscripciones. Durante una campaña aumenta la lectura del catálogo, pero nunca se deben inscribir más alumnos que los cupos disponibles.

1. Elige entre un monolito modular y servicios separados. Indica dónde se verifica y actualiza el cupo.
2. Prioriza dos atributos de calidad y define una forma concreta de medir o probar cada uno.
3. Redacta una decisión breve con contexto, opción elegida, alternativa descartada, beneficio y costo. Indica qué señal te haría reconsiderarla.

**Criterio de revisión:** otra persona debe poder explicar por qué elegiste esa arquitectura y qué prueba podría demostrar que la decisión no cumple su objetivo.
