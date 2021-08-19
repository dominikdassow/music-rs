package de.dominikdassow.musicrs.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixedBaseListTest {

    @Test
    public void testEmpty() {
        FixedBaseList<String> list = new FixedBaseList<>();

        assertEquals(List.of(), list.values());
    }

    @Test
    public void testOnlyBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(1, "B");
        }});

        assertEquals(List.of("A", "B"), list.values());
    }

    @Test
    public void testToString() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        assertEquals("[A, B]", list.toString());
    }

    @Test
    public void testAddAfterBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(1, "B");
        }});

        list.add("C");

        assertEquals(List.of("A", "B", "C"), list.values());
    }

    @Test
    public void testAddBeforeBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(1, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(List.of("C", "A", "B"), list.values());
    }

    @Test
    public void testAddBetweenBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(List.of("A", "C", "B"), list.values());
    }

    @Test
    public void testAddAfterReset() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(List.of("A", "C", "B"), list.values());

        list.reset();

        assertEquals(List.of("A", "B"), list.values());

        list.add("C");

        assertEquals(List.of("A", "C", "B"), list.values());
    }

    @Test
    public void testNullKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FixedBaseList<>(new HashMap<>() {{
            put(null, "A");
        }}));
    }

    @Test
    public void testNegativeKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FixedBaseList<>(new HashMap<>() {{
            put(-1, "A");
        }}));
    }
}
