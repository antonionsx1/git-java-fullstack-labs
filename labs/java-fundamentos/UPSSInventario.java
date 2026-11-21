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

public class UPSSInventario {

    private static final Path ARCHIVO = Paths.get("upss-inventario.txt");

    private static class Articulo {
        int codigo;
        String descripcion;
        int inventarioInicial;
        String fechaAdquisicion;
        double costoUnitario;
        int numeroVentas;
        double precioVenta;
        String clase;

        Articulo(int codigo, String descripcion, int inventarioInicial, String fechaAdquisicion,
                 double costoUnitario, int numeroVentas, double precioVenta, String clase) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.inventarioInicial = inventarioInicial;
            this.fechaAdquisicion = fechaAdquisicion;
            this.costoUnitario = costoUnitario;
            this.numeroVentas = numeroVentas;
            this.precioVenta = precioVenta;
            this.clase = clase;
        }

        String serializar() {
            return codigo + "|" + escapar(descripcion) + "|" + inventarioInicial + "|"
                + escapar(fechaAdquisicion) + "|" + costoUnitario + "|" + numeroVentas + "|"
                + precioVenta + "|" + escapar(clase);
        }

        static Articulo desdeLinea(String linea) {
            String[] partes = linea.split("\\|", -1);
            if (partes.length != 8) {
                throw new IllegalArgumentException("Linea invalida: " + linea);
            }
            return new Articulo(
                Integer.parseInt(partes[0]),
                desescapar(partes[1]),
                Integer.parseInt(partes[2]),
                desescapar(partes[3]),
                Double.parseDouble(partes[4]),
                Integer.parseInt(partes[5]),
                Double.parseDouble(partes[6]),
                desescapar(partes[7])
            );
        }

        public String toString() {
            return "Codigo: " + codigo
                + ", descripcion: " + descripcion
                + ", inventario inicial: " + inventarioInicial
                + ", fecha de adquisicion: " + fechaAdquisicion
                + ", costo unitario: " + costoUnitario
                + ", numero de ventas: " + numeroVentas
                + ", precio de venta: " + precioVenta
                + ", clase: " + clase;
        }

        private static String escapar(String valor) {
            return valor.replace("\\", "\\\\").replace("|", "\\|");
        }

        private static String desescapar(String valor) {
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (int i = 0; i < valor.length(); i++) {
                char c = valor.charAt(i);
                if (escape) {
                    sb.append(c);
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else {
                    sb.append(c);
                }
            }
            if (escape) {
                sb.append('\\');
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8.name());
        int opcion;

        do {
            System.out.println("\nUPP'S - Inventario");
            System.out.println("1. Crear archivo");
            System.out.println("2. Alta");
            System.out.println("3. Baja");
            System.out.println("4. Consulta");
            System.out.println("5. Modificar");
            System.out.println("6. Visualizar");
            System.out.println("0. Salir");
            opcion = leerEntero(sc, "Opcion: ");

            switch (opcion) {
                case 1:
                    crearArchivo();
                    break;
                case 2:
                    alta(sc);
                    break;
                case 3:
                    baja(sc);
                    break;
                case 4:
                    consulta(sc);
                    break;
                case 5:
                    modificar(sc);
                    break;
                case 6:
                    visualizar();
                    break;
                case 0:
                    System.out.println("Saliendo.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    private static void crearArchivo() throws IOException {
        if (Files.exists(ARCHIVO)) {
            System.out.println("El archivo ya existe: " + ARCHIVO.toAbsolutePath());
            return;
        }
        Files.createFile(ARCHIVO);
        System.out.println("Archivo creado: " + ARCHIVO.toAbsolutePath());
    }

    private static void alta(Scanner sc) throws IOException {
        List<Articulo> articulos = leerArticulos();
        int codigo = leerEntero(sc, "Codigo del articulo: ");
        if (buscarPorCodigo(articulos, codigo) != null) {
            System.out.println("Ese codigo ya existe.");
            return;
        }

        String descripcion = leerTexto(sc, "Descripcion: ");
        int inventarioInicial = leerEntero(sc, "Inventario inicial: ");
        String fechaAdquisicion = leerTexto(sc, "Fecha de adquisicion: ");
        double costoUnitario = leerDouble(sc, "Costo unitario: ");
        int numeroVentas = leerEntero(sc, "Numero de ventas: ");
        double precioVenta = leerDouble(sc, "Precio de venta: ");
        String clase = leerTexto(sc, "Clase del articulo: ");

        articulos.add(new Articulo(codigo, descripcion, inventarioInicial, fechaAdquisicion,
            costoUnitario, numeroVentas, precioVenta, clase));
        guardarArticulos(articulos);
        System.out.println("Articulo agregado.");
    }

    private static void baja(Scanner sc) throws IOException {
        List<Articulo> articulos = leerArticulos();
        int codigo = leerEntero(sc, "Codigo del articulo a eliminar: ");
        boolean eliminado = false;

        for (int i = 0; i < articulos.size(); i++) {
            if (articulos.get(i).codigo == codigo) {
                articulos.remove(i);
                eliminado = true;
                break;
            }
        }

        if (eliminado) {
            guardarArticulos(articulos);
            System.out.println("Articulo eliminado.");
        } else {
            System.out.println("No se encontro el articulo.");
        }
    }

    private static void consulta(Scanner sc) throws IOException {
        Articulo articulo = buscarPorCodigo(leerArticulos(), leerEntero(sc, "Codigo del articulo a consultar: "));
        if (articulo == null) {
            System.out.println("No se encontro el articulo.");
            return;
        }
        System.out.println(articulo);
    }

    private static void modificar(Scanner sc) throws IOException {
        List<Articulo> articulos = leerArticulos();
        int codigo = leerEntero(sc, "Codigo del articulo a modificar: ");
        Articulo existente = buscarPorCodigo(articulos, codigo);
        if (existente == null) {
            System.out.println("No se encontro el articulo.");
            return;
        }

        System.out.println("Articulo actual:");
        System.out.println(existente);

        String descripcion = leerTexto(sc, "Nueva descripcion: ");
        int inventarioInicial = leerEntero(sc, "Nuevo inventario inicial: ");
        String fechaAdquisicion = leerTexto(sc, "Nueva fecha de adquisicion: ");
        double costoUnitario = leerDouble(sc, "Nuevo costo unitario: ");
        int numeroVentas = leerEntero(sc, "Nuevo numero de ventas: ");
        double precioVenta = leerDouble(sc, "Nuevo precio de venta: ");
        String clase = leerTexto(sc, "Nueva clase: ");

        existente.descripcion = descripcion;
        existente.inventarioInicial = inventarioInicial;
        existente.fechaAdquisicion = fechaAdquisicion;
        existente.costoUnitario = costoUnitario;
        existente.numeroVentas = numeroVentas;
        existente.precioVenta = precioVenta;
        existente.clase = clase;

        guardarArticulos(articulos);
        System.out.println("Articulo modificado.");
    }

    private static void visualizar() throws IOException {
        List<Articulo> articulos = leerArticulos();
        if (articulos.isEmpty()) {
            System.out.println("No hay articulos registrados.");
            return;
        }
        for (Articulo articulo : articulos) {
            System.out.println(articulo);
        }
    }

    private static List<Articulo> leerArticulos() throws IOException {
        List<Articulo> articulos = new ArrayList<Articulo>();
        if (!Files.exists(ARCHIVO)) {
            return articulos;
        }

        BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8);
        try {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    articulos.add(Articulo.desdeLinea(linea));
                }
            }
        } finally {
            reader.close();
        }
        return articulos;
    }

    private static void guardarArticulos(List<Articulo> articulos) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8);
        try {
            for (Articulo articulo : articulos) {
                writer.write(articulo.serializar());
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    private static Articulo buscarPorCodigo(List<Articulo> articulos, int codigo) {
        for (Articulo articulo : articulos) {
            if (articulo.codigo == codigo) {
                return articulo;
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

    private static double leerDouble(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido. Ingresa un numero.");
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
