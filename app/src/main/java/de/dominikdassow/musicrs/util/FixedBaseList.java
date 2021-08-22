package de.dominikdassow.musicrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FixedBaseList represents a List that has base elements at fixed positions
 * and can be filled with additional elements at the remaining empty positions.
 * <p>
 * The positions must be positive, non-null Integers.
 *
 * @param <T> The type of List elements
 */
public class FixedBaseList<T> {

    private final Map<Integer, T> base;
    private final Map<Integer, T> elements;

    private int currentIndex = 0;

    /**
     * Creates a List without any base elements.
     */
    public FixedBaseList() {
        this.base = new ConcurrentHashMap<>();
        this.elements = new ConcurrentHashMap<>();
    }

    /**
     * Creates a List with the specifies base elements.
     *
     * @param base The base elements with their fixed positions.
     */
    public FixedBaseList(Map<Integer, T> base) {
        if (base.containsKey(null)) throw new IllegalArgumentException();
        if (base.keySet().stream().anyMatch(key -> key < 0)) throw new IllegalArgumentException();

        this.base = new ConcurrentHashMap<>(base);
        this.elements = new ConcurrentHashMap<>(base);
    }

//    /**
//     * Creates a List with the specifies base elements.
//     *
//     * @param base The base elements with their fixed positions.
//     */
//    public FixedBaseList(FixedBaseList<T> baseList, List<T> elements) {
//        this.base = new ConcurrentHashMap<>(baseList.base);
//        this.elements = new ConcurrentHashMap<>(base);
//    }

    /**
     * Adds an element to the List at the first position that's not taken by any base element.
     *
     * @param e The element to add
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(T e) {
        while (elements.containsKey(currentIndex)) currentIndex++;

        elements.put(currentIndex, e);
        currentIndex++;

        return true;
    }

    /**
     * Resets the List to its base elements.
     */
    public void reset() {
        elements.clear();
        currentIndex = 0;
        base.forEach(elements::put);
    }

    /**
     * @return The List of elements ordered by its positions.
     */
    public List<T> values() {
        return new ArrayList<>(elements.values());
    }

    @Override
    public String toString() {
        return elements.values().toString();
    }
}
