import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProveedoresArchivo {
    private static final Path ARCHIVO = Paths.get("proveedores.txt");
    private static final Path INCORRECTOS = Paths.get("proveedores-incorrectos.txt");

    private static class Proveedor {
        long clave;
        String razonSocial;
        String domicilio;
        String telefono;
        String contacto;
        long credito;

        Proveedor(long clave, String razonSocial, String domicilio, String telefono, String contacto, long credito) {
            this.clave = clave;
            this.razonSocial = razonSocial;
            this.domicilio = domicilio;
            this.telefono = telefono;
            this.contacto = contacto;
            this.credito = credito;
        }

        String serializar() {
            return clave + "|" + escape(razonSocial) + "|" + escape(domicilio) + "|"
                + escape(telefono) + "|" + escape(contacto) + "|" + credito;
        }

        static Proveedor desdeLinea(String linea) {
            String[] partes = linea.split("\\|", -1);
            if (partes.length != 6) {
                throw new IllegalArgumentException("Linea invalida: " + linea);
            }
            return new Proveedor(
                Long.parseLong(partes[0]),
                unescape(partes[1]),
                unescape(partes[2]),
                unescape(partes[3]),
                unescape(partes[4]),
                Long.parseLong(partes[5])
            );
        }

        public String toString() {
            return "Clave: " + clave
                + ", razon social: " + razonSocial
                + ", domicilio: " + domicilio
                + ", telefono: " + telefono
                + ", contacto: " + contacto
                + ", credito: " + credito;
        }

        private static String escape(String valor) {
            return valor.replace("\\", "\\\\").replace("|", "\\|");
        }

        private static String unescape(String valor) {
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = 0; i < valor.length(); i++) {
                char c = valor.charAt(i);
                if (escaped) {
                    sb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else {
                    sb.append(c);
                }
            }
            if (escaped) {
                sb.append('\\');
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8.name());
        int opcion;

        do {
            System.out.println("\nProveedores - Archivo");
            System.out.println("1. Alta");
            System.out.println("2. Baja");
            System.out.println("3. Buscar");
            System.out.println("4. Listar");
            System.out.println("0. Salir");
            opcion = leerEntero(sc, "Opcion: ");

            switch (opcion) {
                case 1:
                    alta(sc);
                    break;
                case 2:
                    baja(sc);
                    break;
                case 3:
                    buscar(sc);
                    break;
                case 4:
                    listar();
                    break;
                case 0:
                    System.out.println("Saliendo.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void alta(Scanner sc) throws IOException {
        List<Proveedor> proveedores = leerProveedores();
        long clave = leerLong(sc, "Clave del proveedor: ");
        if (buscarPorClave(proveedores, clave) != null) {
            Proveedor incorrecto = capturarProveedor(sc, clave);
            guardarIncorrecto(incorrecto);
            System.out.println("La clave ya existe. Se registro el intento en " + INCORRECTOS.toAbsolutePath());
            return;
        }

        Proveedor proveedor = capturarProveedor(sc, clave);
        proveedores.add(proveedor);
        guardarProveedores(proveedores);
        System.out.println("Alta realizada.");
    }

    private static Proveedor capturarProveedor(Scanner sc, long clave) {
        String razonSocial = leerTexto(sc, "Razon social: ");
        String domicilio = leerTexto(sc, "Domicilio: ");
        String telefono = leerTexto(sc, "Telefono: ");
        String contacto = leerTexto(sc, "Contacto: ");
        long credito = leerLong(sc, "Credito: ");
        return new Proveedor(clave, razonSocial, domicilio, telefono, contacto, credito);
    }

    private static void baja(Scanner sc) throws IOException {
        List<Proveedor> proveedores = leerProveedores();
        long clave = leerLong(sc, "Clave del proveedor a eliminar: ");
        boolean eliminado = false;

        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).clave == clave) {
                proveedores.remove(i);
                eliminado = true;
                break;
            }
        }

        if (eliminado) {
            guardarProveedores(proveedores);
            System.out.println("Baja realizada.");
        } else {
            System.out.println("Baja inexistente.");
        }
    }

    private static void buscar(Scanner sc) throws IOException {
        long clave = leerLong(sc, "Clave del proveedor a buscar: ");
        Proveedor proveedor = buscarPorClave(leerProveedores(), clave);
        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
            return;
        }
        System.out.println(proveedor);
    }

    private static void listar() throws IOException {
        List<Proveedor> proveedores = leerProveedores();
        if (proveedores.isEmpty()) {
            System.out.println("No hay proveedores registrados.");
            return;
        }

        for (Proveedor proveedor : proveedores) {
            System.out.println(proveedor);
        }
    }

    private static List<Proveedor> leerProveedores() throws IOException {
        List<Proveedor> proveedores = new ArrayList<Proveedor>();
        if (!Files.exists(ARCHIVO)) {
            return proveedores;
        }

        BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8);
        try {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    proveedores.add(Proveedor.desdeLinea(linea));
                }
            }
        } finally {
            reader.close();
        }
        return proveedores;
    }

    private static void guardarProveedores(List<Proveedor> proveedores) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8);
        try {
            for (Proveedor proveedor : proveedores) {
                writer.write(proveedor.serializar());
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static void guardarIncorrecto(Proveedor proveedor) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(
            INCORRECTOS,
            StandardCharsets.UTF_8,
            Files.exists(INCORRECTOS)
                ? new java.nio.file.OpenOption[] {java.nio.file.StandardOpenOption.APPEND}
                : new java.nio.file.OpenOption[] {java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND}
        );
        try {
            writer.write(proveedor.serializar());
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private static Proveedor buscarPorClave(List<Proveedor> proveedores, long clave) {
        for (Proveedor proveedor : proveedores) {
            if (proveedor.clave == clave) {
                return proveedor;
            }
        }
        return null;
    }

    private static int leerEntero(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Ingresa un entero.");
            }
        }
    }

    private static long leerLong(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Long.parseLong(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Ingresa un numero entero largo.");
            }
        }
    }

    private static String leerTexto(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = sc.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("El texto no puede estar vacio.");
        }
    }
}