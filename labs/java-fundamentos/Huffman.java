import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Huffman {
    private static class Nodo implements Comparable<Nodo> {
        char simbolo;
        int frecuencia;
        Nodo izquierda;
        Nodo derecha;

        Nodo(char simbolo, int frecuencia) {
            this.simbolo = simbolo;
            this.frecuencia = frecuencia;
        }

        Nodo(Nodo izquierda, Nodo derecha) {
            this.simbolo = '\0';
            this.frecuencia = izquierda.frecuencia + derecha.frecuencia;
            this.izquierda = izquierda;
            this.derecha = derecha;
        }

        boolean esHoja() {
            return izquierda == null && derecha == null;
        }

        public int compareTo(Nodo otro) {
            return Integer.compare(this.frecuencia, otro.frecuencia);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Texto para codificar: ");
        String texto = sc.nextLine();
        if (texto.isEmpty()) {
            System.out.println("Texto vacio.");
            return;
        }

        Map<Character, Integer> frecuencias = contarFrecuencias(texto);
        Nodo raiz = construirArbol(frecuencias);
        Map<Character, String> codigos = new HashMap<Character, String>();
        generarCodigos(raiz, "", codigos);

        System.out.println("\nCodigos:");
        for (Map.Entry<Character, String> entry : codigos.entrySet()) {
            System.out.println("'" + entry.getKey() + "': " + entry.getValue());
        }

        StringBuilder codificado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            codificado.append(codigos.get(texto.charAt(i)));
        }
        System.out.println("\nTexto codificado:");
        System.out.println(codificado);
    }

    private static Map<Character, Integer> contarFrecuencias(String texto) {
        Map<Character, Integer> frecuencias = new HashMap<Character, Integer>();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            Integer actual = frecuencias.get(c);
            frecuencias.put(c, actual == null ? 1 : actual + 1);
        }
        return frecuencias;
    }

    private static Nodo construirArbol(Map<Character, Integer> frecuencias) {
        PriorityQueue<Nodo> cola = new PriorityQueue<Nodo>();
        for (Map.Entry<Character, Integer> entry : frecuencias.entrySet()) {
            cola.add(new Nodo(entry.getKey(), entry.getValue()));
        }

        while (cola.size() > 1) {
            cola.add(new Nodo(cola.poll(), cola.poll()));
        }
        return cola.poll();
    }

    private static void generarCodigos(Nodo nodo, String codigo, Map<Character, String> codigos) {
        if (nodo.esHoja()) {
            codigos.put(nodo.simbolo, codigo.isEmpty() ? "0" : codigo);
            return;
        }
        generarCodigos(nodo.izquierda, codigo + "0", codigos);
        generarCodigos(nodo.derecha, codigo + "1", codigos);
    }
}
