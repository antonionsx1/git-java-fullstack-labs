# 01 — Modelo mental de Git

~~~text
working tree -> git add -> index -> git commit -> repositorio local -> git push -> remoto
~~~

Git guarda snapshots conectados. Una rama es un nombre móvil que apunta a un commit; `HEAD` indica dónde estás trabajando.

~~~bash
git status --short
git diff
git diff --staged
git log --graph --decorate --oneline --all
git show HEAD
~~~

Usa `git switch` para ramas y `git restore` para archivos.

## Laboratorio

Crea `practice/modelo-mental`, cambia `GreetingService`, observa el diff, prepara solo una parte con `git add -p` y compara `git diff` con `git diff --staged`. Documenta en el PR dónde vivía cada cambio antes del commit.
