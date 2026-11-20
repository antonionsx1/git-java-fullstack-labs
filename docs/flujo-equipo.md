# Flujo de equipo recomendado

1. Comprende el ticket y criterios.
2. Actualiza `main` con `git pull --ff-only`.
3. Crea una rama corta: `feat/`, `fix/`, `refactor/`, `docs/` o `ci/`.
4. Haz commits atómicos y ejecuta pruebas.
5. Abre un draft PR pronto si necesitas conversación.
6. Auto-revisa el diff y completa la plantilla.
7. Resuelve conversaciones y espera checks verdes.
8. Integra mediante squash salvo razón documentada.

El revisor evalúa comportamiento, diseño, legibilidad, pruebas, seguridad, compatibilidad y operación. Las decisiones importantes permanecen en el PR o en un ADR.

En incidentes no hagas force push a `main`: crea una rama, aplica `revert`, valida y abre un PR urgente.
