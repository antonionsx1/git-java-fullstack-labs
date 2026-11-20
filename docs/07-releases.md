# 07 — Tags y releases

Semantic Versioning:

- `MAJOR`: incompatibilidad.
- `MINOR`: funcionalidad compatible.
- `PATCH`: corrección compatible.

~~~bash
git tag -a v1.0.0 -m "Release 1.0.0"
git show v1.0.0
git push origin v1.0.0
~~~

No muevas ni reutilices tags publicados. Una release debe reproducirse desde el tag y tener notas orientadas al usuario.

## Laboratorio

Define v1.0.0, ejecuta pruebas, crea un tag anotado y genera notas. Después corrige un defecto y publica v1.0.1. En proyectos reales, firma tags y artefactos según la política del equipo.
