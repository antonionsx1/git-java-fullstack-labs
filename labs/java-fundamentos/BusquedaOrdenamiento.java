import java.util.Arrays;
import java.util.Scanner;

public class BusquedaOrdenamiento {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int[] datos = { 8, 3, 5, 1, 9, 2, 7 };

        System.out.println("Original: " + Arrays.toString(datos));
        ordenarBurbuja(datos);
        System.out.println("Ordenado: " + Arrays.toString(datos));

        System.out.print("Numero a buscar: ");
        int buscado = sc.nextInt();

        System.out.println("Busqueda lineal: " + describir(busquedaLineal(datos, buscado)));
        System.out.println("Busqueda binaria: " + describir(busquedaBinaria(datos, buscado)));
    }

    private static void ordenarBurbuja(int[] datos) {
        boolean huboCambio;
        do {
            huboCambio = false;
            for (int i = 0; i < datos.length - 1; i++) {
                if (datos[i] > datos[i + 1]) {
                    int temp = datos[i];
                    datos[i] = datos[i + 1];
                    datos[i + 1] = temp;
                    huboCambio = true;
                }
            }
        } while (huboCambio);
    }

    private static int busquedaLineal(int[] datos, int valor) {
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] == valor) {
                return i;
            }
        }
        return -1;
    }

    private static int busquedaBinaria(int[] datos, int valor) {
        int izquierda = 0;
        int derecha = datos.length - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            if (datos[medio] == valor) {
                return medio;
            }
            if (datos[medio] < valor) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return -1;
    }

    private static String describir(int posicion) {
        return posicion >= 0 ? "en posicion " + posicion : "no encontrado";
    }
}
