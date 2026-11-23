INSERT INTO curso (id, titulo)
SELECT i, 'Curso ' || i || ' - Java y Spring: APIs, persistencia y pruebas - ' || md5(i::text)
FROM generate_series(2, 5000) AS i;

ANALYZE curso;
