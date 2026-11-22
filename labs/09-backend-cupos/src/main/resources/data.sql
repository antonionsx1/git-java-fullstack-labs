INSERT INTO curso (id, titulo) VALUES (1, 'Java y Spring') ON CONFLICT DO NOTHING;
INSERT INTO cupo (curso_id, disponibles) VALUES (1, 1) ON CONFLICT DO NOTHING;
