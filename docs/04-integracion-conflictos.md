# 04 — Integración, rebase y conflictos

## Elegir estrategia

- **Squash merge:** predeterminada para PR pequeños.
- **Merge commit:** conserva topología cuando aporta valor.
- **Rebase:** actualiza o limpia una rama privada.
- **Fast-forward:** mueve el puntero sin commit de merge.

Nunca rebases una rama compartida sin coordinarlo. Después de reescribir tu rama usa `git push --force-with-lease`, nunca `--force`.

~~~bash
git rebase -i HEAD~3
# reword, squash, fixup, reorder

git status
# editar, comprender y validar
git add <archivo>
git rebase --continue
git rebase --abort
~~~

Resolver no es borrar marcadores: es conservar la intención correcta y probarla.

## Laboratorio

Dos ramas modifican `GreetingService`. Integra una, rebasa la otra, resuelve el conflicto, ejecuta pruebas y explica cómo preservaste ambas intenciones.
