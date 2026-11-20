# Laboratorio 01 — Modelo mental

Esta rama es el espacio seguro para completar el primer laboratorio. No hagas los ejercicios directamente en `main`.

## Objetivo

Observar un mismo cambio mientras pasa por:

```text
working tree -> index -> commit local -> remoto -> pull request
```

## Ejercicio

1. Clona el repositorio y cambia a esta rama:

   ```bash
   git fetch origin
   git switch practice/01-modelo-mental
   ```

2. Modifica el texto devuelto por `GreetingService.greet`.
3. Agrega una segunda modificación independiente en el mismo archivo.
4. Observa ambos cambios:

   ```bash
   git status --short
   git diff
   ```

5. Ejecuta `git add -p` y prepara solamente una modificación.
6. Compara:

   ```bash
   git diff
   git diff --staged
   ```

7. Crea un commit Conventional Commit.
8. Prepara y confirma el segundo cambio.
9. Ejecuta:

   ```bash
   cd app
   mvn verify
   ```

10. Publica la rama y registra abajo la evidencia.

## Bitácora

- Working tree:
- Index:
- Commits creados:
- Comandos que resultaron confusos:
- Error provocado:
- Forma de recuperación:
- Resultado de `mvn verify`:

## Condición de terminado

- [ ] Hay al menos dos commits atómicos.
- [ ] Los mensajes siguen Conventional Commits.
- [ ] Las pruebas pasan.
- [ ] La bitácora explica lo observado.
- [ ] El PR está listo para auto-revisión.
