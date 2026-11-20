# Guía de continuidad

Última revisión: 20 de noviembre de 2026.

Este documento permite retomar el aprendizaje sin depender del historial de una conversación.

## Estado actual

- Repositorio: `antonionsx1/git-java-fullstack-labs`.
- Rama predeterminada: `main`.
- Java 21, Maven y JUnit configurados en `app/`.
- CI ejecuta `mvn verify` mediante GitHub Actions.
- Dependabot mantiene Maven y las Actions.
- El curso base contiene ocho módulos de Git profesional.
- El laboratorio 01 vive en la rama `practice/01-modelo-mental`.
- El PR #6 está abierto como borrador, es mergeable y su CI más reciente fue exitoso.

## Punto exacto para retomar

### Opción A: completar el laboratorio 01

```bash
git clone https://github.com/antonionsx1/git-java-fullstack-labs.git
cd git-java-fullstack-labs
git fetch origin
git switch practice/01-modelo-mental
git status --short
```

Lee:

- `labs/01-modelo-mental/README.md`
- `labs/01-modelo-mental/BITACORA.md`
- PR #6

La demostración remota ya contiene commits atómicos y CI verde. Queda practicar localmente:

```bash
git diff
git add -p
git diff --staged
cd app
mvn verify
```

Después completa la bitácora con observaciones propias, auto-revisa el PR y decide si debe marcarse listo.

### Opción B: iniciar el módulo siguiente

Crea una rama desde `main`:

```bash
git switch main
git pull --ff-only
git switch -c practice/02-commits-profesionales
```

Usa `docs/02-commits-profesionales.md` como guía y crea `labs/02-commits-profesionales/README.md` con objetivo, pasos, evidencia y Definition of Done.

## Orden sugerido

1. Modelo mental e index.
2. Commits profesionales.
3. Ramas cortas y pull requests.
4. Merge, rebase y conflictos.
5. Restore, revert, reflog y stash.
6. Log, blame y bisect.
7. Tags y releases.
8. CI/CD y seguridad.

No avances por número de videos. Avanza cuando puedas explicar la decisión, ejecutar el flujo y recuperar un error.

## Sesión de estudio recomendada

1. Lee el objetivo durante 10 minutos.
2. Practica 30–45 minutos.
3. Provoca al menos un error controlado.
4. Recupera el estado sin borrar trabajo.
5. Ejecuta pruebas.
6. Documenta evidencia en el PR.
7. Escribe una conclusión de tres líneas.

## Checklist al cerrar una sesión

- [ ] La rama está publicada.
- [ ] Los commits son pequeños y explicables.
- [ ] Las pruebas pasan.
- [ ] CI está verde o el fallo está documentado.
- [ ] La bitácora distingue hechos, hipótesis y pendientes.
- [ ] El README indica el siguiente paso.

## Regla de actualización

Cada curso o laboratorio debe actualizar:

1. `courses/README.md`;
2. su propio README;
3. CI si agrega otra tecnología;
4. esta guía cuando cambie el punto exacto para retomar.
