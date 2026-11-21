import java.util.Scanner;

public class MatricesGaussJordan {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Tamano de la matriz cuadrada: ");
        int n = sc.nextInt();
        double[][] matriz = new double[n][n];

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                System.out.print("A[" + fila + "][" + col + "]: ");
                matriz[fila][col] = sc.nextDouble();
            }
        }

        double[][] inversa = invertir(matriz);
        if (inversa == null) {
            System.out.println("La matriz no tiene inversa.");
            return;
        }

        System.out.println("\nInversa:");
        imprimir(inversa);
    }

    private static double[][] invertir(double[][] matriz) {
        int n = matriz.length;
        double[][] aumentada = new double[n][2 * n];

        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                aumentada[fila][col] = matriz[fila][col];
            }
            aumentada[fila][n + fila] = 1.0;
        }

        for (int pivote = 0; pivote < n; pivote++) {
            int mejorFila = buscarMejorPivote(aumentada, pivote);
            if (Math.abs(aumentada[mejorFila][pivote]) < 1e-10) {
                return null;
            }

            intercambiar(aumentada, pivote, mejorFila);
            normalizarFila(aumentada, pivote, aumentada[pivote][pivote]);

            for (int fila = 0; fila < n; fila++) {
                if (fila != pivote) {
                    eliminarColumna(aumentada, fila, pivote);
                }
            }
        }

        double[][] inversa = new double[n][n];
        for (int fila = 0; fila < n; fila++) {
            for (int col = 0; col < n; col++) {
                inversa[fila][col] = aumentada[fila][n + col];
            }
        }
        return inversa;
    }

    private static int buscarMejorPivote(double[][] m, int columna) {
        int mejor = columna;
        for (int fila = columna + 1; fila < m.length; fila++) {
            if (Math.abs(m[fila][columna]) > Math.abs(m[mejor][columna])) {
                mejor = fila;
            }
        }
        return mejor;
    }

    private static void intercambiar(double[][] m, int a, int b) {
        double[] temp = m[a];
        m[a] = m[b];
        m[b] = temp;
    }

    private static void normalizarFila(double[][] m, int fila, double divisor) {
        for (int col = 0; col < m[fila].length; col++) {
            m[fila][col] /= divisor;
        }
    }

    private static void eliminarColumna(double[][] m, int fila, int pivote) {
        double factor = m[fila][pivote];
        for (int col = 0; col < m[fila].length; col++) {
            m[fila][col] -= factor * m[pivote][col];
        }
    }

    private static void imprimir(double[][] m) {
        for (double[] fila : m) {
            for (double valor : fila) {
                System.out.printf("%10.4f", valor);
            }
            System.out.println();
        }
    }
}
