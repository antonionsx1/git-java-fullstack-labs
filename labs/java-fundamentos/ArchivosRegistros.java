import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArchivosRegistros {
    private static final File ARCHIVO = new File("alumnos.txt");

    private static class Alumno {
        int id;
        String nombre;
        double promedio;

        Alumno(int id, String nombre, double promedio) {
            this.id = id;
            this.nombre = nombre;
            this.promedio = promedio;
        }

        String serializar() {
            return id + "|" + nombre + "|" + promedio;
        }

        static Alumno desdeLinea(String linea) {
            String[] partes = linea.split("\\|", -1);
            return new Alumno(Integer.parseInt(partes[0]), partes[1], Double.parseDouble(partes[2]));
        }

        public String toString() {
            return "ID: " + id + ", nombre: " + nombre + ", promedio: " + promedio;
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n1. Alta");
            System.out.println("2. Listar");
            System.out.println("3. Buscar");
            System.out.println("4. Baja");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = Integer.parseInt(sc.nextLine());

            switch (opcion) {
                case 1:
                    alta(sc);
                    break;
                case 2:
                    listar();
                    break;
                case 3:
                    buscar(sc);
                    break;
                case 4:
                    baja(sc);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void alta(Scanner sc) throws IOException {
        System.out.print("ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Promedio: ");
        double promedio = Double.parseDouble(sc.nextLine());

        List<Alumno> alumnos = leerAlumnos();
        alumnos.add(new Alumno(id, nombre, promedio));
        guardarAlumnos(alumnos);
    }

    private static void listar() throws IOException {
        List<Alumno> alumnos = leerAlumnos();
        if (alumnos.isEmpty()) {
            System.out.println("No hay registros.");
            return;
        }
        for (Alumno alumno : alumnos) {
            System.out.println(alumno);
        }
    }

    private static void buscar(Scanner sc) throws IOException {
        System.out.print("ID a buscar: ");
        int id = Integer.parseInt(sc.nextLine());
        for (Alumno alumno : leerAlumnos()) {
            if (alumno.id == id) {
                System.out.println(alumno);
                return;
            }
        }
        System.out.println("No encontrado.");
    }

    private static void baja(Scanner sc) throws IOException {
        System.out.print("ID a eliminar: ");
        int id = Integer.parseInt(sc.nextLine());
        List<Alumno> alumnos = leerAlumnos();
        boolean eliminado = false;

        for (int i = 0; i < alumnos.size(); i++) {
            if (alumnos.get(i).id == id) {
                alumnos.remove(i);
                eliminado = true;
                break;
            }
        }

        guardarAlumnos(alumnos);
        System.out.println(eliminado ? "Registro eliminado." : "No encontrado.");
    }

    private static List<Alumno> leerAlumnos() throws IOException {
        List<Alumno> alumnos = new ArrayList<Alumno>();
        if (!ARCHIVO.exists()) {
            return alumnos;
        }

        BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO));
        try {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    alumnos.add(Alumno.desdeLinea(linea));
                }
            }
        } finally {
            reader.close();
        }
        return alumnos;
    }

    private static void guardarAlumnos(List<Alumno> alumnos) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO));
        try {
            for (Alumno alumno : alumnos) {
                writer.write(alumno.serializar());
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }
}
