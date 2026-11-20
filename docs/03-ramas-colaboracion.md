# 03 — Ramas cortas y pull requests

~~~bash
git switch main
git pull --ff-only
git switch -c feat/personalized-greeting
# cambios, pruebas y commits
git push -u origin feat/personalized-greeting
~~~

Las ramas duran horas o pocos días. Integra frecuentemente, divide funciones grandes y usa feature flags si algo incompleto debe llegar a `main`.

Un PR efectivo incluye problema, solución, verificación, riesgos y reversión. El autor revisa primero su propio diff.

## Git Flow

Conoce `develop`, `release` y `hotfix`, pero no lo uses por defecto. Puede servir para versiones instaladas con mantenimiento prolongado; en entrega continua añade ramas longevas.

## Laboratorio

Implementa una mejora menor, publica un PR con la plantilla y revísalo por comportamiento, legibilidad, pruebas, seguridad y operación.
