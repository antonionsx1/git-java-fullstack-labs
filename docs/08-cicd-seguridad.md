# 08 — CI/CD y seguridad

El repositorio compila y prueba con Java 21. El patrón se transfiere a GitLab CI y Azure Pipelines: checkout, toolchain, caché, build, pruebas, análisis y artefactos.

## Controles vigentes

- Rama principal protegida.
- PR y revisión obligatorios.
- Checks requeridos.
- Permisos mínimos del workflow.
- Dependencias actualizadas automáticamente.
- Escaneo de secretos y dependencias.
- Aprobación para producción.
- OIDC para nube, no credenciales permanentes.

## Si un secreto llegó a Git

1. Revócalo inmediatamente.
2. Rota credenciales relacionadas.
3. Investiga uso indebido.
4. Limpia el historial después y según el procedimiento del equipo.

Borrar el archivo en otro commit no elimina el secreto del historial.

## Laboratorio

Rompe una prueba, observa CI en rojo y corrígela con un commit separado. Configura el check como requisito de `main` si la política de la cuenta lo permite.
