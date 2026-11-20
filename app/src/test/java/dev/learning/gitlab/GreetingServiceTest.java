package dev.learning.gitlab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class GreetingServiceTest {
    private final GreetingService service = new GreetingService();

    @Test
    void greetsAndTrimsTheName() {
        assertEquals("Hola, Ada. Practiquemos Git con intención.", service.greet("  Ada  "));
    }

    @Test
    void rejectsBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> service.greet(" "));
    }
}
