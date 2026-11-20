# Bitácora verificada — Laboratorio 01

## Evidencia remota

- `3e392e7 docs(greeting): explain the public contract`
- `2c3e011 refactor(greeting): isolate name validation`
- CI run 13: **success**.
- El PR #6 está abierto, en borrador y es mergeable.

## Alcance de la demostración

La conexión de GitHub escribió directamente sobre la rama, por lo que no existieron working tree ni index locales. Los commits demuestran separación de intenciones, pero no sustituyen la práctica de:

```bash
git status --short
git diff
git add -p
git diff --staged
cd app
mvn verify
```

## Estado

- [x] Dos commits atómicos.
- [x] Conventional Commits.
- [x] CI exitoso.
- [ ] Práctica local de working tree e index.
- [ ] Auto-revisión antes de sacar el PR de borrador.
