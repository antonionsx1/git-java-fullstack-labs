# 05 — Recuperación sin pánico

- Sin preparar: `git restore archivo`.
- Preparado por error: `git restore --staged archivo`.
- Commit local incorrecto: `git commit --amend` o rebase interactivo.
- Commit publicado: `git revert <sha>`.
- Trabajo temporal: `git stash push -u -m "contexto"`.
- Commit perdido: `git reflog`.

`reset --hard` elimina cambios no guardados. Úsalo solo en una rama de laboratorio y crea antes una referencia de seguridad.

~~~bash
git switch -c practice/recovery
# crea dos commits
git branch safety/recovery
git reset --hard HEAD~2
git reflog
git switch -c recovered/session <sha-encontrado>
~~~

En producción, revierte mediante PR. No reescribas `main`.
