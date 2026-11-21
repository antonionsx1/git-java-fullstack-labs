abstract class Persona {
    private final String nombre;

    Persona(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public abstract void mostrarRol();
}

class Estudiante extends Persona {
    private final String carrera;

    Estudiante(String nombre, String carrera) {
        super(nombre);
        this.carrera = carrera;
    }

    @Override
    public void mostrarRol() {
        System.out.println(getNombre() + " es estudiante de " + carrera + ".");
    }
}

class Empleado extends Persona {
    private final String puesto;

    Empleado(String nombre, String puesto) {
        super(nombre);
        this.puesto = puesto;
    }

    @Override
    public void mostrarRol() {
        System.out.println(getNombre() + " trabaja como " + puesto + ".");
    }
}

public class HerenciaPOO {
    public static void main(String[] args) {
        Persona persona1 = new Estudiante("Jose", "Informatica");
        Persona persona2 = new Empleado("Carlos", "Analista");

        persona1.mostrarRol();
        persona2.mostrarRol();
    }
}