import java.util.Scanner;

public class ArbolBinario {
    private Nodo raiz;

    private static class Nodo {
        int valor;
        Nodo izquierda;
        Nodo derecha;

        Nodo(int valor) {
            this.valor = valor;
        }
    }

    public void insertar(int valor) {
        raiz = insertar(raiz, valor);
    }

    private Nodo insertar(Nodo actual, int valor) {
        if (actual == null) {
            return new Nodo(valor);
        }
        if (valor < actual.valor) {
            actual.izquierda = insertar(actual.izquierda, valor);
        } else if (valor > actual.valor) {
            actual.derecha = insertar(actual.derecha, valor);
        }
        return actual;
    }

    public boolean buscar(int valor) {
        return buscar(raiz, valor);
    }

    private boolean buscar(Nodo actual, int valor) {
        if (actual == null) {
            return false;
        }
        if (actual.valor == valor) {
            return true;
        }
        return valor < actual.valor ? buscar(actual.izquierda, valor) : buscar(actual.derecha, valor);
    }

    public void preorden() {
        preorden(raiz);
        System.out.println();
    }

    private void preorden(Nodo actual) {
        if (actual != null) {
            System.out.print(actual.valor + " ");
            preorden(actual.izquierda);
            preorden(actual.derecha);
        }
    }

    public void inorden() {
        inorden(raiz);
        System.out.println();
    }

    private void inorden(Nodo actual) {
        if (actual != null) {
            inorden(actual.izquierda);
            System.out.print(actual.valor + " ");
            inorden(actual.derecha);
        }
    }

    public void postorden() {
        postorden(raiz);
        System.out.println();
    }

    private void postorden(Nodo actual) {
        if (actual != null) {
            postorden(actual.izquierda);
            postorden(actual.derecha);
            System.out.print(actual.valor + " ");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArbolBinario arbol = new ArbolBinario();

        System.out.print("Cuantos valores deseas insertar? ");
        int n = sc.nextInt();
        for (int i = 0; i < n; i++) {
            System.out.print("Valor " + (i + 1) + ": ");
            arbol.insertar(sc.nextInt());
        }

        System.out.print("Preorden: ");
        arbol.preorden();
        System.out.print("Inorden: ");
        arbol.inorden();
        System.out.print("Postorden: ");
        arbol.postorden();

        System.out.print("Valor a buscar: ");
        int buscado = sc.nextInt();
        System.out.println(arbol.buscar(buscado) ? "Encontrado" : "No encontrado");
    }
}
