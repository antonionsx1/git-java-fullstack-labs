# ADR 002: mantener el monolito y evaluar la extracción de Catálogo

**Estado:** aceptado para el laboratorio. La extracción es una alternativa, no una implementación.

## Decisión

Mantener el monolito modular del [ADR 001](001-monolito-modular.md). No hay mediciones que demuestren que Catálogo necesita escalar por separado. Extraerlo hoy introduciría otro despliegue y un proceso de aprovisionamiento entre servicios sin un beneficio demostrado.

Retomar la decisión si, tras optimizar las consultas, el p95 de `GET /cursos` supera sostenidamente los 300 ms a 50 solicitudes por segundo y una prueba comparativa muestra que dar capacidad independiente a Catálogo cumple ese objetivo a un costo aceptable. Son umbrales de ejemplo, no resultados medidos.

## Diseño si se aprueba la extracción

```text
GET /cursos ---> Catálogo -------> BD Catálogo [curso]
                    |
                    | aprovisionar cupo inicial para cursoId publicado
                    v
POST /cursos/{cursoId}/inscripciones ---> Inscripciones ---> BD Inscripciones
                                                            [cupo, inscripcion]
```

Catálogo posee la publicación del curso. Inscripciones posee el cupo y decide si confirma el alta. El aprovisionamiento lleva el identificador del curso publicado y su cupo inicial acordado; repetirlo para el mismo curso no repone los lugares consumidos. Un curso no aprovisionado no admite inscripciones.

El POST no llama a Catálogo: durante una caída de ese servicio, los cursos ya aprovisionados siguen admitiendo inscripciones. El decremento condicional y la inserción conservan su transacción en la base de Inscripciones.

## Migración y reversión

1. Copiar los cursos al nuevo almacén y verificar que todos los `curso_id` de `cupo` existan allí. Mantener la clave foránea del monolito hasta verificar el inventario y el aprovisionamiento.
2. En la ventana de cambio, pausar las escrituras de Catálogo, sincronizar los cambios pendientes y habilitar el nuevo servicio como único escritor. Mantener las tablas de Inscripciones en su base y retirar la clave foránea entre contextos solo cuando la validación previa haya pasado.
3. Si hay referencias faltantes o los contratos no responden como se espera, cancelar el cambio. Si el nuevo servicio ya aceptó escrituras, pausarlas y reconciliar esos cambios antes de devolver la escritura al monolito. No restaurar los datos de Inscripciones desde una copia que pierda altas confirmadas.

## Criterios de aceptación

- Publicar una versión de Catálogo sin redesplegar Inscripciones.
- Confirmar un alta en un curso habilitado con Catálogo detenido.
- Repetir el aprovisionamiento tras una inscripción y comprobar que el cupo no aumenta.
- Mantener las pruebas de último cupo concurrente y rollback por duplicado.

Estos criterios quedan pendientes mientras no se implemente la extracción.
