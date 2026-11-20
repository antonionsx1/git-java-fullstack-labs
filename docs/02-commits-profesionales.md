# 02 — Commits profesionales

Crea commits pequeños que compilen, tengan una sola intención y expliquen por qué.

~~~text
<tipo>(<alcance>): <descripción imperativa>

Contexto y razón cuando no son obvios.
Refs: #123
~~~

Tipos útiles: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci` y `chore`.

~~~bash
git add -p
git commit --verbose
git commit --amend
git show --stat
~~~

`amend` es para trabajo todavía no compartido. No mezcles formato masivo, refactor y funcionalidad.

## Laboratorio

Implementa una validación en un commit, su prueba en otro y documentación en un tercero. Cada commit debe pasar `mvn verify` y ser revisable aisladamente.
