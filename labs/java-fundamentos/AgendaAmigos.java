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

public class AgendaAmigos {
    private static final File ARCHIVO = new File("agenda-amigos.txt");

    private static class Amigo {
        String nombre;
        int edad;
        String telefono;
        String direccion;

        Amigo(String nombre, int edad, String telefono, String direccion) {
            this.nombre = nombre;
            this.edad = edad;
            this.telefono = telefono;
            this.direccion = direccion;
        }

        String serializar() {
            return limpiar(nombre) + "|" + edad + "|" + limpiar(telefono) + "|" + limpiar(direccion);
        }

        static Amigo desdeLinea(String linea) {
            String[] p = linea.split("\\|", -1);
            return new Amigo(p[0], Integer.parseInt(p[1]), p[2], p[3]);
        }

        public String toString() {
            return "Nombre: " + nombre + " | Edad: " + edad + " | Telefono: " + telefono
                    + " | Direccion: " + direccion;
        }

        private static String limpiar(String texto) {
            return texto.replace('|', '/');
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\nAGENDA DE AMIGOS");
            System.out.println("1. Crear archivo");
            System.out.println("2. Agregar");
            System.out.println("3. Eliminar");
            System.out.println("4. Listar");
            System.out.println("5. Buscar");
            System.out.println("6. Modificar");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = leerEntero(sc);

            switch (opcion) {
                case 1:
                    crearArchivo();
                    break;
                case 2:
                    agregar(sc);
                    break;
                case 3:
                    eliminar(sc);
                    break;
                case 4:
                    listar();
                    break;
                case 5:
                    buscar(sc);
                    break;
                case 6:
                    modificar(sc);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void crearArchivo() throws IOException {
        guardar(new ArrayList<Amigo>());
        System.out.println("Archivo creado desde cero.");
    }

    private static void agregar(Scanner sc) throws IOException {
        List<Amigo> amigos = leer();
        amigos.add(capturar(sc));
        guardar(amigos);
        System.out.println("Amigo agregado.");
    }

    private static void eliminar(Scanner sc) throws IOException {
        System.out.print("Nombre a eliminar: ");
        String nombre = sc.nextLine();
        List<Amigo> amigos = leer();
        boolean eliminado = false;

        Iterator<Amigo> it = amigos.iterator();
        while (it.hasNext()) {
            if (it.next().nombre.equalsIgnoreCase(nombre)) {
                it.remove();
                eliminado = true;
                break;
            }
        }

        guardar(amigos);
        System.out.println(eliminado ? "Registro eliminado." : "No encontrado.");
    }

    private static void listar() throws IOException {
        List<Amigo> amigos = leer();
        if (amigos.isEmpty()) {
            System.out.println("Agenda vacia.");
            return;
        }
        for (int i = 0; i < amigos.size(); i++) {
            System.out.println((i + 1) + ". " + amigos.get(i));
        }
    }

    private static void buscar(Scanner sc) throws IOException {
        System.out.print("Nombre a buscar: ");
        String nombre = sc.nextLine();
        for (Amigo amigo : leer()) {
            if (amigo.nombre.equalsIgnoreCase(nombre)) {
                System.out.println(amigo);
                return;
            }
        }
        System.out.println("No encontrado.");
    }

    private static void modificar(Scanner sc) throws IOException {
        System.out.print("Nombre a modificar: ");
        String nombre = sc.nextLine();
        List<Amigo> amigos = leer();

        for (int i = 0; i < amigos.size(); i++) {
            if (amigos.get(i).nombre.equalsIgnoreCase(nombre)) {
                System.out.println("Registro actual: " + amigos.get(i));
                amigos.set(i, capturar(sc));
                guardar(amigos);
                System.out.println("Registro modificado.");
                return;
            }
        }

        System.out.println("No encontrado.");
    }

    private static Amigo capturar(Scanner sc) {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Edad: ");
        int edad = leerEntero(sc);
        System.out.print("Telefono: ");
        String telefono = sc.nextLine();
        System.out.print("Direccion: ");
        String direccion = sc.nextLine();
        return new Amigo(nombre, edad, telefono, direccion);
    }

    private static List<Amigo> leer() throws IOException {
        List<Amigo> amigos = new ArrayList<Amigo>();
        if (!ARCHIVO.exists()) {
            return amigos;
        }

        BufferedReader reader = new BufferedReader(new FileReader(ARCHIVO));
        try {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    amigos.add(Amigo.desdeLinea(linea));
                }
            }
        } finally {
            reader.close();
        }
        return amigos;
    }

    private static void guardar(List<Amigo> amigos) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO));
        try {
            for (Amigo amigo : amigos) {
                writer.write(amigo.serializar());
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
                System.out.print("Introduce un numero valido: ");
            }
        }
    }
}
