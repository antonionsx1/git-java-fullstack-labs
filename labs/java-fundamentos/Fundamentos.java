import java.util.Scanner;

public class Fundamentos {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nombre: ");
        String nombre = sc.nextLine();

        System.out.print("Edad: ");
        int edad = sc.nextInt();

        System.out.print("Calificacion 1: ");
        double c1 = sc.nextDouble();
        System.out.print("Calificacion 2: ");
        double c2 = sc.nextDouble();
        System.out.print("Calificacion 3: ");
        double c3 = sc.nextDouble();

        double promedio = promedio(c1, c2, c3);

        System.out.println("\nAlumno: " + nombre);
        System.out.println("Edad: " + edad);
        System.out.printf("Promedio: %.2f%n", promedio);
        System.out.println(promedio >= 6.0 ? "Resultado: aprobado" : "Resultado: reprobado");

        System.out.println("\nTabla usando la edad:");
        for (int i = 1; i <= 10; i++) {
            System.out.println(i + " x " + edad + " = " + (i * edad));
        }
    }

    private static double promedio(double a, double b, double c) {
        return (a + b + c) / 3.0;
    }
}
