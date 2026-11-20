# Git para Java Full Stack: laboratorios profesionales

Ruta práctica para aprender el Git usado hoy en equipos de software. El objetivo no es memorizar comandos: es trabajar con cambios pequeños, revisables, recuperables y automatizados sobre una aplicación Java real.

## Ruta

| Módulo | Resultado |
| --- | --- |
| [01. Modelo mental](docs/01-modelo-mental.md) | Working tree, index, commits y remotos |
| [02. Commits profesionales](docs/02-commits-profesionales.md) | Historial legible |
| [03. Ramas y colaboración](docs/03-ramas-colaboracion.md) | Ramas cortas y PR |
| [04. Integración y conflictos](docs/04-integracion-conflictos.md) | Merge, rebase y squash |
| [05. Recuperación](docs/05-recuperacion.md) | Corregir sin perder trabajo |
| [06. Diagnóstico](docs/06-diagnostico.md) | Log, blame y bisect |
| [07. Releases](docs/07-releases.md) | Tags y versiones |
| [08. CI/CD y seguridad](docs/08-cicd-seguridad.md) | Automatización y protección |

Consulta el [flujo de equipo](docs/flujo-equipo.md), la [hoja de referencia](docs/cheatsheet.md) y los [retos](labs/README.md).

## Aplicación de práctica

`app/` contiene una aplicación Maven pequeña para practicar con cambios reales.

~~~bash
cd app
mvn verify
mvn exec:java
~~~

Requisitos: Git 2.40+, JDK 21 y Maven 3.9+.

## Principios

1. `main` siempre debe ser integrable.
2. Una rama representa una intención pequeña.
3. Un commit explica una decisión.
4. Un PR facilita revisión, no almacena semanas de trabajo.
5. La historia pública se corrige con `revert`; la local puede limpiarse con rebase.
6. Nunca se guardan secretos en Git.
7. Automatiza todo control repetible.

## Fuera de alcance deliberadamente

No se dedica tiempo a instalaciones repetitivas, administrar GitLab CE, comparar interfaces ni AWS CodeCommit. Git Flow se estudia solo para reconocerlo; trunk-based development es la opción predeterminada.
