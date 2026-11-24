# 05 — Recuperación sin pánico

- Sin preparar: `git restore archivo`.
- Preparado por error: `git restore --staged archivo`.
- Commit local incorrecto: `git commit --amend` o rebase interactivo.
- Commit publicado: `git revert <sha>`.
- Trabajo temporal: `git stash push -u -m "contexto"`.
- Commit perdido: `git reflog`.

`git reset --hard <commit>` mueve la rama actual al commit indicado y ajusta el índice y el árbol de trabajo a ese estado. Descarta cambios sin commit en archivos rastreados y puede sobrescribir archivos sin seguimiento que estorben al escribir el contenido del commit.

Antes del ejercicio, ejecuta `git status` y guarda cualquier cambio que quieras conservar. La rama de respaldo conserva los commits, pero no captura cambios sin commit. [Documentación de git reset](https://git-scm.com/docs/git-reset)

~~~bash
git switch -c practice/recovery
# crea dos commits
git branch safety/recovery
git reset --hard HEAD~2
git reflog
git switch -c recovered/session <sha-encontrado>
~~~

En producción, revierte mediante PR. No reescribas `main`.
