package herencia;

public class Gerente extends Empleado {
    private final double gratificacionAnual;
    private final String automovilCompania;
    private final int acciones;

    public Gerente(String nombre, String puesto, double salario, double gratificacionAnual,
            String automovilCompania, int acciones) {
        super(nombre, puesto, salario);
        this.gratificacionAnual = gratificacionAnual;
        this.automovilCompania = automovilCompania;
        this.acciones = acciones;
    }

    @Override
    public void mostrar() {
        super.mostrar();
        System.out.println("Gratificacion anual: $" + gratificacionAnual);
        System.out.println("Automovil de compania: " + automovilCompania);
        System.out.println("Acciones: " + acciones);
    }
}
