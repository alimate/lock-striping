package me.alidg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

/**
 * A very simple {@link ConcurrentMap} implementation the uses one lock to synchronize all operations.
 *
 * @param <K> The map key type.
 * @param <V> The map value type.
 */
public final class CoarseGrainedConcurrentMap<K, V> extends AbstractConcurrentMap<K, V> {

    private final ReentrantLock lock;

    public CoarseGrainedConcurrentMap() {
        super();
        lock = new ReentrantLock();
    }

    @Override
    protected void acquire(Entry<K, V> entry) {
        lock.lock();
    }

    @Override
    protected void release(Entry<K, V> entry) {
        lock.unlock();
    }

    @Override
    protected boolean shouldResize() {
        return size / table.length >= 5;
    }

    @Override
    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    protected void resize() {
        lock.lock();
        try {
            var oldTable = table;
            var doubledCapacity = oldTable.length * 2;

            table = (List<Entry<K, V>>[]) new List[doubledCapacity];
            for (var i = 0; i < doubledCapacity; i++) {
                table[i] = new ArrayList<>();
            }

            for (var entries : oldTable) {
                for (var entry : entries) {
                    var newBucketNumber = abs(entry.hashCode()) % doubledCapacity;
                    table[newBucketNumber].add(entry);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
