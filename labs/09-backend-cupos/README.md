# Laboratorio 09: el último cupo

Aplica [Estilos arquitectónicos](../../courses/backend-java-spring/04-arquitectura-de-software/02-estilos-arquitectonicos.md) con un monolito modular de Spring Boot y PostgreSQL. El catálogo lee sus propios datos; inscripciones controla el cupo y el registro. Ambos módulos se despliegan juntos.

Consulta la [guía de arquitectura](ARQUITECTURA.md) para ver los límites del sistema, la transacción del último cupo y las pruebas que respaldan cada garantía.

## Verificar

Con JDK 17+, Maven 3.6.3+ y Docker Compose, ejecuta desde este directorio:

```sh
docker compose up -d --wait
mvn verify
```

Usa solo la base del laboratorio: las pruebas limpian las inscripciones del curso de demostración y ajustan su cupo. La prueba concurrente exige exactamente un HTTP 201, un HTTP 409, una fila de inscripción y cero cupos restantes. Otra prueba fuerza una inscripción duplicada y comprueba el rollback del decremento.

Para probar únicamente el caso de uso sin Spring ni PostgreSQL, ejecuta `mvn -Dtest=InscribirAlumnoCasoDeUsoTest test`. Son las pruebas de los casos con y sin cupo de la [lección de arquitectura hexagonal](../../courses/backend-java-spring/04-arquitectura-de-software/04-arquitectura-limpia-y-hexagonal.md).

## Base existente sin Flyway

En una base vacía, Flyway ejecuta V1, V2 y V3 automáticamente. En una base creada por la versión anterior, el arranque normal rechaza la adopción automática:

1. Detén los escritores y respalda la base. En una copia, compara tablas, tipos, nulabilidad, restricciones e índices con [V1](src/main/resources/db/migration/V1__esquema_inicial.sql), por ejemplo con `pg_dump --schema-only`. Si hay diferencias o ya existe un historial de Flyway, no marques V1 a ciegas.
2. Solo tras comprobar la coincidencia y ensayar la actualización en la copia, arranca una vez con `mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.flyway.baseline-on-migrate=true --spring.flyway.baseline-version=1"`. Flyway marca V1 como punto de partida y ejecuta V2 y V3. No dejes esa opción habilitada para los siguientes arranques.
3. Comprueba el historial de Flyway y los conteos y cupos previos. V2 deja `confirmada_en` en `NULL` para inscripciones históricas; V3 no sobrescribe el curso de demostración ni repone su cupo si ya existe.

Las altas nuevas escriben la fecha de confirmación en la misma transacción que consume el cupo. El contrato HTTP no cambia.

## Probar la API

En una base recién iniciada, ejecuta `mvn spring-boot:run`. El catálogo es público y el POST sin credenciales devuelve 401. Sin configuración del emisor, el catálogo sigue disponible, pero no se acepta ningún token.

```sh
curl -i http://127.0.0.1:8080/cursos
curl -i -X POST http://127.0.0.1:8080/cursos/1/inscripciones
```

Para probar el alta manualmente, usa un emisor de pruebas y una configuración externa como esta (sin claves privadas ni tokens):

```yaml
cursos:
  seguridad:
    emisor: https://issuer.example.test
    audiencia: cursos-lab
    clave-publica: file:///ruta/clave-publica.pem
    alumnos:
      "[sujeto-del-alumno]": 101
```

Arranca con `--spring.config.additional-location=file:/ruta/seguridad-local.yaml` como argumento de la aplicación. El token debe estar firmado con RS256 por el emisor configurado, tener la audiencia esperada, una expiración válida, el sujeto vinculado y el scope `inscripciones:crear`. Envía el token en la cabecera `Authorization: Bearer …`, sin `alumnoId` en la URL. Con cupo y credenciales válidas, la primera alta devuelve 201 y el duplicado devuelve 409.

La API solo autentica mediante tokens Bearer en cabecera, sin sesión ni cookies de autenticación. El laboratorio escucha en loopback; no reutilices la configuración de prueba en producción. Para probar sin un emisor externo, `mvn verify` firma los tokens de prueba con claves efímeras y usa PostgreSQL.

## Consultar señales

Usa un JWT con el scope `observabilidad:leer` para leer `GET /actuator/metrics/http.server.requests` o `GET /actuator/metrics/cursos.inscripciones`. Ese scope no concede permiso para inscribir y un token con solo `inscripciones:crear` no puede leer métricas. Los contadores de negocio aparecen al observar su primer resultado.

Busca en los logs JSON el `requestId` que devuelve la cabecera `X-Request-Id`. Los eventos de negocio usan la ruta normalizada y distinguen `confirmada` (201), `duplicada` o `cupo_agotado` (409), y `fallo_persistencia` (503). Los rechazos de seguridad se cuentan en las métricas HTTP, no como resultados de inscripción. La duración del log de negocio mide el caso de uso y su transacción; la métrica HTTP mide la solicitud completa.

`mvn verify` comprueba la protección de las métricas, la correlación entre cabecera y log, y que un rechazo de escritura de PostgreSQL no descuente un cupo. No se exportan trazas en este laboratorio.

## Decisión

El SQL decrementa el cupo solo cuando es positivo: bajo el aislamiento predeterminado de PostgreSQL, una actualización concurrente espera por la misma fila y vuelve a evaluar la condición. El decremento y el insert pertenecen a la misma transacción. Una lectura previa del cupo sin una actualización condicional no ofrece esa garantía.

Conserva el monolito modular mientras el equipo y la carga no requieran despliegues independientes. Completa el ADR de la lección con el costo de compartir el despliegue y la métrica que justificaría extraer el catálogo.
