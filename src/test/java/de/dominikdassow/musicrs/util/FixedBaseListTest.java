package de.dominikdassow.musicrs.util;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixedBaseListTest {

    @Test
    public void testEmpty() {
        FixedBaseList<String> list = new FixedBaseList<>();

        assertEquals(Lists.emptyList(), list.values());
    }

    @Test
    public void testOnlyBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(1, "B");
        }});

        assertEquals(Lists.list("A", "B"), list.values());
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

        assertEquals(Lists.list("A", "B", "C"), list.values());
    }

    @Test
    public void testAddBeforeBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(1, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(Lists.list("C", "A", "B"), list.values());
    }

    @Test
    public void testAddBetweenBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(Lists.list("A", "C", "B"), list.values());
    }

    @Test
    public void testAddAfterReset() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(Lists.list("A", "C", "B"), list.values());

        list.reset();

        assertEquals(Lists.list("A", "B"), list.values());

        list.add("C");

        assertEquals(Lists.list("A", "C", "B"), list.values());
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
