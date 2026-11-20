package dev.learning.gitlab;

public final class GreetingService {
    /**
     * Builds the message shown by the learning application.
     *
     * @param name person or team receiving the greeting
     * @return a normalized greeting
     */
    public String greet(String name) {
        requireName(name);
        return "Hola, " + name.trim() + ". Practiquemos Git con intención.";
    }

    private void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
