# Prueba de carga de GET /cursos

Hipótesis: habilitar gzip reduce los bytes enviados, a cambio de trabajo de compresión. Se mantienen el mismo endpoint, datos, cliente y cabecera `Accept-Encoding: gzip`; el único cambio es `server.compression.enabled`. No se mide el POST de inscripción.

## Reproducir en Windows

Desde el directorio del laboratorio, con JDK 17, Maven, Node 24 y Docker. Reserva los puertos locales 55433 y 58081. La base es temporal, independiente de la del laboratorio, y contiene datos sintéticos.

```powershell
docker run --rm -d --name backend-cupos-benchmark -p 127.0.0.1:55433:5432 -e POSTGRES_DB=cursos_benchmark -e POSTGRES_USER=lab -e POSTGRES_PASSWORD=lab postgres:17
docker exec backend-cupos-benchmark pg_isready -U lab -d cursos_benchmark
```

Cuando PostgreSQL acepte conexiones:

```powershell
docker exec backend-cupos-benchmark psql -U lab -d cursos_benchmark -v ON_ERROR_STOP=1 -c "CREATE TABLE curso (id BIGINT PRIMARY KEY, titulo TEXT NOT NULL); INSERT INTO curso SELECT i, 'Curso ' || i || ' - Java y Spring: APIs, persistencia y pruebas - ' || md5(i::text) FROM generate_series(2,5000) AS i; ANALYZE curso;"
mvn -B -ntp -DskipTests package
$env:CURSOS_JDBC_URL='jdbc:postgresql://127.0.0.1:55433/cursos_benchmark'
$env:CURSOS_DB_USER='lab'
$env:CURSOS_DB_PASSWORD='lab'
node benchmark/catalogo.mjs
docker stop backend-cupos-benchmark
```

La inicialización de Spring agrega el curso 1 para completar 5 000 filas. El script arranca una JVM nueva por ronda (heap de 256 a 512 MiB), calienta 3 s y mide 6 s por nivel a 25, 50 y 100 solicitudes/s. Ejecuta dos rondas por variante en orden sin compresión, gzip, gzip, sin compresión, para reducir el efecto del orden. Usa 24 conexiones HTTP como máximo y un límite de 2 s por solicitud.

Esta es una comparación local corta, no el protocolo prolongado de la lección. El p95 mide del envío por el cliente Node hasta recibir todo el cuerpo, sin descomprimirlo. El retraso de arranque se registra aparte. Se cuentan respuestas HTTP 200 completas como éxitos, y los bytes corresponden solo al cuerpo de una respuesta, no a cabeceras ni a tráfico de transporte.

## Resultados observados

Windows x64, Intel Core i7-13620H (16 procesadores lógicos), 16 GiB, Java 17.0.12, Node 24.13.1 y PostgreSQL 17.12 en Docker. Cliente, aplicación y base en el mismo equipo. Cada celda con dos valores indica las rondas 1 y 2. La tasa completada incluye fallos y divide por el tiempo hasta el último resultado, no solo por la ventana de envío.

| Variante | Objetivo (sol./s) | Tasa completada (sol./s) | HTTP 200 completos / enviados por ronda | p95 de éxitos (ms) |
| --- | ---: | ---: | --- | ---: |
| Sin compresión | 25 | 25,00 / 25,00 | 150/150, 150/150 | 24,97 / 25,54 |
| gzip | 25 | 25,00 / 24,98 | 150/150, 150/150 | 37,67 / 35,38 |
| Sin compresión | 50 | 50,00 / 49,80 | 300/300, 300/300 | 21,60 / 20,83 |
| gzip | 50 | 50,00 / 50,00 | 300/300, 300/300 | 29,94 / 26,96 |
| Sin compresión | 100 | 99,92 / 74,98 | 600/600, 550/600 | 35,20 / 1 659,56 |
| gzip | 100 | 99,89 / 99,84 | 600/600, 600/600 | 22,36 / 22,69 |

El cuerpo bajó de **572 712 a 135 985 bytes** por respuesta (76,3 % menos). A 50 solicitudes/s, ambas variantes cumplieron el objetivo ilustrativo de p95 menor a 300 ms y al menos 99 % de éxito en ambas rondas, pero gzip aumentó el p95 local. A 100 solicitudes/s, una ronda sin compresión tuvo 50 fallos; la otra no. El p95 de retraso de envío osciló entre 0,84 y 15,80 ms en el conjunto de muestras.

**Decisión:** conservar el despliegue actual. La reducción del tamaño justifica probar gzip con una red representativa, no afirmar que reduzca latencia o aumente capacidad en producción. No se midieron CPU, GC, pool ni bloqueos, así que no se identificó el recurso limitante ni la causa de la ronda atípica. Antes de decidir sobre escalado, repetir con ventanas largas, cliente separado y métricas del servidor y de PostgreSQL.
