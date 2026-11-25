import java.util.Scanner;

public class Arreglos {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Cuantos numeros deseas capturar? ");
        int n = sc.nextInt();
        int[] numeros = new int[n];

        for (int i = 0; i < numeros.length; i++) {
            System.out.print("Numero " + (i + 1) + ": ");
            numeros[i] = sc.nextInt();
        }

        System.out.println("\nDatos capturados:");
        imprimir(numeros);
        System.out.println("Suma: " + suma(numeros));
        System.out.printf("Promedio: %.2f%n", promedio(numeros));
        System.out.println("Mayor: " + mayor(numeros));
        System.out.println("Menor: " + menor(numeros));
    }

    private static void imprimir(int[] datos) {
        for (int dato : datos) {
            System.out.print(dato + " ");
        }
        System.out.println();
    }

    private static int suma(int[] datos) {
        int total = 0;
        for (int dato : datos) {
            total += dato;
        }
        return total;
    }

    private static double promedio(int[] datos) {
        return datos.length == 0 ? 0 : (double) suma(datos) / datos.length;
    }

    private static int mayor(int[] datos) {
        int mayor = datos[0];
        for (int dato : datos) {
            if (dato > mayor) {
                mayor = dato;
            }
        }
        return mayor;
    }

    private static int menor(int[] datos) {
        int menor = datos[0];
        for (int dato : datos) {
            if (dato < menor) {
                menor = dato;
            }
        }
        return menor;
    }
}
