# Hoja de referencia

~~~bash
git status --short
git diff
git diff --staged
git log --graph --decorate --oneline --all

git switch main
git switch -c feat/nombre
git fetch --prune
git add -p
git commit

git pull --ff-only
git rebase origin/main
git push -u origin HEAD
git push --force-with-lease

git restore archivo
git restore --staged archivo
git revert <sha>
git reflog
git stash push -u -m "contexto"

git log -S"texto"
git log -G"regex"
git blame archivo
git bisect start

git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
~~~

Evita `git add .` sin revisar, `git push --force`, commits gigantes, ramas de semanas y secretos en Git.

## Configuración e inicio

- `git config --global user.name "<nombre>"` y `git config --global user.email "<correo>"` configuran la identidad predeterminada de tus commits.
- `git init` inicializa un repositorio y `git clone <url>` crea una copia local de uno existente.

## Revisar una rama

- `git fetch origin` actualiza las referencias de seguimiento remoto. `git diff origin/main...HEAD` muestra los cambios desde el ancestro común con `origin/main` hasta tu commit actual.
- `git show <commit>` inspecciona un commit. `git log --follow -- <archivo>` sigue el historial de un archivo a través de renombres.
- `git branch` lista las ramas locales. `git merge <rama>` integra la rama indicada en la actual. `git branch -d <rama>` elimina una rama local si Git la considera integrada.

## Administrar cambios y archivos

- `git add -- <archivo>` prepara los cambios de un archivo. `git commit -m "<mensaje>"` registra lo que está preparado.
- `git stash list` lista las entradas guardadas. `git stash pop` aplica la más reciente y la retira si la aplicación tiene éxito. `git stash drop stash@{0}` elimina una entrada sin aplicarla. En PowerShell, escribe `'stash@{0}'` entre comillas.
- `git mv <origen> <destino>` mueve un archivo y prepara el cambio. `git rm --cached -- <archivo>` lo retira del índice y conserva la copia de trabajo.
- `.gitignore` define patrones para archivos sin seguimiento. `git ls-files --others --ignored --exclude-standard` lista los archivos ignorados sin seguimiento.

Para elegir cómo deshacer un cambio, consulta el [módulo de recuperación](05-recuperacion.md).

Referencia de origen: *Hoja de referencia para GitHub Git*, versión 1.1.1. [Documentación oficial de Git](https://git-scm.com/docs/git)
