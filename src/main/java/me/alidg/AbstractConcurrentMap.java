package me.alidg;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public abstract class AbstractConcurrentMap<K, V> implements ConcurrentMap<K, V> {

    protected static final int INITIAL_CAP = 100;
    protected List<Entry<K, V>>[] table;
    protected int size = 0;

    @SuppressWarnings("unchecked")
    public AbstractConcurrentMap() {
        table = (List<Entry<K, V>>[]) new List[INITIAL_CAP];
        for (int i = 0; i < INITIAL_CAP; i++) {
            table[i] = new ArrayList<>();
        }
    }

    protected abstract void acquire(Entry<K, V> entry);

    protected abstract void release(Entry<K, V> entry);

    protected abstract boolean shouldResize();

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
