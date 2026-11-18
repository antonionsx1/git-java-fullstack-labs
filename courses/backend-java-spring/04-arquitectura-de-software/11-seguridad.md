# 11. Seguridad

La primera versión del [laboratorio del último cupo](../../../labs/09-backend-cupos/README.md) escuchaba en loopback y aceptaba `alumnoId` en el POST. Validar que el identificador es positivo no demuestra que corresponda a quien llama. Esta lección diseña un contrato que obtiene el alumno del lado del servidor.

## Identidad y permisos

**Autenticar** es verificar quién llama. **Autorizar** es decidir si esa identidad puede realizar una acción sobre un recurso. Puede haber un token válido y aun así faltar el permiso para inscribirse.

| Operación | Política propuesta |
| --- | --- |
| `GET /cursos` | Cualquier persona puede consultar los datos públicos que publica Catálogo. |
| `POST /cursos/{cursoId}/inscripciones` | Requiere una identidad autenticada, permiso de inscripción y vínculo con un alumno. La API obtiene el `AlumnoId` de ese vínculo, no de un parámetro HTTP. |
| Consultar una inscripción individual, si se añade después | Solo puede verla su titular o una identidad con un permiso explícito. Comprobar el permiso sobre esa inscripción, no solo sobre la ruta. |

En un diseño con Spring Security y tokens JWT, la API valida la firma, el emisor, la audiencia y la vigencia antes de confiar en las credenciales. Una referencia estable para resolver al alumno es el par emisor-sujeto (`iss`, `sub`): el sujeto identifica una cuenta dentro de su emisor, pero no tiene por qué ser el número interno del alumno. El caso de uso Java sigue recibiendo `AlumnoId` y no necesita conocer el token.

## Controles y respuestas

- **Credenciales ausentes o inválidas:** HTTP 401. **Credenciales válidas sin permiso o vínculo requerido:** HTTP 403. **Entrada inválida:** HTTP 400 para una llamada autorizada. **Conflicto de negocio:** HTTP 409 para un duplicado o cupo agotado. Rechazar una solicitud por seguridad no debe descontar un cupo.
- **Menor privilegio:** declarar públicas solo las rutas previstas, denegar lo que no tenga una regla y separar la cuenta que ejecuta migraciones de la cuenta de ejecución cuando se despliegue fuera del laboratorio.
- **Protección de credenciales:** usar HTTPS fuera de loopback y guardar secretos fuera del repositorio. No registrar tokens, cabeceras de autenticación ni datos personales innecesarios. Auditar el tipo de operación, el resultado y un identificador de correlación.
- **Navegador:** CORS regula qué orígenes pueden leer respuestas en un navegador; no reemplaza la autorización. Evaluar CSRF según cómo se envían las credenciales: un navegador adjunta cookies automáticamente, a diferencia de un token que el cliente agrega explícitamente a la cabecera.

## Ejercicio: asegurar el alta sin romper el último cupo

1. Define la política pública y privada en Spring Security. Usa una identidad verificada del lado del servidor para resolver el alumno y elimina `alumnoId` como fuente de identidad del POST. Documenta el cambio de contrato respecto al laboratorio actual.
2. Prueba Catálogo sin credenciales y el POST con token ausente, inválido o sin permiso. Añade una prueba que envíe un identificador ajeno y comprueba que nunca se crea una inscripción para esa otra identidad. En los rechazos, verifica que no cambia el cupo.
3. Repite las pruebas de concurrencia y rollback con dos identidades de prueba distintas para la carrera y la misma identidad para el duplicado. Debe quedar una sola inscripción cuando existe un solo lugar.

**Entregable:** matriz de acceso, pruebas de autorización y evidencia de las garantías de PostgreSQL. La [solución del laboratorio](../../../labs/09-backend-cupos/README.md#probar-la-api) usa un emisor configurado y rechaza `alumnoId` en el POST.
