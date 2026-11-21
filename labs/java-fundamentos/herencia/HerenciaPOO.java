package herencia;

public class HerenciaPOO {
    public static void main(String[] args) {
        Empleado programadora = new Empleado("Ana Lopez", "Programadora", 35000);
        Gerente gerente = new Gerente("Lilia Castro", "Gerente", 50000, 5000, "Lexus", 1000);

        imprimir(programadora);
        System.out.println();
        imprimir(gerente);
    }

    private static void imprimir(Empleado empleado) {
        empleado.mostrar();
    }
}
