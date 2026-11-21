import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class Pila {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Deque<Integer> pila = new ArrayDeque<Integer>();

        int opcion;
        do {
            System.out.println("\n1. Push");
            System.out.println("2. Pop");
            System.out.println("3. Ver cima");
            System.out.println("4. Mostrar");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = sc.nextInt();

            switch (opcion) {
                case 1:
                    System.out.print("Valor: ");
                    pila.push(sc.nextInt());
                    break;
                case 2:
                    System.out.println(pila.isEmpty() ? "Pila vacia" : "Sale: " + pila.pop());
                    break;
                case 3:
                    System.out.println(pila.isEmpty() ? "Pila vacia" : "Cima: " + pila.peek());
                    break;
                case 4:
                    System.out.println("Pila: " + pila);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }
}
