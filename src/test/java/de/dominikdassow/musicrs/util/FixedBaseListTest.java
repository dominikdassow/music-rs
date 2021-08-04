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

        assertEquals(list.values(), Lists.emptyList());
    }

    @Test
    public void testOnlyBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(1, "B");
        }});

        assertEquals(list.values(), Lists.list("A", "B"));
    }

    @Test
    public void testToString() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        assertEquals(list.toString(), "[A, B]");
    }

    @Test
    public void testAddAfterBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(1, "B");
        }});

        list.add("C");

        assertEquals(list.values(), Lists.list("A", "B", "C"));
    }

    @Test
    public void testAddBeforeBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(1, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(list.values(), Lists.list("C", "A", "B"));
    }

    @Test
    public void testAddBetweenBase() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(list.values(), Lists.list("A", "C", "B"));
    }

    @Test
    public void testAddAfterReset() {
        FixedBaseList<String> list = new FixedBaseList<>(new HashMap<>() {{
            put(0, "A");
            put(2, "B");
        }});

        list.add("C");

        assertEquals(list.values(), Lists.list("A", "C", "B"));

        list.reset();

        assertEquals(list.values(), Lists.list("A", "B"));

        list.add("C");

        assertEquals(list.values(), Lists.list("A", "C", "B"));
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
