import java.util.Scanner;

public class OperacionesBasicas {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Numero 1: ");
        int num1 = sc.nextInt();
        System.out.print("Numero 2: ");
        int num2 = sc.nextInt();

        System.out.println("Suma: " + sumar(num1, num2));
        System.out.println("Resta: " + restar(num1, num2));
        System.out.println("Multiplicacion: " + multiplicar(num1, num2));

        if (num2 == 0) {
            System.out.println("Division: no se puede dividir entre cero");
        } else {
            System.out.printf("Division: %.4f%n", dividir(num1, num2));
        }
    }

    private static int sumar(int a, int b) {
        return a + b;
    }

    private static int restar(int a, int b) {
        return a - b;
    }

    private static int multiplicar(int a, int b) {
        return a * b;
    }

    private static double dividir(int a, int b) {
        return (double) a / b;
    }
}
