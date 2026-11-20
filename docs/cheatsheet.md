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
