# Programas vigentes para estudiar en Java

Esta carpeta contiene versiones modernas en Java de los temas utiles que aparecen en los programas C/C++ originales.
No es una traduccion linea por linea: se quitaron dependencias antiguas como `conio.h`, `iostream.h`, `void main`, `gets`, rutas fijas y menus de Turbo C++.

## Orden recomendado

1. `Fundamentos.java`: entrada, salida, condicionales, ciclos y funciones.
2. `Arreglos.java`: arreglos, promedios, maximos, minimos y recorrido.
3. `MatricesGaussJordan.java`: matrices y metodo de Gauss-Jordan.
4. `Recursividad.java`: factorial y Fibonacci.
5. `BusquedaOrdenamiento.java`: busqueda lineal, busqueda binaria y burbuja.
6. `Pila.java`: estructura LIFO.
7. `ColaCircular.java`: estructura FIFO circular.
8. `ArbolBinario.java`: arbol binario de busqueda y recorridos.
9. `Huffman.java`: codificacion de Huffman con cola de prioridad.
10. `ArchivosRegistros.java`: altas, bajas, busqueda y listado usando archivo de texto.
11. `HerenciaPOO.java`: clases, herencia, encapsulamiento y polimorfismo basico.
12. `ListaArchivo.java`: lee un archivo de texto secuencial y carga sus lineas en una lista grafica.
13. `ReordenacionCadenas.java`: captura cadenas y las ordena alfabeticamente.
14. `SistemaCapacitacion.java`: ejemplo tipo proyecto VB6 `PROY_2`, con login, menu, CRUD, cursos, puestos y reportes en archivos de texto.
15. `MensajesInstituto.java`: salida simple en pantalla, equivalente conceptual a `PRUEBA.ASM`.
16. `OperacionesBasicas.java`: subrutinas para suma, resta, multiplicacion y division.
17. `MensajeEnsamblador.java`: salida simple en pantalla, equivalente conceptual a `uno.asm`.
18. `UPSSInventario.java`: CRUD de inventario por consola con persistencia en archivo de texto, migrado desde `UPSS.CPP`.
19. `AgendaAmigos.java`: agenda CRUD de amigos con persistencia en archivo de texto.
20. `BlogEstudiante.java`: modelo de blog con usuarios, perfiles, categorias, temas, contenido y comentarios.

## Compilar

Desde esta carpeta:

```powershell
javac -d out *.java herencia/*.java
```

Ejecutar un ejemplo:

```powershell
java -cp out Fundamentos
java -cp out herencia.HerenciaPOO
```

Ejecutar el ejemplo equivalente a `ListaArchivo`:

```powershell
java -cp out ListaArchivo People.txt
```

## Que comparar con los originales

- `ARBOL.CPP` -> `ArbolBinario.java`
- `PILAS.CPP`, `PILAPUSH.CPP`, `PILAPOP.CPP` -> `Pila.java`
- `COLAING.CPP`, `COLASCIR.CPP` -> `ColaCircular.java`
- `BURBUJA.CPP`, `ORDENADO.CPP`, `BUS.CPP`, `busca.CPP` -> `BusquedaOrdenamiento.java`
- `FACTREC.CPP`, `FIBOREC.CPP` -> `Recursividad.java`
- `G_JORDAN.C` -> `MatricesGaussJordan.java`
- `ARCHIVO2.CPP`, `FICHERO1.CPP`, `programa1.cpp`, `vaca.cpp` -> `ArchivosRegistros.java`
- `HERENCIA.CPP`, `LAHERENC.CPP` -> `HerenciaPOO.java`
- `ListaArchivo\Listfill.frm` -> `ListaArchivo.java`
- `reoreacion.txt` -> `ReordenacionCadenas.java`
- `PROY_2\PROY_2\Proyecto1.vbp` -> `SistemaCapacitacion.java`
- `PRUEBA.ASM` -> `MensajesInstituto.java`
- `UPSS.CPP` -> `UPSSInventario.java`
- `1.cpp` -> `AgendaAmigos.java`
- `Blog del Estudiante.pptx` -> `BlogEstudiante.java`
