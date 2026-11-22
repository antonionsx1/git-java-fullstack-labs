# Catálogo de cursos relacionados

Este directorio registra cursos externos y traduce su contenido a prácticas verificables. No se copian videos ni material protegido: se documentan objetivos, decisiones, ejercicios propios y evidencia.

## Cursos

| Curso o ruta | Enfoque laboral | Estado | Laboratorios |
| --- | --- | --- | --- |
| Git profesional para Java Full Stack | Colaboración, recuperación, PR, releases y CI | En progreso | `labs/01-modelo-mental` y retos de `labs/README.md` |
| [Backend con Java y Spring](backend-java-spring/README.md) | Decisiones de arquitectura y atributos de calidad | En progreso | Ejercicio de diseño de una API de cursos |

## Criterios para incorporar un curso

Inclúyelo cuando:

- aporte una competencia requerida para Java Full Stack;
- permita construir o mejorar un artefacto práctico;
- tenga contenidos vigentes o reemplazables por prácticas actuales;
- pueda verificarse con código, pruebas, CI o documentación;
- no duplique otro curso sin aportar profundidad.

No lo incorpores completo cuando se concentre en instalaciones, interfaces antiguas, comparaciones comerciales o herramientas descontinuadas.

## Rutas recomendadas para ampliar

### Java backend

- Java moderno y concurrencia.
- Spring Boot, Spring Security y testing.
- JPA, SQL y migraciones con Flyway o Liquibase.
- Arquitectura modular, DDD y patrones.
- Observabilidad y rendimiento JVM.

### Frontend

- TypeScript.
- Angular o React.
- Accesibilidad y rendimiento web.
- Pruebas unitarias, integración y E2E.

### Plataforma

- Docker y redes.
- Kubernetes.
- Azure o AWS.
- CI/CD, seguridad de cadena de suministro y observabilidad.
- Terraform cuando el puesto incluya infraestructura.

## Proceso de alta

1. Copia `courses/_template/README.md`.
2. Crea `courses/<slug-del-curso>/README.md`.
3. Evalúa el temario: imprescindible, útil, opcional u obsoleto.
4. Define un proyecto o mejora observable.
5. Crea laboratorios en `labs/<numero>-<tema>/`.
6. Añade pruebas y CI.
7. Trabaja mediante una rama y PR.
8. Actualiza la tabla de este catálogo.

## Convenciones

- Slugs en minúsculas y separados por guiones.
- Evidencia propia; no copiar transcripciones.
- Conventional Commits.
- Un PR por unidad de aprendizaje pequeña.
- Cada curso debe indicar fecha de revisión y tecnologías utilizadas.
