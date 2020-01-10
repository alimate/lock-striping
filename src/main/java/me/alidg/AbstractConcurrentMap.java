package me.alidg;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * An abstract super class for all {@link ConcurrentMap} implementation encapsulating all common
 * operations.
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public abstract class AbstractConcurrentMap<K, V> implements ConcurrentMap<K, V> {

    /**
     * The initial number of buckets. This is, in fact, a magic number, shamelessly.
     */
    protected static final int INITIAL_CAP = 100;

    /**
     * Represents the buckets and their chained elements.
     */
    protected List<Entry<K, V>>[] table;

    /**
     * How many elements are stored in the map?
     */
    protected int size = 0;

    @SuppressWarnings("unchecked")
    public AbstractConcurrentMap() {
        table = (List<Entry<K, V>>[]) new List[INITIAL_CAP];
        for (int i = 0; i < INITIAL_CAP; i++) {
            table[i] = new ArrayList<>();
        }
    }

    /**
     * Tries to acquire a lock to perform an operation on the given {@code entry}.
     *
     * @param entry The entry to block on.
     */
    protected abstract void acquire(Entry<K, V> entry);

    /**
     * Tries to release a lock after performing some operation on the given {@code entry}.
     *
     * @param entry The entry to release the lock for.
     */
    protected abstract void release(Entry<K, V> entry);

    /**
     * @return Whether should we add more buckets to the hash table?
     */
    protected abstract boolean shouldResize();

    /**
     * Adds more buckets to the hash table.
     */
    protected abstract void resize();

    @Override
    public boolean put(K key, V value) {
        if (key == null) return false;

        var result = false;
        var entry = new Entry<>(key, value);
        acquire(entry);
        try {
            var bucketNumber = findBucket(entry);
            if (!table[bucketNumber].contains(entry)) {
                table[bucketNumber].add(entry);
                size++;
                result = true;
            }
        } finally {
            release(entry);
        }

        if (shouldResize()) resize();
        return result;
    }

    @Override
    public V get(K key) {
        if (key == null) return null;

        var entry = new Entry<K, V>(key, null);
        acquire(entry);
        try {
            var bucket = table[findBucket(entry)];
            return bucket.stream().filter(e -> e.equals(entry)).findFirst().map(Entry::getValue).orElse(null);
        } finally {
            release(entry);
        }
    }

    @Override
    public boolean remove(K key) {
        if (key == null) return false;

        var entry = new Entry<K, V>(key, null);
        acquire(entry);
        try {
            return table[findBucket(entry)].remove(entry);
        } finally {
            release(entry);
        }
    }

    protected int findBucket(Entry<K, V> entry) {
        return abs(entry.hashCode()) % table.length;
    }
}
