# Laboratorio 09: el último cupo

Aplica [Estilos arquitectónicos](../../courses/backend-java-spring/04-arquitectura-de-software/02-estilos-arquitectonicos.md) con un monolito modular de Spring Boot y PostgreSQL. El catálogo lee sus propios datos; inscripciones controla el cupo y el registro. Ambos módulos se despliegan juntos.

## Verificar

Con JDK 17+, Maven 3.6.3+ y Docker Compose, ejecuta desde este directorio:

```sh
docker compose up -d --wait
mvn verify
```

Usa solo la base del laboratorio: las pruebas limpian las inscripciones del curso de demostración y ajustan su cupo. La prueba concurrente exige exactamente un HTTP 201, un HTTP 409, una fila de inscripción y cero cupos restantes. Otra prueba fuerza una inscripción duplicada y comprueba el rollback del decremento.

Para probar únicamente el caso de uso sin Spring ni PostgreSQL, ejecuta `mvn -Dtest=InscribirAlumnoCasoDeUsoTest test`. Son las pruebas de los casos con y sin cupo de la [lección de arquitectura hexagonal](../../courses/backend-java-spring/04-arquitectura-de-software/04-arquitectura-limpia-y-hexagonal.md).

## Probar la API

En una base recién iniciada, ejecuta `mvn spring-boot:run` y, desde otra terminal:

```sh
curl -i http://127.0.0.1:8080/cursos
curl -i -X POST "http://127.0.0.1:8080/cursos/1/inscripciones?alumnoId=101"
curl -i -X POST "http://127.0.0.1:8080/cursos/1/inscripciones?alumnoId=102"
```

La primera inscripción devuelve 201 y la segunda 409. Ejecuta `docker compose down -v` para eliminar los datos de esta base local y volver a empezar. La API no autentica alumnos: el identificador explícito solo sirve para la práctica y la aplicación escucha en loopback.

## Decisión

El SQL decrementa el cupo solo cuando es positivo: bajo el aislamiento predeterminado de PostgreSQL, una actualización concurrente espera por la misma fila y vuelve a evaluar la condición. El decremento y el insert pertenecen a la misma transacción. Una lectura previa del cupo sin una actualización condicional no ofrece esa garantía.

Conserva el monolito modular mientras el equipo y la carga no requieran despliegues independientes. Completa el ADR de la lección con el costo de compartir el despliegue y la métrica que justificaría extraer el catálogo.
