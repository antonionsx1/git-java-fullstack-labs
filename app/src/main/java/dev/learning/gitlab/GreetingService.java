package dev.learning.gitlab;

public final class GreetingService {
    public String greet(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return "Hola, " + name.trim() + ". Practiquemos Git con intención.";
    }
}
