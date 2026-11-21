import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class SistemaCapacitacion {
    private static final String PASSWORD = "13";
    private static final File CARPETA = new File("proy2-data");
    private static final File EMPLEADOS = new File(CARPETA, "empleados.txt");
    private static final File CURSOS = new File(CARPETA, "cursos.txt");
    private static final File PUESTOS = new File(CARPETA, "puestos.txt");

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        asegurarCarpeta();

        if (!login(sc)) {
            System.out.println("Acceso denegado.");
            return;
        }

        int opcion;
        do {
            System.out.println("\nSISTEMA DE CAPACITACIÓN");
            System.out.println("1. Datos generales de empleados");
            System.out.println("2. Cursos por empleado");
            System.out.println("3. Descripción de puestos");
            System.out.println("4. Reporte general");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = leerEntero(sc);

            switch (opcion) {
                case 1:
                    menuEmpleados(sc);
                    break;
                case 2:
                    menuCursos(sc);
                    break;
                case 3:
                    menuPuestos(sc);
                    break;
                case 4:
                    reporteGeneral();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no valida.");
            }
        } while (opcion != 0);
    }

    private static boolean login(Scanner sc) {
        System.out.print("Password: ");
        return PASSWORD.equals(sc.nextLine().trim());
    }

    private static void menuEmpleados(Scanner sc) throws IOException {
        int opcion;
        do {
            System.out.println("\nDATOS GENERALES");
            System.out.println("1. Nuevo");
            System.out.println("2. Listar");
            System.out.println("3. Buscar");
            System.out.println("4. Modificar");
            System.out.println("5. Eliminar");
            System.out.println("0. Regresar");
            System.out.print("Opción: ");
            opcion = leerEntero(sc);

            switch (opcion) {
                case 1:
                    agregarEmpleado(sc);
                    break;
                case 2:
                    listarEmpleados();
                    break;
                case 3:
                    buscarEmpleado(sc);
                    break;
                case 4:
                    modificarEmpleado(sc);
                    break;
                case 5:
                    eliminarEmpleado(sc);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void menuCursos(Scanner sc) throws IOException {
        int opcion;
        do {
            System.out.println("\nCURSOS");
            System.out.println("1. Registrar curso");
            System.out.println("2. Listar cursos");
            System.out.println("3. Buscar por empleado");
            System.out.println("0. Regresar");
            System.out.print("Opcion: ");
            opcion = leerEntero(sc);

            switch (opcion) {
                case 1:
                    agregarCurso(sc);
                    break;
                case 2:
                    listarCursos();
                    break;
                case 3:
                    buscarCursosPorEmpleado(sc);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void menuPuestos(Scanner sc) throws IOException {
        int opcion;
        do {
            System.out.println("\nDESCRIPCION DE PUESTOS");
            System.out.println("1. Registrar puesto");
            System.out.println("2. Listar puestos");
            System.out.println("3. Buscar puesto");
            System.out.println("0. Regresar");
            System.out.print("Opcion: ");
            opcion = leerEntero(sc);

            switch (opcion) {
                case 1:
                    agregarPuesto(sc);
                    break;
                case 2:
                    listarLineas(PUESTOS);
                    break;
                case 3:
                    buscarTextoEnArchivo(sc, PUESTOS, "Puesto a buscar: ");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void agregarEmpleado(Scanner sc) throws IOException {
        System.out.print("CVE empleado: ");
        String clave = sc.nextLine();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Escolaridad: ");
        String escolaridad = sc.nextLine();
        System.out.print("Nombre del puesto: ");
        String puesto = sc.nextLine();
        System.out.print("Departamento: ");
        String departamento = sc.nextLine();
        System.out.print("CVE departamento: ");
        String claveDepartamento = sc.nextLine();
        System.out.print("Fecha de nacimiento: ");
        String nacimiento = sc.nextLine();
        System.out.print("Fecha de alta: ");
        String alta = sc.nextLine();

        agregarLinea(EMPLEADOS, unir(clave, nombre, escolaridad, puesto, departamento,
                claveDepartamento, nacimiento, alta));
        System.out.println("Empleado guardado.");
    }

    private static void listarEmpleados() throws IOException {
        List<String> empleados = leerLineas(EMPLEADOS);
        if (empleados.isEmpty()) {
            System.out.println("No hay empleados.");
            return;
        }

        for (String linea : empleados) {
            String[] p = separar(linea, 8);
            System.out.println("CVE: " + p[0] + " | Nombre: " + p[1] + " | Puesto: " + p[3]
                    + " | Depto: " + p[4]);
        }
    }

    private static void buscarEmpleado(Scanner sc) throws IOException {
        buscarTextoEnArchivo(sc, EMPLEADOS, "Nombre o CVE a buscar: ");
    }

    private static void modificarEmpleado(Scanner sc) throws IOException {
        System.out.print("CVE empleado a modificar: ");
        String clave = sc.nextLine();
        List<String> empleados = leerLineas(EMPLEADOS);
        boolean modificado = false;

        for (int i = 0; i < empleados.size(); i++) {
            String[] p = separar(empleados.get(i), 8);
            if (p[0].equalsIgnoreCase(clave)) {
                System.out.println("Registro actual: " + empleados.get(i));
                System.out.println("Captura los nuevos datos.");
                System.out.print("Nombre: ");
                p[1] = sc.nextLine();
                System.out.print("Escolaridad: ");
                p[2] = sc.nextLine();
                System.out.print("Nombre del puesto: ");
                p[3] = sc.nextLine();
                System.out.print("Departamento: ");
                p[4] = sc.nextLine();
                System.out.print("CVE departamento: ");
                p[5] = sc.nextLine();
                System.out.print("Fecha de nacimiento: ");
                p[6] = sc.nextLine();
                System.out.print("Fecha de alta: ");
                p[7] = sc.nextLine();
                empleados.set(i, unir(p));
                modificado = true;
                break;
            }
        }

        guardarLineas(EMPLEADOS, empleados);
        System.out.println(modificado ? "Empleado modificado." : "Empleado no encontrado.");
    }

    private static void eliminarEmpleado(Scanner sc) throws IOException {
        System.out.print("CVE empleado a eliminar: ");
        String clave = sc.nextLine();
        List<String> empleados = leerLineas(EMPLEADOS);
        boolean eliminado = false;

        Iterator<String> it = empleados.iterator();
        while (it.hasNext()) {
            String[] p = separar(it.next(), 8);
            if (p[0].equalsIgnoreCase(clave)) {
                it.remove();
                eliminado = true;
                break;
            }
        }

        guardarLineas(EMPLEADOS, empleados);
        System.out.println(eliminado ? "Empleado eliminado." : "Empleado no encontrado.");
    }

    private static void agregarCurso(Scanner sc) throws IOException {
        System.out.print("CVE empleado: ");
        String claveEmpleado = sc.nextLine();
        System.out.print("Nombre del empleado: ");
        String nombre = sc.nextLine();
        System.out.print("Curso: ");
        String curso = sc.nextLine();
        System.out.print("Fecha cursada: ");
        String fecha = sc.nextLine();

        agregarLinea(CURSOS, unir(claveEmpleado, nombre, curso, fecha));
        System.out.println("Curso guardado.");
    }

    private static void listarCursos() throws IOException {
        listarLineas(CURSOS);
    }

    private static void buscarCursosPorEmpleado(Scanner sc) throws IOException {
        buscarTextoEnArchivo(sc, CURSOS, "Empleado o CVE a buscar: ");
    }

    private static void agregarPuesto(Scanner sc) throws IOException {
        System.out.print("Nombre del puesto: ");
        String puesto = sc.nextLine();
        List<String> cursos = new ArrayList<String>();
        for (int i = 1; i <= 10; i++) {
            System.out.print("Curso " + i + " (Enter para omitir): ");
            String curso = sc.nextLine();
            if (!curso.trim().isEmpty()) {
                cursos.add(curso);
            }
        }

        agregarLinea(PUESTOS, puesto + "|" + unir(cursos.toArray(new String[cursos.size()])));
        System.out.println("Puesto guardado.");
    }

    private static void reporteGeneral() throws IOException {
        System.out.println("\nEMPLEADOS");
        listarEmpleados();
        System.out.println("\nCURSOS");
        listarLineas(CURSOS);
        System.out.println("\nPUESTOS");
        listarLineas(PUESTOS);
    }

    private static void listarLineas(File archivo) throws IOException {
        List<String> lineas = leerLineas(archivo);
        if (lineas.isEmpty()) {
            System.out.println("No hay registros.");
            return;
        }
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }

    private static void buscarTextoEnArchivo(Scanner sc, File archivo, String prompt) throws IOException {
        System.out.print(prompt);
        String texto = sc.nextLine().toLowerCase();
        boolean encontrado = false;

        for (String linea : leerLineas(archivo)) {
            if (linea.toLowerCase().contains(texto)) {
                System.out.println(linea);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No encontrado.");
        }
    }

    private static void asegurarCarpeta() {
        if (!CARPETA.exists()) {
            CARPETA.mkdirs();
        }
    }

    private static void agregarLinea(File archivo, String linea) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true));
        try {
            writer.write(linea);
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private static List<String> leerLineas(File archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        if (!archivo.exists()) {
            return lineas;
        }

        BufferedReader reader = new BufferedReader(new FileReader(archivo));
        try {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } finally {
            reader.close();
        }
        return lineas;
    }

    private static void guardarLineas(File archivo, List<String> lineas) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(archivo));
        try {
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static int leerEntero(Scanner sc) {
        while (true) {
            String texto = sc.nextLine();
            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                System.out.print("Introduce un numero: ");
            }
        }
    }

    private static String unir(String... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(valores[i].replace('|', '/'));
        }
        return sb.toString();
    }

    private static String[] separar(String linea, int minimo) {
        String[] partes = linea.split("\\|", -1);
        if (partes.length >= minimo) {
            return partes;
        }

        String[] completa = new String[minimo];
        for (int i = 0; i < minimo; i++) {
            completa[i] = i < partes.length ? partes[i] : "";
        }
        return completa;
    }
}
