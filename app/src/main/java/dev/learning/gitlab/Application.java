package dev.learning.gitlab;

public final class Application {
    private Application() {}

    public static void main(String[] args) {
        var name = args.length == 0 ? "equipo" : args[0];
        System.out.println(new GreetingService().greet(name));
    }
}
