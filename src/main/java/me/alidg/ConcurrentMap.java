package me.alidg;

/**
 * A very simple contract for concurrent maps.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface ConcurrentMap<K, V> {

    /**
     * Adds a new entry to the map under the given {@code key} with the given {@code value}.
     *
     * @param key   The key to add.
     * @param value The corresponding value for the given key.
     * @return true if successfully managed to add the requested element, false otherwise.
     */
    boolean put(K key, V value);

    /**
     * Returns the value of the given {@code key}.
     *
     * @param key The key to search for.
     * @return The value corresponding the given key or null when there is no such mapping.
     */
    V get(K key);

    /**
     * Removes the entry represented by the given {@code key}.
     *
     * @param key The key to delete.
     * @return true if the entry was successfully removed. Returns false otherwise.
     */
    boolean remove(K key);
}
