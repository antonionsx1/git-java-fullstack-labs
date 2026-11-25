import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ReordenacionCadenas {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<String> cadenas = new ArrayList<>();

        System.out.println("Introduce cadenas, una por linea. Escribe FIN para terminar.");
        while (true) {
            System.out.print("Cadena: ");
            String texto = sc.nextLine();
            if ("FIN".equalsIgnoreCase(texto)) {
                break;
            }
            cadenas.add(texto);
        }

        System.out.println("\nOriginal:");
        imprimir(cadenas);

        Collections.sort(cadenas);
        System.out.println("\nOrden alfabético:");
        imprimir(cadenas);

        Collections.reverse(cadenas);
        System.out.println("\nOrden inverso:");
        imprimir(cadenas);
    }

    private static void imprimir(List<String> cadenas) {
        if (cadenas.isEmpty()) {
            System.out.println("(sin datos)");
            return;
        }
        for (String cadena : cadenas) {
            System.out.println(cadena);
        }
    }
}
