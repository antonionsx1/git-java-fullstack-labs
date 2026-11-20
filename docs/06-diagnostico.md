# 06 — Diagnóstico del historial

~~~bash
git log -- path/al/archivo
git log -S"texto" --oneline
git log -G"expresión" --oneline
git blame -L 10,30 archivo
git show <sha>
~~~

`blame` busca contexto, no culpables. Lee el commit y el PR antes de concluir.

## Bisect

~~~bash
git bisect start
git bisect bad
git bisect good <tag-o-sha>
git bisect run mvn -q -f app/pom.xml test
git bisect reset
~~~

Git realiza una búsqueda binaria. El comando devuelve cero para un commit bueno y otro código para uno malo.

## Laboratorio

Crea ocho commits, introduce una regresión intermedia y localízala con `bisect`. Registra SHA y evidencia.
