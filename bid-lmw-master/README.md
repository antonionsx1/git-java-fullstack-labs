# BID - Latin Macro Watch

Backend for BID - Latin Macro Watch

## Project setup

Run the following commands at the directory level of the **pom.xml** file.

### Download third party dependencies

```
mvnw clean install
```

### Generate the JAR executable

```
mvnw clean package
```

The generated JAR file **lmw-0-0-1.jar** will be in the **target/** directory.

### Copy files to server via SSH

```
scp -i BID.pem <fileName> <user>@<ip-server>:/home/<user>
```

**BID.pem**, is the key in Pem format for accessing the server via SSH.

Copy the following files to the server with the above command

- lmw-0-0-1.jar
- Dockerfile
- docker-compose.yml

### Log in to the server with SSH

```
ssh -i "BID.pem" <user>@<ip-server>
```

### Run docker compose

```
docker-compose up -d
```

### Review installation

```
docker ps
```

## Status Codes 

### Database API


| Nombre               | Descripción                                    | Valor |
|----------------------|------------------------------------------------|-------|
| OK                   | Ok                                             | 100   |
| DB_COLUMNS_MISMATCH  | Faltan las columnas requeridas lmwUUID o lang. | 101   |
| DB_BACKUP_ERROR      | Error al crear archivo de respaldo.            | 102   |
| DB_BACKUP_NOT_FOUND  | Archivo de respaldo no encontrado.             | 103   |
| DB_REPLACEMENT_ERROR | Error al reemplazar los datos.                 | 104   |
| DB_FILE_NOT_FOUND    | Archivo de datos no encontrado.                | 105   |
| DB_TABLE_NOT_FOUND   | Tabla de datos no encontrada.                  | 106   |
| DB_UPDATE_ERROR      | Error al actualizar los datos.                 | 107   |
| DB_BAD_QUERY_REQUEST | No fue posible generar una consulta valida.    | 108   |
| DB_SAVE_ERROR        | Error con el guardado de archivo.              | 109   |
