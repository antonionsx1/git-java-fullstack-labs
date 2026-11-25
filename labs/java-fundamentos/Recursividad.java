import java.util.Scanner;

public class Recursividad {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Numero para factorial: ");
        int n = sc.nextInt();
        System.out.println("Factorial: " + factorial(n));

        System.out.print("Cuantos terminos de Fibonacci? ");
        int terminos = sc.nextInt();
        for (int i = 0; i < terminos; i++) {
            System.out.print(fibonacci(i) + " ");
        }
        System.out.println();
    }

    private static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("No existe factorial de negativos.");
        }
        return n <= 1 ? 1 : n * factorial(n - 1);
    }

    private static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("No existe Fibonacci negativo.");
        }
        return n <= 1 ? n : fibonacci(n - 1) + fibonacci(n - 2);
    }
}
