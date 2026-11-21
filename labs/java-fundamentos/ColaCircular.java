import java.util.Scanner;

public class ColaCircular {
    private final int[] datos;
    private int frente;
    private int fin;
    private int cantidad;

    public ColaCircular(int capacidad) {
        datos = new int[capacidad];
    }

    public boolean estaVacia() {
        return cantidad == 0;
    }

    public boolean estaLlena() {
        return cantidad == datos.length;
    }

    public void encolar(int valor) {
        if (estaLlena()) {
            throw new IllegalStateException("Cola llena.");
        }
        datos[fin] = valor;
        fin = (fin + 1) % datos.length;
        cantidad++;
    }

    public int desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("Cola vacia.");
        }
        int valor = datos[frente];
        frente = (frente + 1) % datos.length;
        cantidad--;
        return valor;
    }

    public void mostrar() {
        System.out.print("Cola: ");
        for (int i = 0; i < cantidad; i++) {
            int indice = (frente + i) % datos.length;
            System.out.print(datos[indice] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ColaCircular cola = new ColaCircular(5);

        int opcion;
        do {
            System.out.println("\n1. Encolar");
            System.out.println("2. Desencolar");
            System.out.println("3. Mostrar");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");
            opcion = sc.nextInt();

            try {
                switch (opcion) {
                    case 1:
                        System.out.print("Valor: ");
                        cola.encolar(sc.nextInt());
                        break;
                    case 2:
                        System.out.println("Sale: " + cola.desencolar());
                        break;
                    case 3:
                        cola.mostrar();
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (IllegalStateException ex) {
                System.out.println(ex.getMessage());
            }
        } while (opcion != 0);
    }
}
